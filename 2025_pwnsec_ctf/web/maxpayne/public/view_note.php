<?php
require_once 'config.php';
header("Content-Security-Policy: default-src 'self'; style-src https://cdn.jsdelivr.net 'sha256-47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU='; script-src 'none'; object-src 'none'");


if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $note_id = $_GET['id'] ?? '';
    
    if (empty($note_id)) {
        http_response_code(404);
        echo json_encode(['success' => false, 'message' => 'Note ID is required']);
        exit;
    }
    
    $db = new Database();
    $stmt = $db->getConnection()->prepare("SELECT n.*, u.username FROM notes n JOIN users u ON n.user_id = u.id WHERE n.id = ?");
    $stmt->execute([$note_id]);
    $note = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$note) {
        http_response_code(404);
        echo json_encode(['success' => false, 'message' => 'Note not found']);
        exit;
    }
    
    // Check permissions
    if (isLoggedIn() && !isAdmin() && $note['user_id'] != $_SESSION['user_id']) {
        http_response_code(403);
        echo json_encode(['success' => false, 'message' => 'Access denied']);
        exit;
    }
    
    // Return JSON response with CSP header already set
    echo json_encode([
        'success' => true,
        'note' => [
            'id' => $note['id'],
            'title' => $note['title'],
            'content' => $note['content'],
            'username' => $note['username'],
            'user_id' => $note['user_id']
        ],
        'viewer' => [
            'is_logged_in' => isLoggedIn(),
            'is_admin' => isAdmin(),
            'username' => $_SESSION['username'] ?? null
        ]
    ]);
    exit;
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}
?>
