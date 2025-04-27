package api

import (
	"encoding/json"
	"errors"
	"net/http"
	"strconv"

	"syllabus/internal/model"
	"syllabus/internal/setup"
	"syllabus/internal/storage"
	"syllabus/pkg/logger"

	"github.com/gin-gonic/gin"
)

type BulkDeleteRequest struct {
	IDs []uint `json:"ids" binding:"required"`
}

func GetAllLessons(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, exists := c.Get("user_id")
		if !exists {
			c.JSON(http.StatusUnauthorized, NewUnauthorizedError())
			return
		}

		lessons, err := s.Lessons().AllByUserID(userID.(uint))
		if err != nil {
			logger.Errorf("api.GetAllLessons: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		c.JSON(http.StatusOK, model.ToLessonDTOList(lessons))
	}
}

func FindLessonByID(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, NewWrongIdTypeError())
			return
		}

		lesson, err := s.Lessons().FindByID(uint(id))
		if err != nil {
			c.JSON(http.StatusInternalServerError, NewIdNotFoundError())
			return
		}

		userID, exists := c.Get("user_id")
		if !exists {
			c.JSON(http.StatusUnauthorized, NewUnauthorizedError())
			return
		}

		if lesson.UserID != userID.(uint) {
			c.JSON(http.StatusForbidden, NewNoPermissionsError())
			return
		}

		c.JSON(http.StatusOK, lesson.ToDTO())
	}
}

