package api

import (
	"errors"
	"net/http"
	"strings"

	"syllabus/internal/model"
	"syllabus/internal/storage"
	"syllabus/internal/utils"
	"syllabus/pkg/logger"

	"github.com/gin-contrib/sessions"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

type AuthRequest struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

func Register(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req AuthRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, NewWrongJsonDataError())
			return
		}

		if req.Username == "" || req.Password == "" {
			c.JSON(http.StatusBadRequest, NewEmptyCredentialsError())
			return
		}

		if len(req.Username) < 9 || len(req.Password) < 9 {
			c.JSON(http.StatusBadRequest, NewShortCredentialsError())
			return
		}

		user, err := s.Users().Create(req.Username, req.Password)
		if err != nil {
			if errors.Is(err, gorm.ErrDuplicatedKey) || strings.Contains(err.Error(), "duplicate key value") || strings.Contains(err.Error(), "23505") {
				c.JSON(http.StatusBadRequest, NewUsernameAlreadyExistsError())
				return
			}

			logger.Errorf("Failed to create user %s: %v", req.Username, err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		// Примеры уроков
		if _, err := s.Lessons().Create(&model.Lesson{
			Name:        "Урок 1",
			Description: "Пример урока",
			Owner:       model.LessonOwnerTeacherEnglish,
			UserID:      user.ID,
			Position:    0,
		}); err != nil {
			logger.Errorf("Failed to create lesson: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		if _, err := s.Lessons().Create(&model.Lesson{
			Name:        "Урок 2",
			Description: "Пример урока",
			Owner:       model.LessonOwnerTeacherSocial,
			UserID:      user.ID,
			Position:    1,
		}); err != nil {
			logger.Errorf("Failed to create lesson: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		if _, err := s.Lessons().Create(&model.Lesson{
			Name:        "Урок 3",
			Description: "Пример урока",
			Owner:       model.LessonOwnerTeacherLiterature,
			UserID:      user.ID,
			Position:    2,
		}); err != nil {
			logger.Errorf("Failed to create lesson: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}
	}
}

func Login(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req AuthRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, NewWrongJsonDataError())
			return
		}

		user, err := s.Users().FindByUsername(req.Username)
		if err != nil {
			c.JSON(http.StatusBadRequest, NewUserNotFoundError())
			return
		}

		if !utils.IsPasswordCorrect(req.Password, user.Password) {
			c.JSON(http.StatusBadRequest, NewWrongCredentialsError())
			return
		}

		session := sessions.Default(c)
		session.Set("user_id", user.ID)
		if err := session.Save(); err != nil {
			logger.Errorf("Failed to save session: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		c.JSON(http.StatusOK, NewLoginMessage())
	}
}

func Logout(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		session := sessions.Default(c)
		session.Clear()
		if err := session.Save(); err != nil {
			logger.Errorf("Failed to save session: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		c.JSON(http.StatusOK, NewLogoutMessage())
	}
}

func GetCurrentUser(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		session := sessions.Default(c)
		userID := session.Get("user_id")
		if userID == nil {
			c.JSON(http.StatusUnauthorized, NewUnauthorizedError())
			return
		}

		user, err := s.Users().FindByID(userID.(uint))
		if err != nil {
			logger.Errorf("Failed to find user %d: %v", userID.(uint), err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		c.JSON(http.StatusOK, user.ToDTO())
	}
}
