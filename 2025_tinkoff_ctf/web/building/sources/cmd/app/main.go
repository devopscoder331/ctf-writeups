package main

import (
	"context"
	"crypto/rand"
	"database/sql"
	"encoding/base64"
	"encoding/csv"
	"encoding/json"
	"fmt"
	"html/template"
	"io"
	"log"
	mathrand "math/rand"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"building/internal/db"
	"building/internal/models"

	"github.com/gorilla/mux"
	"github.com/gorilla/sessions"
	"golang.org/x/crypto/bcrypt"
)

var (
	store     = sessions.NewCookieStore([]byte(os.Getenv("SESSION_SECRET")))
	templates = template.Must(template.ParseGlob("templates/*.html"))
)

func main() {
	dbPath := "file:data/office.db"
	if err := db.InitDB(dbPath); err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	if err := db.RunMigrations("migrations"); err != nil {
		log.Fatal(err)
	}

	r := mux.NewRouter()

	r.PathPrefix("/static/").Handler(http.StripPrefix("/static/", http.FileServer(http.Dir("static"))))

	r.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path != "/" {
			http.NotFound(w, r)
			return
		}
		http.Redirect(w, r, "/login", http.StatusSeeOther)
	}).Methods("GET")

	r.HandleFunc("/login", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodGet {
			flash, _ := getFlash(w, r, "error")
			data := map[string]interface{}{
				"Title": "Login",
				"Flash": flash,
			}
			if err := templates.ExecuteTemplate(w, "login.html", data); err != nil {
				http.Error(w, "Error rendering template", http.StatusInternalServerError)
				return
			}
			return
		}
		handleLogin(w, r)
	}).Methods("GET", "POST")

	r.HandleFunc("/register", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodGet {
			data := map[string]interface{}{
				"Title": "Register",
			}

			if flash, err := getFlash(w, r, "error"); err == nil && flash != "" {
				data["Flash"] = map[string]string{
					"Type":    "danger",
					"Message": flash,
				}
			}

			if err := templates.ExecuteTemplate(w, "register.html", data); err != nil {
				log.Printf("Template error: %v", err)
				http.Error(w, "Error rendering template", http.StatusInternalServerError)
				return
			}
			return
		}
		handleRegister(w, r)
	}).Methods("GET", "POST")

	r.HandleFunc("/dashboard", func(w http.ResponseWriter, r *http.Request) {
		userID, ok := isAuthenticated(r)
		if !ok {
			http.Redirect(w, r, "/login", http.StatusSeeOther)
			return
		}

		user, err := db.GetUser(userID)
		if err != nil {
			http.Redirect(w, r, "/login", http.StatusSeeOther)
			return
		}

		flash, _ := getFlash(w, r, "message")
		data := map[string]interface{}{
			"Title": "Dashboard",
			"User":  user,
			"Flash": flash,
		}

		if err := templates.ExecuteTemplate(w, "dashboard.html", data); err != nil {
			http.Error(w, "Error rendering template", http.StatusInternalServerError)
			log.Printf("Template error: %v", err)
			return
		}
	}).Methods("GET")

	r.HandleFunc("/admin", handleAdmin).Methods("GET")
	r.HandleFunc("/sample_csv", handleSampleCSV).Methods("GET")

	api := r.PathPrefix("/api").Subrouter()
	api.HandleFunc("/register", handleRegister).Methods("POST")
	api.HandleFunc("/login", handleLogin).Methods("POST")
	api.HandleFunc("/logout", handleLogout).Methods("POST")
	api.HandleFunc("/rooms/{id}", handleGetRoom).Methods("GET")
	api.HandleFunc("/rooms/{id}/users", handleListRoomUsers).Methods("GET")
	api.HandleFunc("/rooms/{id}/users", handleUploadUsers).Methods("POST")
	api.HandleFunc("/users/{id}/delete", handleDeleteUser).Methods("POST")
	api.HandleFunc("/users/{id}/change-role", handleChangeUserRole).Methods("POST")

	log.Printf("Server starting on :8080")
	log.Fatal(http.ListenAndServe(":8080", r))
}

func isAuthenticated(r *http.Request) (int64, bool) {
	session, err := store.Get(r, "session-name")
	if err != nil {
		return 0, false
	}

	userID, ok := session.Values["user_id"].(int64)
	if !ok {
		return 0, false
	}

	return userID, true
}