func CreateLesson(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		var lessonDTO model.LessonDTO
		decoder := json.NewDecoder(c.Request.Body)
		if err := decoder.Decode(&lessonDTO); err != nil {
			c.JSON(http.StatusBadRequest, NewWrongJsonDataError())
			return
		}
		defer c.Request.Body.Close()

		// Get user ID from context
		userID, exists := c.Get("user_id")
		if !exists {
			c.JSON(http.StatusUnauthorized, NewUnauthorizedError())
			return
		}

		// Check if the position is available
		available, err := setup.CheckPositionAvailable(s, userID.(uint), lessonDTO.Position, 0)
		if err != nil {
			logger.Errorf("api.CreateLesson: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		if !available {
			c.JSON(http.StatusBadRequest, NewPositionCollisionError())
			return
		}

		lesson := lessonDTO.ToModel()
		lesson.Owner = model.LessonOwnerStudent
		lesson.UserID = userID.(uint)

		lesson, err = s.Lessons().Create(lesson)
		if err != nil {
			logger.Errorf("api.CreateLesson: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		c.JSON(http.StatusOK, lesson.ToDTO())
	}
}

func UpdateLesson(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, NewWrongIdTypeError())
			return
		}

		var lessonDTO model.LessonDTO
		decoder := json.NewDecoder(c.Request.Body)
		if err := decoder.Decode(&lessonDTO); err != nil {
			c.JSON(http.StatusBadRequest, NewWrongJsonDataError())
			return
		}
		defer c.Request.Body.Close()

		lesson, err := s.Lessons().FindByID(uint(id))
		if err != nil {
			c.JSON(http.StatusInternalServerError, NewIdNotFoundError())
			return
		}

		userID, exists := c.Get("user_id")
		if !exists {
			c.JSON(http.StatusUnauthorized, NewUnauthorizedError())
			return
		}

		if lesson.UserID != userID.(uint) {
			c.JSON(http.StatusForbidden, NewNoPermissionsError())
			return
		}

		if lesson.Owner != model.LessonOwnerStudent {
			c.JSON(http.StatusForbidden, NewNoPermissionsError())
			return
		}

		available, err := setup.CheckPositionAvailable(s, userID.(uint), lessonDTO.Position, uint(id))
		if err != nil {
			logger.Errorf("api.UpdateLesson: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		if !available {
			c.JSON(http.StatusBadRequest, NewPositionCollisionError())
			return
		}

		updatedLesson := lessonDTO.ToModel()
		updatedLesson.Owner = model.LessonOwnerStudent
		updatedLesson.UserID = lesson.UserID
		updatedLesson, err = s.Lessons().Update(uint(id), updatedLesson)
		if err != nil {
			logger.Errorf("api.UpdateLesson: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		c.JSON(http.StatusOK, updatedLesson.ToDTO())
	}
}

func DeleteLesson(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		id, err := strconv.Atoi(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusBadRequest, NewWrongIdTypeError())
			return
		}

		lesson, err := s.Lessons().FindByID(uint(id))
		if err != nil {
			c.JSON(http.StatusInternalServerError, NewIdNotFoundError())
			return
		}

		userID, exists := c.Get("user_id")
		if !exists {
			c.JSON(http.StatusUnauthorized, NewUnauthorizedError())
			return
		}

		if lesson.UserID != userID.(uint) {
			c.JSON(http.StatusForbidden, NewNoPermissionsError())
			return
		}

		if lesson.Owner != model.LessonOwnerStudent {
			c.JSON(http.StatusForbidden, NewNoPermissionsError())
			return
		}

		err = s.Lessons().Delete(lesson.ID)
		if err != nil {
			logger.Errorf("api.DeleteLesson: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		c.JSON(http.StatusOK, NewLessonDeletedMessage())
	}
}

func BulkDeleteLessons(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req BulkDeleteRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, NewWrongJsonDataError())
			return
		}

		if len(req.IDs) == 0 {
			c.JSON(http.StatusBadRequest, NewNoIDsProvidedError())
			return
		}

		userID, exists := c.Get("user_id")
		if !exists {
			c.JSON(http.StatusUnauthorized, NewUnauthorizedError())
			return
		}

		lessons, err := s.Lessons().BulkFindByIDs(userID.(uint), req.IDs)
		if err != nil {
			logger.Errorf("api.BulkDeleteLessons: %v", err)
			c.JSON(http.StatusInternalServerError, NewDbError())
			return
		}

		for _, lesson := range lessons {
			if lesson.Owner != model.LessonOwnerStudent {
				c.JSON(http.StatusForbidden, NewNoPermissionsError())
			}

			if err := s.Lessons().Delete(lesson.ID); err != nil {
				logger.Errorf("api.BulkDeleteLessons: %v", err)
				c.JSON(http.StatusInternalServerError, NewDbError())
			}
		}

		c.JSON(http.StatusOK, NewLessonsDeletedMessage())
	}
}

func GetLearningRequirements(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			model.LessonOwnerTeacherEnglish.String():    setup.DefaultLessonSetupConfig().EnglishLessons,
			model.LessonOwnerTeacherSocial.String():     setup.DefaultLessonSetupConfig().SocialLessons,
			model.LessonOwnerTeacherLiterature.String(): setup.DefaultLessonSetupConfig().LiteratureLessons,
			model.LessonOwnerStudent.String():           setup.DefaultLessonSetupConfig().StudentLessons,
		})
	}
}

func StartLearning(s storage.Store) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, exists := c.Get("user_id")
		if !exists {
			c.JSON(http.StatusUnauthorized, NewUnauthorizedError())
			return
		}

		config := setup.DefaultLessonSetupConfig()

		eligible, err := setup.CheckStartLearningEligibility(s, userID.(uint), config)
		if err != nil {
			if !errors.Is(err, setup.ErrSyllabusCollision) && !errors.Is(err, setup.ErrSyllabusNotEnough) {
				logger.Errorf("api.StartLearning: %v", err)
			}
			c.JSON(http.StatusBadRequest, gin.H{
				"error": err.Error(),
			})
			return
		}

		if !eligible {
			c.JSON(http.StatusBadRequest, NewNotEligibleForLearningError())
			return
		}

		c.JSON(http.StatusOK, NewStartLearningSuccessMessage("tctf{/*REDACTED*/}"))
	}
}
