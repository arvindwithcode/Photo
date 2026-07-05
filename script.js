/**
 * Lumina Gallery - Modern Client-side Interaction Script
 * Handles custom password modals, image previews, full screen viewports,
 * client-side instant searches, and dynamic drag-and-drop feedback.
 */

document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const uploadFab = document.getElementById('uploadFab');
    const authModal = document.getElementById('authModal');
    const uploadModal = document.getElementById('uploadModal');
    const viewerModal = document.getElementById('viewerModal');

    const authForm = document.getElementById('authForm');
    const uploadForm = document.getElementById('uploadForm');
    const searchInput = document.getElementById('searchInput');

    const passwordInput = document.getElementById('passwordInput');
    const authError = document.getElementById('authError');

    const fileInput = document.getElementById('fileInput');
    const fileDragArea = document.getElementById('fileDragArea');
    const filePreview = document.getElementById('filePreview');
    const previewImg = document.getElementById('previewImg');
    const dragPrompt = document.getElementById('dragPrompt');

    // Viewer modal elements
    const viewerClose = document.getElementById('viewerClose');
    const viewerImg = document.getElementById('viewerImg');
    const viewerTitle = document.getElementById('viewerTitle');
    const viewerMeta = document.getElementById('viewerMeta');
    const viewerDownload = document.getElementById('viewerDownload');

    // Password requirement defined in config
    const ADMIN_PASSWORD = 'xarvind07';

    // Search Filtering Functionality (Instant search)
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const items = document.querySelectorAll('.gallery-item');
            let visibleCount = 0;

            items.forEach(item => {
                const title = item.getAttribute('data-title').toLowerCase();
                if (title.includes(query)) {
                    item.style.display = 'block';
                    visibleCount++;
                } else {
                    item.style.display = 'none';
                }
            });

            // Display empty state if no images are visible
            const emptyState = document.getElementById('emptyState');
            if (emptyState) {
                if (visibleCount === 0) {
                    emptyState.style.display = 'block';
                    emptyState.querySelector('h3').textContent = 'No matching images';
                    emptyState.querySelector('p').textContent = 'Try adjusting your search criteria.';
                } else {
                    emptyState.style.display = 'none';
                }
            }
        });
    }

    // Modal helpers
    function openModal(modal) {
        modal.classList.add('active');
    }

    function closeModal(modal) {
        modal.classList.remove('active');
        // Reset forms inside if any
        const form = modal.querySelector('form');
        if (form) form.reset();
    }

    // Floating upload button flow
    if (uploadFab) {
        uploadFab.addEventListener('click', () => {
            // First ask for password
            openModal(authModal);
            passwordInput.focus();
        });
    }

    // Close on overlay click
    document.querySelectorAll('.modal-overlay').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModal(modal);
            }
        });
    });

    // Close buttons inside modals
    document.querySelectorAll('[data-close]').forEach(button => {
        button.addEventListener('click', (e) => {
            const modal = e.target.closest('.modal-overlay');
            if (modal) closeModal(modal);
        });
    });

    // Cancel buttons
    document.querySelectorAll('.btn-secondary').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const modal = e.target.closest('.modal-overlay');
            if (modal) closeModal(modal);
        });
    });

    // Password submission check
    if (authForm) {
        authForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const val = passwordInput.value;

            if (val === ADMIN_PASSWORD) {
                // Correct -> hide auth, open upload
                closeModal(authModal);
                authError.style.display = 'none';
                
                // Set the correct password to a hidden input field in the upload form
                document.getElementById('hiddenPassword').value = val;
                
                openModal(uploadModal);
            } else {
                // Wrong password
                authError.style.display = 'block';
                passwordInput.select();
            }
        });
    }

    // File selection drag & drop functionality
    if (fileDragArea && fileInput) {
        fileDragArea.addEventListener('click', () => fileInput.click());

        fileDragArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            fileDragArea.classList.add('dragover');
        });

        fileDragArea.addEventListener('dragleave', () => {
            fileDragArea.classList.remove('dragover');
        });

        fileDragArea.addEventListener('drop', (e) => {
            e.preventDefault();
            fileDragArea.classList.remove('dragover');
            
            if (e.dataTransfer.files.length > 0) {
                const file = e.dataTransfer.files[0];
                fileInput.files = e.dataTransfer.files;
                handleFileSelected(file);
            }
        });

        fileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                handleFileSelected(e.target.files[0]);
            }
        });
    }

    function handleFileSelected(file) {
        // Validate client side
        const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
        if (!validTypes.includes(file.type)) {
            alert('Invalid file format. Please upload JPG, PNG, or WEBP images only.');
            fileInput.value = '';
            return;
        }

        if (file.size > 10 * 1024 * 1024) {
            alert('File too large. Maximum size allowed is 10MB.');
            fileInput.value = '';
            return;
        }

        // Show image preview
        const reader = new FileReader();
        reader.onload = (e) => {
            previewImg.src = e.target.result;
            filePreview.style.display = 'block';
            dragPrompt.style.display = 'none';
        };
        reader.readAsDataURL(file);
    }

    // Reset preview on form reset
    if (uploadForm) {
        uploadForm.addEventListener('reset', () => {
            filePreview.style.display = 'none';
            dragPrompt.style.display = 'block';
            previewImg.src = '';
        });
    }

    // Full Screen Detail Viewer Opening
    const galleryItems = document.querySelectorAll('.gallery-item');
    galleryItems.forEach(item => {
        item.addEventListener('click', (e) => {
            // Skip if clicking the quick download button directly
            if (e.target.closest('.quick-download-btn')) {
                return;
            }

            const title = item.getAttribute('data-title');
            const path = item.getAttribute('data-path');
            const date = item.getAttribute('data-date');
            const size = item.getAttribute('data-size');

            // Populate viewer modal
            viewerImg.src = path;
            viewerTitle.textContent = title;
            viewerMeta.textContent = `Uploaded on ${date} • ${size}`;
            viewerDownload.href = path;
            viewerDownload.download = title;

            openModal(viewerModal);
        });
    });

    if (viewerClose) {
        viewerClose.addEventListener('click', () => {
            closeModal(viewerModal);
        });
    }
});
