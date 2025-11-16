<?php
require_once 'config.php';

// Set CSP header for backend API
header("Content-Security-Policy: default-src 'self'; style-src https://cdn.jsdelivr.net 'sha256-47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU='; script-src 'none'; object-src 'none'");
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Handle both JSON and form data
    $input = json_decode(file_get_contents('php://input'), true);
    $username = trim(($input['username'] ?? $_POST['username']) ?? '');
    $password = ($input['password'] ?? $_POST['password']) ?? '';
    
    if (empty($username) || empty($password)) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Username and password are required']);
        exit;
    }
    
    if (strlen($password) < 4) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Password must be at least 4 characters']);
        exit;
    }
    
    $db = new Database();
    $stmt = $db->getConnection()->prepare("SELECT id FROM users WHERE username = ?");
    $stmt->execute([$username]);
    
    if ($stmt->fetch()) {
        http_response_code(409);
        echo json_encode(['success' => false, 'message' => 'Username already exists']);
        exit;
    }
    
    $stmt = $db->getConnection()->prepare("INSERT INTO users (username, password) VALUES (?, ?)");
    if ($stmt->execute([$username, $password])) {
        echo json_encode(['success' => true, 'message' => 'Registration successful! You can now login.']);
        exit;
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Registration failed. Please try again.']);
        exit;
    }
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}
?>