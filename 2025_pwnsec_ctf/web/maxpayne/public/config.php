<?php
define('DB_PATH', $_ENV['DB_PATH'] ?? '/var/www/html/database.sqlite');
define('ADMIN_USERNAME', $_ENV['ADMIN_USERNAME'] ?? 'adminnnnnnn');
define('ADMIN_PASSWORD', $_ENV['ADMIN_PASSWORD'] ?? 'adminnnnnnn');
define('APP_URL', $_ENV['APP_URL'] ?? 'http://localhost:80');
define('BOT_URL', $_ENV['BOT_URL'] ?? 'http://localhost:3000');
define('FLASK_URL', $_ENV['FLASK_URL'] ?? 'http://localhost:5000');
define('FLAG', $_ENV['FLAG'] ?? 'flag{DEFAULT}');

class Database {
    private $connection;
    
    public function __construct() {
        try {
            // Create database file if it doesn't exist
            if (!file_exists(DB_PATH)) {
                $this->initializeDatabase();
            }
            
            $this->connection = new PDO(
                "sqlite:" . DB_PATH,
                null,
                null,
                [
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
                ]
            );
            
            // Enable foreign key constraints for SQLite
            $this->connection->exec("PRAGMA foreign_keys = ON");
            
        } catch (PDOException $e) {
            die("Database connection failed: " . $e->getMessage());
        }
    }
    
    private function initializeDatabase() {
        // Create empty database file
        touch(DB_PATH);
        chmod(DB_PATH, 0666);
        
        // Connect and create tables
        $pdo = new PDO("sqlite:" . DB_PATH);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        
        // Create tables
        $pdo->exec("
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(50) NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        ");
        
        $pdo->exec("
            CREATE TABLE notes (
                id VARCHAR(16) PRIMARY KEY,
                user_id INTEGER NOT NULL,
                title VARCHAR(255) NOT NULL,
                content TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        ");
        
        // Create admin user
        $admin_hash = md5(ADMIN_PASSWORD);
        $stmt = $pdo->prepare("INSERT INTO users (username, password) VALUES (?, ?)");
        $stmt->execute([ADMIN_USERNAME, $admin_hash]);
        
        // Create a sample note for testing
        $note_id = $this->generateUUID();
        $sample_xss = 'hello';
        $stmt = $pdo->prepare("INSERT INTO notes (id, user_id, title, content) VALUES (?, ?, ?, ?)");
        $stmt->execute([$note_id, 1, 'XSS Test Note', $sample_xss]);
    }
    
    private function generateUUID() {
        return sprintf('%04x%04x%04x%04x', mt_rand(0, 0xffff), mt_rand(0, 0xffff), mt_rand(0, 0xffff), mt_rand(0, 0xffff));
    }
    
    public function getConnection() {
        return $this->connection;
    }
}

session_start();
function isLoggedIn() {
    return isset($_SESSION['user_id']);
}
function requireLogin() {
    if (!isLoggedIn()) {
        header('Location: /login.php');
        exit;
    }
}
function isAdmin() {
    return isLoggedIn() && $_SESSION['username'] === ADMIN_USERNAME;
}
function generateUUID() {
    return sprintf('%04x%04x%04x%04x', mt_rand(0, 0xffff), mt_rand(0, 0xffff), mt_rand(0, 0xffff), mt_rand(0, 0xffff));
}
?>
