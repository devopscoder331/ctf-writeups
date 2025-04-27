package db

import (
	"database/sql"
	"fmt"
	"io/ioutil"
	"log"
	"path/filepath"
	"strings"

	"building/internal/models"
	"github.com/mattn/go-sqlite3"
	"golang.org/x/crypto/bcrypt"
)

var DB *sql.DB

func init() {
	sql.Register("sqlite", &sqlite3.SQLiteDriver{
		ConnectHook: func(conn *sqlite3.SQLiteConn) error {
			if err := conn.RegisterFunc("bcryptHash", bcryptHash, true); err != nil {
				return fmt.Errorf("error registering bcryptHash function: %v", err)
			}
			if err := conn.RegisterFunc("setRole", setRole, true); err != nil {
				return fmt.Errorf("error registering setRole function: %v", err)
			}
			return nil
		},
	})
}

func bcryptHash(password string) (string, error) {
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return "", fmt.Errorf("error hashing password: %v", err)
	}
	return string(hashedPassword), nil
}

func setRole(targetUserID, currentUserID int64, role models.Role) (bool, error) {
	targetUser, err := GetUser(targetUserID)
	if err != nil {
		return false, err
	}

	currentUser, err := GetUser(currentUserID)
	if err != nil {
		return false, err
	}

	if currentUser.RoleID != models.Administrator {
		if currentUser.RoomID == nil || targetUser.RoomID == nil || *currentUser.RoomID != *targetUser.RoomID {
			return false, nil
		}

		if currentUser.RoleID >= targetUser.RoleID {
			return false, nil
		}

		if currentUser.RoleID >= role {
			return false, nil
		}
	}

	if targetUser.RoleID == models.Administrator {
		return false, nil
	}

	result, err := DB.Exec(`
        UPDATE users
        SET role_id = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?`, role, targetUserID)
	if err != nil {
		return false, err
	}

	rowsAffected, err := result.RowsAffected()
	if err != nil {
		return false, err
	}

	return rowsAffected > 0, nil
}

func InitDB(dbPath string) error {
	var err error
	DB, err = sql.Open("sqlite", dbPath)
	if err != nil {
		return fmt.Errorf("error opening database: %v", err)
	}

	if err = DB.Ping(); err != nil {
		return fmt.Errorf("error connecting to the database: %v", err)
	}

	_, err = DB.Exec(`
        CREATE TABLE IF NOT EXISTS migrations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL UNIQUE,
            applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    `)
	if err != nil {
		return fmt.Errorf("error creating migrations table: %v", err)
	}

	return nil
}

func ChangeUserRole(targetUserID, currentUserID int64, role models.Role) (bool, error) {
	var success bool
	err := DB.QueryRow("SELECT setRole(?, ?, ?)", targetUserID, currentUserID, role).Scan(&success)
	if err != nil {
		return false, fmt.Errorf("error changing role: %v", err)
	}
	return success, nil
}

func GetUser(userID int64) (*models.User, error) {
	var user models.User
	var roomID sql.NullInt64

	err := DB.QueryRow(`
        SELECT id, username, password_hash, role_id, room_id, card_id
        FROM users 
        WHERE id = ?`, userID).Scan(
		&user.ID,
		&user.Username,
		&user.PasswordHash,
		&user.RoleID,
		&roomID,
		&user.CardID,
	)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, fmt.Errorf("user not found")
		}
		return nil, err
	}

	if roomID.Valid {
		user.RoomID = &roomID.Int64
	}

	return &user, nil
}

func GetUserByUsername(username string) (*models.User, error) {
	user := &models.User{}
	err := DB.QueryRow(`
        SELECT id, username, password_hash, role_id, room_id, card_id, created_at, updated_at
        FROM users WHERE username = ?`, username).Scan(
		&user.ID,
		&user.Username,
		&user.PasswordHash,
		&user.RoleID,
		&user.RoomID,
		&user.CardID,
		&user.CreatedAt,
		&user.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}
	return user, nil
}

