<?php
require_once 'config.php';


if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Handle both JSON and form data
    $input = json_decode(file_get_contents('php://input'), true);
    $username = trim(($input['username'] ?? $_POST['username']) ?? '');
    $password = ($input['password'] ?? $_POST['password']) ?? '';
    
    if ($username === ADMIN_USERNAME && $password === ADMIN_PASSWORD) {
        session_regenerate_id(true);
        $_SESSION['user_id'] = 1;
        $_SESSION['username'] = ADMIN_USERNAME;
        
        echo json_encode([
            'success' => true, 
            'message' => 'Admin login successful',
            'user' => [
                'id' => 1,
                'username' => ADMIN_USERNAME,
                'is_admin' => true,
                'flag' => FLAG
            ]
        ]);
        exit;
    } else {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Invalid admin credentials']);
        exit;
    }
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}
?>
