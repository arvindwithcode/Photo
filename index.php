<?php
/**
 * Lumina Gallery - Modern Glassmorphic Image Sharing Website
 * This acts as the main homepage layout pulling from MySQL database.
 */

require_once 'config.php';

// Fetch all images from database ordered by upload_date descending
try {
    $stmt = $pdo->query("SELECT * FROM images ORDER BY upload_date DESC");
    $images = $stmt->fetchAll();
} catch (PDOException $e) {
    // If table doesn't exist yet, we will define an empty array of images
    $images = [];
}

// Format file size helper
function format_bytes($bytes, $precision = 1) {
    if ($bytes <= 0) return '0 B';
    $units = array('B', 'KB', 'MB', 'GB');
    $bytes = max($bytes, 0);
    $pow = floor(($bytes ? log($bytes) : 0) / log(1024));
    $pow = min($pow, count($units) - 1);
    $bytes /= pow(1024, $pow);
    return round($bytes, $precision) . ' ' . $units[$pow];
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lumina Gallery - Glassmorphic Image Sharing</title>
    <!-- Modern Styling -->
    <link rel="stylesheet" href="style.css">
    <!-- Google Font -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;700;800&display=swap" rel="stylesheet">
</head>
<body>

    <!-- Header Panel -->
    <header>
        <div class="brand-section">
            <div class="brand-logo">
                <svg viewBox="0 0 24 24">
                    <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
                </svg>
            </div>
            <div class="brand-text">
                <h1>LUMINA</h1>
                <p>Responsive Glassmorphic Gallery</p>
            </div>
        </div>

        <div class="search-container">
            <input type="text" id="searchInput" class="search-input" placeholder="Search images by title...">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
        </div>
    </header>

    <!-- Feedback Alerts -->
    <?php if (isset($_GET['upload']) && $_GET['upload'] === 'success'): ?>
        <script>
            alert("Image uploaded successfully!");
            window.history.replaceState({}, document.title, window.location.pathname);
        </script>
    <?php endif; ?>

    <?php if (isset($_GET['upload_error'])): ?>
        <script>
            alert("Upload Failed: <?php echo htmlspecialchars($_GET['upload_error'], ENT_QUOTES, 'UTF-8'); ?>");
            window.history.replaceState({}, document.title, window.location.pathname);
        </script>
    <?php endif; ?>

    <!-- Main Gallery Container -->
    <main class="gallery-container">
        <?php if (empty($images)): ?>
            <!-- Empty state view -->
            <div class="empty-state" id="emptyState">
                <div class="empty-icon">
                    <svg viewBox="0 0 24 24">
                        <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
                    </svg>
                </div>
                <h3>Your Gallery is Empty</h3>
                <p>Click the floating "+" button in the bottom right corner to upload your first image.</p>
            </div>
        <?php else: ?>
            <div class="gallery-grid" id="galleryGrid">
                <?php foreach ($images as $img): ?>
                    <?php 
                        $title = htmlspecialchars($img['title'], ENT_QUOTES, 'UTF-8');
                        $path = htmlspecialchars($img['file_path'], ENT_QUOTES, 'UTF-8');
                        $date = date("Y-m-d H:i", strtotime($img['upload_date']));
                        $size = format_bytes($img['file_size']);
                    ?>
                    <div class="gallery-item" 
                         data-title="<?php echo $title; ?>" 
                         data-path="<?php echo $path; ?>" 
                         data-date="<?php echo $date; ?>" 
                         data-size="<?php echo $size; ?>">
                        <img src="<?php echo $path; ?>" class="gallery-img" alt="<?php echo $title; ?>" loading="lazy">
                        <div class="item-overlay">
                            <div class="item-info">
                                <p class="item-title"><?php echo $title; ?></p>
                                <p class="item-date"><?php echo $date; ?></p>
                            </div>
                            <a href="<?php echo $path; ?>" download="<?php echo $title; ?>" class="quick-download-btn" title="Download Image">
                                <svg viewBox="0 0 24 24">
                                    <path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z"/>
                                </svg>
                            </a>
                        </div>
                    </div>
                <?php endforeach; ?>
            </div>
            <!-- Client side fallback empty state for searches -->
            <div class="empty-state" id="emptyState" style="display: none;">
                <div class="empty-icon">
                    <svg viewBox="0 0 24 24">
                        <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
                    </svg>
                </div>
                <h3>No matching images</h3>
                <p>Try adjusting your search criteria.</p>
            </div>
        <?php endif; ?>
    </main>

    <!-- Floating upload FAB -->
    <div class="fab-btn" id="uploadFab" title="Upload Image">
        <svg viewBox="0 0 24 24">
            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
        </svg>
    </div>

    <!-- 1. Password/Authentication Modal -->
    <div class="modal-overlay" id="authModal">
        <div class="glass-modal">
            <div class="modal-header">
                <h3>Authentication Required</h3>
                <p>Please provide the verification password to unlock image uploads.</p>
            </div>
            <form id="authForm">
                <div class="input-group">
                    <label class="input-label" for="passwordInput">Password</label>
                    <input type="password" id="passwordInput" class="form-input" placeholder="Enter password" required autocomplete="current-password">
                    <span class="error-msg" id="authError">Incorrect Password. Access Denied.</span>
                </div>
                <div class="modal-actions">
                    <button type="button" class="btn btn-secondary">Cancel</button>
                    <button type="submit" class="btn btn-primary">Verify</button>
                </div>
            </form>
        </div>
    </div>

    <!-- 2. Secure Upload Form Modal -->
    <div class="modal-overlay" id="uploadModal">
        <div class="glass-modal">
            <div class="modal-header">
                <h3>Upload Image</h3>
                <p>Upload files in JPG, PNG or WEBP format. Maximum size is 10MB.</p>
            </div>
            <form id="uploadForm" action="upload.php" method="POST" enctype="multipart/form-data">
                <!-- Pass password back to server-side for backend check -->
                <input type="hidden" name="password" id="hiddenPassword">

                <div class="input-group">
                    <label class="input-label">Select Image</label>
                    <div class="file-drag-area" id="fileDragArea">
                        <input type="file" name="imageFile" id="fileInput" accept="image/jpeg, image/png, image/webp" style="display: none;" required>
                        <div class="file-preview" id="filePreview">
                            <img src="" id="previewImg" alt="Selected Preview">
                        </div>
                        <div id="dragPrompt">
                            <div class="drag-icon">
                                <svg viewBox="0 0 24 24">
                                    <path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"/>
                                </svg>
                            </div>
                            <p class="drag-text">Drag & Drop or Click to Browse</p>
                            <p class="drag-subtext">JPG, PNG, or WEBP up to 10MB</p>
                        </div>
                    </div>
                </div>

                <div class="input-group">
                    <label class="input-label" for="titleInput">Image Title</label>
                    <input type="text" name="title" id="titleInput" class="form-input" placeholder="Title (Optional)">
                </div>

                <div class="modal-actions">
                    <button type="reset" class="btn btn-secondary" data-close>Cancel</button>
                    <button type="submit" class="btn btn-primary">Upload</button>
                </div>
            </form>
        </div>
    </div>

    <!-- 3. Full Screen Detail Viewer Modal -->
    <div class="modal-overlay viewer-modal" id="viewerModal">
        <div class="glass-modal">
            <div class="viewer-header">
                <span class="viewer-close-btn" id="viewerClose" title="Close Viewer">
                    <svg viewBox="0 0 24 24">
                        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                    </svg>
                </span>
            </div>
            <div class="viewer-img-container">
                <img src="" id="viewerImg" class="viewer-img" alt="Detail Image">
            </div>
            <div class="viewer-details">
                <div class="viewer-meta">
                    <h4 id="viewerTitle">Untitled</h4>
                    <p id="viewerMeta">Uploaded on 2026-07-04 • 1.2 MB</p>
                </div>
                <a href="" id="viewerDownload" class="btn btn-primary viewer-download-btn" download>Download Image</a>
            </div>
        </div>
    </div>

    <!-- Core Script -->
    <script src="script.js"></script>
</body>
</html>
