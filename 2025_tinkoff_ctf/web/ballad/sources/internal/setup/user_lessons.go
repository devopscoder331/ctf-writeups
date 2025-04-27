package setup

import (
	"errors"

	"syllabus/internal/model"
	"syllabus/internal/storage"
)

var (
	ErrSyllabusCollision = errors.New("Обнаружено пересечение уроков в учебном плане")
	ErrSyllabusNotEnough = errors.New("Недостаточно уроков для начала обучения")
)

type LessonSetupConfig struct {
	EnglishLessons    int
	SocialLessons     int
	LiteratureLessons int
	StudentLessons    int
}

func DefaultLessonSetupConfig() LessonSetupConfig {
	return LessonSetupConfig{
		EnglishLessons:    3,
		SocialLessons:     3,
		LiteratureLessons: 3,
		StudentLessons:    5,
	}
}

func CheckStartLearningEligibility(s storage.Store, userID uint, config LessonSetupConfig) (bool, error) {
	lessons, err := s.Lessons().AllByUserID(userID)
	if err != nil {
		return false, err
	}

	englishCount := 0
	socialCount := 0
	literatureCount := 0
	studentCount := 0

	positionMap := make(map[int]bool)

	for _, lesson := range lessons {
		if positionMap[lesson.Position] {
			return false, ErrSyllabusCollision
		}
		positionMap[lesson.Position] = true

		switch lesson.Owner {
		case model.LessonOwnerTeacherEnglish:
			englishCount++
		case model.LessonOwnerTeacherSocial:
			socialCount++
		case model.LessonOwnerTeacherLiterature:
			literatureCount++
		case model.LessonOwnerStudent:
			studentCount++
		}
	}

	if englishCount < config.EnglishLessons ||
		socialCount < config.SocialLessons ||
		literatureCount < config.LiteratureLessons ||
		studentCount < config.StudentLessons {
		return false, ErrSyllabusNotEnough
	}

	return true, nil
}

func CheckPositionAvailable(s storage.Store, userID uint, position int, excludeLessonID uint) (bool, error) {
	lessons, err := s.Lessons().AllByUserID(userID)
	if err != nil {
		return false, err
	}

	for _, lesson := range lessons {
		if lesson.ID == excludeLessonID {
			continue
		}

		if lesson.Position == position {
			return false, nil
		}
	}

	return true, nil
}
