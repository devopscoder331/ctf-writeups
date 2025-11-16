<?php
require_once 'config.php';



if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

// Check authentication
if (!isLoggedIn()) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Authentication required']);
    exit;
}

$db = new Database();

// Handle POST requests (create note or report note)
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true);
    $action = $input['action'] ?? $_POST['action'] ?? '';
    
    if ($action === 'create_note') {
        $title = trim(($input['title'] ?? $_POST['title']) ?? '');
        $content = ($input['content'] ?? $_POST['content']) ?? '';
        
        if (empty($title) || empty($content)) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'Title and content are required']);
            exit;
        }
        
        $note_id = generateUUID();
        $stmt = $db->getConnection()->prepare("INSERT INTO notes (id, user_id, title, content) VALUES (?, ?, ?, ?)");
        
        if ($stmt->execute([$note_id, $_SESSION['user_id'], $title, $content])) {
            echo json_encode(['success' => true, 'message' => 'Note created successfully!', 'note_id' => $note_id]);
            exit;
        } else {
            http_response_code(500);
            echo json_encode(['success' => false, 'message' => 'Failed to create note']);
            exit;
        }
    }
    
    if ($action === 'report_note') {
        $note_id = ($input['note_id'] ?? $_POST['note_id']) ?? '';
        
        if (empty($note_id)) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => 'Note ID is required']);
            exit;
        }
        
        $note_url = FLASK_URL . "/view_note/" . $note_id;
        $bot_url = BOT_URL . "/";
        $post_data = http_build_query(['url' => $note_url]);
        $context = stream_context_create([
            'http' => [
                'method' => 'POST',
                'header' => "Content-Type: application/x-www-form-urlencoded\r\n",
                'content' => $post_data
            ]
        ]);
        
        $result = @file_get_contents($bot_url, false, $context);
        if ($result !== false) {
            $bot_response = json_decode($result, true);
            if ($bot_response && isset($bot_response['success'])) {
                echo json_encode(['success' => true, 'message' => 'Note reported successfully! Bot will visit: ' . $note_url]);
            } else {
                $error_msg = $bot_response['error'] ?? $result;
                echo json_encode(['success' => false, 'message' => 'Bot error: ' . $error_msg]);
            }
            exit;
        } else {
            http_response_code(500);
            echo json_encode(['success' => false, 'message' => 'Failed to notify bot server']);
            exit;
        }
    }
    
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Invalid action']);
    exit;
}

// Handle GET requests (get dashboard data)
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Get user's notes
    $stmt = $db->getConnection()->prepare("SELECT * FROM notes WHERE user_id = ? ORDER BY id DESC");
    $stmt->execute([$_SESSION['user_id']]);
    $notes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Get all notes if admin
    if (isAdmin()) {
        $stmt = $db->getConnection()->prepare("SELECT n.*, u.username FROM notes n JOIN users u ON n.user_id = u.id ORDER BY n.id DESC");
        $stmt->execute();
        $all_notes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    } else {
        $all_notes = $notes;
    }
    
    $response = [
        'success' => true,
        'user' => [
            'id' => $_SESSION['user_id'],
            'username' => $_SESSION['username'],
            'is_admin' => isAdmin()
        ],
        'notes' => $notes,
        'all_notes' => $all_notes
    ];
    
    if (isAdmin()) {
        $response['flag'] = FLAG;
    }
    
    echo json_encode($response);
    exit;
}
?>