func DeleteUser(userID, currentUserID int64) (bool, error) {
	tx, err := DB.Begin()
	if err != nil {
		return false, err
	}
	defer tx.Rollback()

	var currentUserRole, currentUserRoomID int64
	err = tx.QueryRow(`
        SELECT role_id, room_id
        FROM users WHERE id = ?`, currentUserID).Scan(&currentUserRole, &currentUserRoomID)
	if err != nil {
		return false, err
	}

	var targetUserRole, targetUserRoomID int64
	err = tx.QueryRow(`
        SELECT role_id, room_id
        FROM users WHERE id = ?`, userID).Scan(&targetUserRole, &targetUserRoomID)
	if err != nil {
		return false, err
	}

	if models.Role(currentUserRole) != models.Administrator {
		if currentUserRoomID != targetUserRoomID {
			return false, fmt.Errorf("insufficient permissions: user in different room")
		}

		if currentUserRole >= targetUserRole {
			return false, fmt.Errorf("insufficient permissions: target user has higher role")
		}
	}

	_, err = tx.Exec("DELETE FROM users WHERE id = ?", userID)
	if err != nil {
		return false, err
	}

	return true, tx.Commit()
}

func RunMigrations(migrationsDir string) error {
	files, err := ioutil.ReadDir(migrationsDir)
	if err != nil {
		return fmt.Errorf("error reading migrations directory: %v", err)
	}

	var migrationFiles []string
	for _, file := range files {
		if filepath.Ext(file.Name()) == ".sql" {
			migrationFiles = append(migrationFiles, file.Name())
		}
	}

	for _, fileName := range migrationFiles {
		var count int
		err := DB.QueryRow("SELECT COUNT(*) FROM migrations WHERE name = ?", fileName).Scan(&count)
		if err != nil {
			return fmt.Errorf("error checking migration status: %v", err)
		}

		if count > 0 {
			log.Printf("Skipping migration %s: already applied", fileName)
			continue
		}

		content, err := ioutil.ReadFile(filepath.Join(migrationsDir, fileName))
		if err != nil {
			return fmt.Errorf("error reading migration file %s: %v", fileName, err)
		}

		tx, err := DB.Begin()
		if err != nil {
			return fmt.Errorf("error starting transaction for migration %s: %v", fileName, err)
		}

		statements := splitStatements(string(content))

		for _, stmt := range statements {
			if stmt = strings.TrimSpace(stmt); stmt == "" {
				continue
			}

			if strings.HasPrefix(strings.ToUpper(stmt), "CREATE TABLE") {
				stmt = strings.Replace(
					stmt,
					"CREATE TABLE",
					"CREATE TABLE IF NOT EXISTS",
					1,
				)
			}

			_, err = tx.Exec(stmt)
			if err != nil {
				tx.Rollback()
				return fmt.Errorf("error executing statement in migration %s: %v\nStatement: %s", fileName, err, stmt)
			}
		}

		_, err = tx.Exec("INSERT INTO migrations (name) VALUES (?)", fileName)
		if err != nil {
			tx.Rollback()
			return fmt.Errorf("error recording migration %s: %v", fileName, err)
		}

		if err := tx.Commit(); err != nil {
			return fmt.Errorf("error committing migration %s: %v", fileName, err)
		}

		log.Printf("Applied migration: %s", fileName)
	}

	return nil
}

func splitStatements(content string) []string {
	statements := strings.Split(content, ";")
	var result []string

	for _, stmt := range statements {
		if strings.TrimSpace(stmt) != "" {
			result = append(result, stmt)
		}
	}

	return result
}

func Close() error {
	if DB != nil {
		return DB.Close()
	}
	return nil
}

