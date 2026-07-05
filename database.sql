-- Database Initialization for Lumina Gallery
-- Creates the database schema, user-facing image details table, and appropriate indexes.

CREATE DATABASE IF NOT EXISTS `image_gallery_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `image_gallery_db`;

-- Create Table for Uploaded Images
CREATE TABLE IF NOT EXISTS `images` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL DEFAULT 'Untitled Image',
    `file_path` VARCHAR(512) NOT NULL,
    `file_size` INT NOT NULL,
    `upload_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `aspect_ratio` DECIMAL(3,2) DEFAULT 1.00,
    INDEX `idx_upload_date` (`upload_date` DESC),
    INDEX `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
