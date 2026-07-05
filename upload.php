<?php
/**
 * Secure Backend Image Upload Processor
 * Verifies passwords, validates images strictly, and registers meta details in database.
 */

require_once 'config.php';

// Support only POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header("Location: index.php");
    exit;
}

$error_message = "";

try {
    // 1. Password Verification
    $password = isset($_POST['password']) ? trim($_POST['password']) : '';
    if ($password !== UPLOAD_PASSWORD) {
        throw new Exception("Incorrect Password. Upload privileges denied.");
    }

    // 2. Validate File presence
    if (!isset($_FILES['imageFile']) || $_FILES['imageFile']['error'] !== UPLOAD_ERR_OK) {
        $errorCode = isset($_FILES['imageFile']) ? $_FILES['imageFile']['error'] : -1;
        throw new Exception("File upload failed. Error code: $errorCode");
    }

    $file = $_FILES['imageFile'];

    // 3. Size validation
    if ($file['size'] > MAX_FILE_SIZE) {
        throw new Exception("File is too large. Maximum allowed size is 10MB.");
    }

    // 4. Mime type validation (Server side check to prevent spoofing)
    $finfo = new finfo(FILEINFO_MIME_TYPE);
    val_mime_type:
    $real_mime = $finfo->file($file['tmp_name']);

    if (!array_key_exists($real_mime, $allowed_mime_types)) {
        throw new Exception("Invalid file type. Only JPG, PNG, and WEBP images are allowed.");
    }

    // Double check with image measurement to prevent fake headers (polyglot files)
    $image_info = @getimagesize($file['tmp_name']);
    if ($image_info === false) {
        throw new Exception("Uploaded file is not a valid image structure.");
    }

    $width = $image_info[0];
    $height = $image_info[1];
    $aspect_ratio = round($width / $height, 2);

    // 5. Sanitize Title & Prepare Target Path
    $title = isset($_POST['title']) ? trim($_POST['title']) : '';
    if (empty($title)) {
        $title = "Untitled Image";
    }
    // Escape for secure output
    $title = htmlspecialchars($title, ENT_QUOTES, 'UTF-8');

    // Create unique name
    $ext = $allowed_mime_types[$real_mime];
    $safe_name = "img_" . time() . "_" . bin2hex(random_bytes(4)) . "." . $ext;
    
    $target_dir = "uploads/";
    if (!is_dir($target_dir)) {
        mkdir($target_dir, 0755, true);
    }
    
    $target_path = $target_dir . $safe_name;

    // 6. Move file to destination folder
    if (!move_uploaded_file($file['tmp_name'], $target_path)) {
        throw new Exception("Failed to save uploaded file on host server.");
    }

    // 7. Insert to MySQL Database via secure PDO Prepared Statement
    $stmt = $pdo->prepare("INSERT INTO images (title, file_path, file_size, aspect_ratio) VALUES (:title, :file_path, :file_size, :aspect_ratio)");
    $stmt->execute([
        ':title' => $title,
        ':file_path' => $target_path,
        ':file_size' => $file['size'],
        ':aspect_ratio' => $aspect_ratio
    ]);

    // Success -> Redirect to Home screen instantly
    header("Location: index.php?upload=success");
    exit;

} catch (Exception $e) {
    // Return error message via query parameter
    $error_message = urlencode($e->getMessage());
    header("Location: index.php?upload_error=" . $error_message);
    exit;
}
?>