func setFlash(w http.ResponseWriter, r *http.Request, name string, value string) {
	session, err := store.Get(r, "session-name")
	if err != nil {
		return
	}
	session.AddFlash(value, name)
	session.Save(r, w)
}

func getFlash(w http.ResponseWriter, r *http.Request, name string) (string, error) {
	session, err := store.Get(r, "session-name")
	if err != nil {
		return "", err
	}

	if flashes := session.Flashes(name); len(flashes) > 0 {
		session.Save(r, w)
		return flashes[0].(string), nil
	}
	return "", nil
}

func handleRegister(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Username string `json:"username"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	existingUser, err := db.GetUserByUsername(req.Username)
	if err == nil && existingUser != nil {
		http.Error(w, "Username already exists", http.StatusConflict)
		return
	}

	password := generatePassword()

	user, roomID, err := db.CreateUserWithGeneratedRoom(req.Username, password, models.Renter)
	if err != nil {
		log.Printf("Error creating user: %v", err)
		if strings.Contains(err.Error(), "UNIQUE constraint") {
			http.Error(w, "Username already exists", http.StatusConflict)
			return
		}
		http.Error(w, "Failed to create user", http.StatusInternalServerError)
		return
	}

	response := struct {
		Username string `json:"username"`
		Password string `json:"password"`
		RoomID   string `json:"room_id"`
	}{
		Username: user.Username,
		Password: password,
		RoomID:   strconv.FormatInt(roomID, 10),
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

func handleLogin(w http.ResponseWriter, r *http.Request) {
	var input struct {
		Username string `json:"username"`
		Password string `json:"password"`
	}

	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	user, err := db.GetUserByUsername(input.Username)
	if err != nil {
		http.Error(w, "Invalid credentials", http.StatusUnauthorized)
		return
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.PasswordHash), []byte(input.Password)); err != nil {
		http.Error(w, "Invalid credentials", http.StatusUnauthorized)
		return
	}

	session, _ := store.Get(r, "session-name")
	session.Values["user_id"] = user.ID
	session.Save(r, w)

	json.NewEncoder(w).Encode(user)
}

func handleLogout(w http.ResponseWriter, r *http.Request) {
	session, err := store.Get(r, "session-name")
	if err != nil {
		http.Error(w, "Session error", http.StatusBadRequest)
		return
	}

	session.Values = make(map[interface{}]interface{})
	session.Options.MaxAge = -1

	if err := session.Save(r, w); err != nil {
		http.Error(w, "Error saving session", http.StatusBadRequest)
		return
	}

	w.WriteHeader(http.StatusOK)
}

func handleGetRoom(w http.ResponseWriter, r *http.Request) {
	_, ok := isAuthenticated(r)
	if !ok {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	vars := mux.Vars(r)
	roomID := vars["id"]

	room, err := db.GetRoomDetails(roomID)
	if err != nil {
		if err.Error() == "room not found" {
			http.Error(w, "Room not found", http.StatusNotFound)
			return
		}
		http.Error(w, "Error fetching room", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(room)
}

func handleListRoomUsers(w http.ResponseWriter, r *http.Request) {
	userID, ok := isAuthenticated(r)
	if !ok {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	vars := mux.Vars(r)
	roomID := vars["id"]

	user, err := db.GetUser(userID)
	if err != nil {
		http.Error(w, "Error fetching user", http.StatusInternalServerError)
		return
	}
	userRoleID := user.RoleID
	userRoomID := *user.RoomID

	targetRoomID, _ := strconv.ParseInt(roomID, 10, 64)
	if userRoleID != models.Administrator && userRoomID != targetRoomID {
		http.Error(w, "Access denied", http.StatusForbidden)
		return
	}

	users, err := db.ListRoomUsers(roomID)
	if err != nil {
		log.Printf("Error fetching users: %v", err)
		http.Error(w, "Error fetching users", http.StatusBadRequest)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(users)
}

func handleUploadUsers(w http.ResponseWriter, r *http.Request) {
	userID, ok := isAuthenticated(r)
	if !ok {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	vars := mux.Vars(r)
	roomID := vars["id"]

	roomIDInt, err := strconv.ParseInt(roomID, 10, 64)
	if err != nil {
		http.Error(w, "invalid room ID", http.StatusBadRequest)
		return
	}

	userModel, err := db.GetUser(userID)
	if err != nil {
		http.Error(w, "Error fetching user role", http.StatusBadRequest)
		return
	}

	if userModel.RoleID != models.Administrator {
		if *userModel.RoomID != roomIDInt {
			http.Error(w, "Error checking room access", http.StatusForbidden)
			return
		}
	}

	room, err := db.GetRoomDetails(roomID)
	if err != nil {
		http.Error(w, "Error fetching room details", http.StatusBadRequest)
		return
	}

	if room.UserCount >= 10 {
		http.Error(w, "Room is full", http.StatusForbidden)
		return
	}

	file, header, err := r.FormFile("file")
	if err != nil {
		http.Error(w, "Error reading file", http.StatusBadRequest)
		return
	}
	defer file.Close()

	ext := filepath.Ext(header.Filename)
	switch ext {
	case ".csv":
		err = processCSVUsers(file, roomIDInt, userModel.RoleID)
	case ".db":
		err = processSQLiteUsers(file, roomIDInt, userModel.RoleID)
	default:
		http.Error(w, "Unsupported file format", http.StatusBadRequest)
		return
	}

	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	w.WriteHeader(http.StatusOK)
}

func createUserInRoom(username, cardID, role, password string, roomIDInt int64, currentUserRole models.Role) error {
	cardIDInt, err := strconv.ParseInt(cardID, 10, 64)
	if err != nil {
		return fmt.Errorf("invalid card ID: %v", err)
	}

	var roleID models.Role
	switch role {
	case "user":
		roleID = models.RegularUser
	case "manager":
		roleID = models.Manager
	case "admin":
		roleID = models.Administrator
	default:
		return nil
	}

	if currentUserRole >= roleID && currentUserRole != models.Administrator {
		return nil
	}

	hashedPassword, _ := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)

	usernameSuffix := strconv.Itoa(int(roomIDInt))

	user := &models.User{
		Username:     username + "@" + usernameSuffix,
		PasswordHash: string(hashedPassword),
		RoleID:       roleID,
		RoomID:       &roomIDInt,
		CardID:       &cardIDInt,
	}

	if err := db.CreateUser(user); err != nil {
		if strings.Contains(err.Error(), "UNIQUE constraint") {
			log.Printf("Skipping duplicate username: %s", username)
			return nil
		}
		return fmt.Errorf("error creating user %s: %v", username, err)
	}

	return nil
}

func processCSVUsers(file io.Reader, roomIDInt int64, currentUserRole models.Role) error {
	// username, card_id, role, password
	reader := csv.NewReader(file)
	reader.FieldsPerRecord = 4

	if _, err := reader.Read(); err != nil {
		return fmt.Errorf("error reading CSV header: %v", err)
	}

	cnt := 0
	for {
		record, err := reader.Read()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		username := record[0]
		cardID := record[1]
		role := record[2]
		password := record[3]

		if err := createUserInRoom(username, cardID, role, password, roomIDInt, currentUserRole); err != nil {
			return err
		}
		cnt++

		if cnt > 10 {
			break
		}
	}

	return nil
}

func processSQLiteUsers(file io.Reader, roomIDInt int64, currentUserRole models.Role) error {
	tempFile, err := os.CreateTemp("", "uploaded-*.db")
	if err != nil {
		return err
	}
	defer os.Remove(tempFile.Name())
	defer tempFile.Close()

	if _, err := io.Copy(tempFile, file); err != nil {
		return err
	}

	uploadedDB, err := sql.Open("sqlite", tempFile.Name())
	if err != nil {
		return err
	}
	defer uploadedDB.Close()

	ctx, _ := context.WithTimeout(context.Background(), 1*time.Second)
	rows, err := uploadedDB.QueryContext(ctx, "SELECT username, card_id, role, password FROM users")
	if err != nil {
		return err
	}
	defer rows.Close()

	cnt := 0
	for rows.Next() {
		var username, cardID, role, password string
		if err := rows.Scan(&username, &cardID, &role, &password); err != nil {
			return err
		}

		if err := createUserInRoom(username, cardID, role, password, roomIDInt, currentUserRole); err != nil {
			return err
		}
		cnt++

		if cnt > 10 {
			break
		}
	}

	return nil
}

func generatePassword() string {
	randomBytes := make([]byte, 12)
	if _, err := rand.Read(randomBytes); err != nil {
		log.Printf("Error generating random bytes: %v", err)
		return ""
	}

	return base64.URLEncoding.EncodeToString(randomBytes)
}

func handleDeleteUser(w http.ResponseWriter, r *http.Request) {
	userID, ok := isAuthenticated(r)
	if !ok {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	vars := mux.Vars(r)
	targetUserID := vars["id"]
	targetID, err := strconv.ParseInt(targetUserID, 10, 64)
	if err != nil {
		http.Error(w, "Invalid user ID", http.StatusBadRequest)
		return
	}

	success, err := db.DeleteUser(targetID, userID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusForbidden)
		return
	}
	if !success {
		http.Error(w, "Permission denied", http.StatusForbidden)
		return
	}

	w.WriteHeader(http.StatusOK)
}

func handleChangeUserRole(w http.ResponseWriter, r *http.Request) {
	userID, ok := isAuthenticated(r)
	if !ok {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	vars := mux.Vars(r)
	targetUserID := vars["id"]
	targetID, err := strconv.ParseInt(targetUserID, 10, 64)
	if err != nil {
		http.Error(w, "Invalid user ID", http.StatusBadRequest)
		return
	}

	var input struct {
		NewRole string `json:"new_role"`
	}
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	var roleID models.Role
	switch input.NewRole {
	case "user":
		roleID = models.RegularUser
	case "manager":
		roleID = models.Manager
	case "renter":
		roleID = models.Manager
	case "admin":
		roleID = models.Administrator
	}
	if roleID == 0 {
		http.Error(w, "Invalid roleID", http.StatusBadRequest)
		return
	}

	success, err := db.ChangeUserRole(targetID, userID, roleID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}
	if !success {
		http.Error(w, "Permission denied or invalid role change", http.StatusForbidden)
		return
	}

	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(map[string]interface{}{
		"message":  "Role updated successfully",
		"new_role": input.NewRole,
	})
}

func handleAdmin(w http.ResponseWriter, r *http.Request) {
	userID, ok := isAuthenticated(r)
	if !ok {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	user, err := db.GetUser(userID)
	if err != nil {
		http.Error(w, "Database error", http.StatusInternalServerError)
		return
	}

	if user.RoleID != models.Administrator {
		data := map[string]interface{}{
			"Title": "Admin",
			"User":  user,
			"Alert": "К этой странице могут получить доступ только администраторы",
		}

		if err := templates.ExecuteTemplate(w, "admin.html", data); err != nil {
			http.Error(w, "Error rendering template", http.StatusInternalServerError)
			return
		}
		return
	}

	totalUsers, err := db.GetUserCount()
	if err != nil {
		http.Error(w, "Database error", http.StatusInternalServerError)
		return
	}

	totalRooms, err := db.GetRoomCount()
	if err != nil {
		http.Error(w, "Database error", http.StatusInternalServerError)
		return
	}

	data := map[string]interface{}{
		"Title":      "Admin",
		"User":       user,
		"Flag":       getFlag(w, r),
		"TotalUsers": totalUsers,
		"TotalRooms": totalRooms,
	}

	if err := templates.ExecuteTemplate(w, "admin.html", data); err != nil {
		http.Error(w, "Error rendering template", http.StatusInternalServerError)
		return
	}
}

func handleSampleCSV(w http.ResponseWriter, r *http.Request) {
	_, ok := isAuthenticated(r)
	if !ok {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	users := make([]struct {
		username string
		cardID   string
		role     string
		password string
	}, 5)

	roles := []string{"user", "manager"}

	for i := range users {
		usernameBytes := make([]byte, 8)
		rand.Read(usernameBytes)
		username := fmt.Sprintf("user_%x", base64.URLEncoding.EncodeToString(usernameBytes[:4]))
		users[i].username = username

		cardID := fmt.Sprintf("%d", mathrand.Int63n(1000000000000))
		users[i].cardID = cardID

		users[i].role = roles[mathrand.Intn(len(roles))]

		passwordBytes := make([]byte, 16)
		rand.Read(passwordBytes)
		users[i].password = base64.URLEncoding.EncodeToString(passwordBytes)[:16]
	}

	w.Header().Set("Content-Type", "text/csv")
	w.Header().Set("Content-Disposition", "attachment; filename=sample_users.csv")
	writer := csv.NewWriter(w)
	defer writer.Flush()

	if err := writer.Write([]string{"username", "card_uid", "role", "password"}); err != nil {
		http.Error(w, "Error writing CSV header", http.StatusInternalServerError)
		return
	}

	for _, user := range users {
		if err := writer.Write([]string{user.username, user.cardID, user.role, user.password}); err != nil {
			http.Error(w, "Error writing CSV data", http.StatusInternalServerError)
			return
		}
	}
}

func getFlag(w http.ResponseWriter, r *http.Request) string {
	return os.Getenv("FLAG")
}