func CreateUserWithGeneratedRoom(username string, password string, roleID models.Role) (*models.User, int64, error) {
	tx, err := DB.Begin()
	if err != nil {
		return nil, 0, fmt.Errorf("error starting transaction: %v", err)
	}
	defer tx.Rollback()

	result, err := tx.Exec(`
        INSERT INTO rooms (status) 
        VALUES (?)`,
		models.Available)
	if err != nil {
		return nil, 0, fmt.Errorf("error creating room: %v", err)
	}

	roomID, err := result.LastInsertId()
	if err != nil {
		return nil, 0, fmt.Errorf("error getting room ID: %v", err)
	}

	user := &models.User{
		Username:     username,
		PasswordHash: password,
		RoleID:       roleID,
		RoomID:       &roomID,
	}

	result, err = tx.Exec(`
        INSERT INTO users (username, password_hash, role_id, room_id) 
        VALUES (?, bcryptHash(?), ?, ?)`,
		user.Username, user.PasswordHash, user.RoleID, user.RoomID)
	if err != nil {
		return nil, 0, fmt.Errorf("error creating user: %v", err)
	}

	userID, err := result.LastInsertId()
	if err != nil {
		return nil, 0, fmt.Errorf("error getting user ID: %v", err)
	}
	user.ID = userID

	if err := tx.Commit(); err != nil {
		return nil, 0, fmt.Errorf("error committing transaction: %v", err)
	}

	return user, roomID, nil
}

type RoomDetails struct {
	ID        int64 `json:"id"`
	Status    int   `json:"status"`
	UserCount int   `json:"user_count"`
}

type RoomUser struct {
	ID       int64  `json:"id"`
	Username string `json:"username"`
	CardID   *int64 `json:"card_id"`
	RoleID   int64  `json:"role_id"`
}

func GetRoomDetails(roomID string) (*RoomDetails, error) {
	var room RoomDetails

	err := DB.QueryRow(`
        SELECT r.id, r.status, COUNT(u.id) as user_count
        FROM rooms r
        LEFT JOIN users u ON u.room_id = r.id
        WHERE r.id = ?
        GROUP BY r.id`, roomID).Scan(&room.ID, &room.Status, &room.UserCount)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, fmt.Errorf("room not found")
		}
		return nil, fmt.Errorf("error fetching room: %v", err)
	}

	return &room, nil
}

func ListRoomUsers(roomID string) ([]RoomUser, error) {
	rows, err := DB.Query(`
        SELECT id, username, card_id, role_id
        FROM users
        WHERE room_id = ?
        ORDER BY id`, roomID)
	if err != nil {
		return nil, fmt.Errorf("error fetching users: %v", err)
	}
	defer rows.Close()

	var users []RoomUser
	for rows.Next() {
		var user RoomUser
		if err := rows.Scan(&user.ID, &user.Username, &user.CardID, &user.RoleID); err != nil {
			return nil, fmt.Errorf("error scanning user: %v", err)
		}
		users = append(users, user)
	}

	if err = rows.Err(); err != nil {
		return nil, fmt.Errorf("error iterating users: %v", err)
	}

	return users, nil
}

func CreateUser(user *models.User) (error) {
	query := `
		INSERT INTO users (username, password_hash, role_id, room_id, card_id, created_at, updated_at)
		VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
	`
	result, err := DB.Exec(query, user.Username, user.PasswordHash, user.RoleID, user.RoomID, user.CardID)
	if err != nil {
		return err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return err
	}

	user.ID = id
	return nil
}

func GetRoomCount() (int, error) {
	var count int
	err := DB.QueryRow(`SELECT count(1) as cnt FROM rooms`).Scan(&count)
	if err != nil {
		return 0, fmt.Errorf("error getting room count: %v", err)
	}
	return count, nil
}

func GetUserCount() (int, error) {
	var count int
	err := DB.QueryRow(`SELECT count(1) as cnt FROM users`).Scan(&count)
	if err != nil {
		return 0, fmt.Errorf("error getting user count: %v", err)
	}
	return count, nil
}
