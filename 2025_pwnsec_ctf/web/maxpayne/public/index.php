<?php
require_once 'config.php';

// Set CSP header for backend API
header("Content-Security-Policy: default-src 'self'; style-src https://cdn.jsdelivr.net 'sha256-47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU='; script-src 'none'; object-src 'none'");
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

// API status endpoint
echo json_encode([
    'success' => true,
    'message' => 'Note-Taking API Backend',
    'version' => '1.0',
    'endpoints' => [
        'POST /login.php' => 'User authentication',
        'POST /register.php' => 'User registration',
        'POST /admin-login.php' => 'Admin authentication',
        'GET /dashboard.php' => 'Get dashboard data',
        'POST /dashboard.php' => 'Create note or report note',
        'GET /view_note.php?id={id}' => 'View specific note',
        'GET|POST /logout.php' => 'Logout user'
    ],
    'csp_controlled_by' => 'backend',
    'logged_in' => isLoggedIn(),
    'is_admin' => isAdmin()
]);
exit;
?>