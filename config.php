<?php
/**
 * Configuration file for the Image Sharing Gallery
 * Handles database connection details and global configuration variables.
 */

// Database Credentials
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'image_gallery_db');

// Global Password Requirement
define('UPLOAD_PASSWORD', 'xarvind07');

// Maximum File Size: 10MB (in bytes)
define('MAX_FILE_SIZE', 10 * 1024 * 1024);

// Allowed MIME Types
$allowed_mime_types = [
    'image/jpeg' => 'jpg',
    'image/png'  => 'png',
    'image/webp' => 'webp'
];

try {
    // Establish PDO Database Connection
    $pdo = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4", DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES   => false,
    ]);
} catch (PDOException $e) {
    // In production, log error instead of printing details directly
    die("Database Connection Failed: " . htmlspecialchars($e->getMessage()));
}
?>
