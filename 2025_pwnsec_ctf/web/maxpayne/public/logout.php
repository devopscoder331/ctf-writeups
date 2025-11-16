<?php
require_once 'config.php';

// Set CSP header for backend API
header("Content-Security-Policy: default-src 'self'; style-src https://cdn.jsdelivr.net 'sha256-47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU='; script-src 'none'; object-src 'none'");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

session_destroy();
echo json_encode(['success' => true, 'message' => 'Logged out successfully']);
exit;
?>