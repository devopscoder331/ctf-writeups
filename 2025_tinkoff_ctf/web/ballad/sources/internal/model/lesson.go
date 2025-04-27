package model

import (
	"time"

	"gorm.io/gorm"
)

type Lesson struct {
	ID        uint `gorm:"primary_key"`
	CreatedAt time.Time
	UpdatedAt time.Time
	DeletedAt gorm.DeletedAt `gorm:"index"`

	Name        string
	Description string
	Position    int `gorm:"check:position >= 0 AND position <= 15"`
	Owner       LessonOwner

	UserID uint
}

type LessonDTO struct {
	ID          uint   `json:"id"`
	Name        string `json:"name"`
	Description string `json:"description"`
	Position    int    `json:"position"`
	Owner       string `json:"owner"`
}

func (l *Lesson) ToDTO() *LessonDTO {
	return &LessonDTO{
		ID:          l.ID,
		Name:        l.Name,
		Description: l.Description,
		Position:    l.Position,
		Owner:       l.Owner.String(),
	}
}

func (l *LessonDTO) ToModel() *Lesson {
	return &Lesson{
		Name:        l.Name,
		Description: l.Description,
		Position:    l.Position,
	}
}

func ToLessonModelList(lessonDTOs []*LessonDTO) []*Lesson {
	lessons := make([]*Lesson, len(lessonDTOs))

	for i, lessonDTO := range lessonDTOs {
		lessons[i] = lessonDTO.ToModel()
	}

	return lessons
}

func ToLessonDTOList(lessons []*Lesson) []*LessonDTO {
	lessonDTOs := make([]*LessonDTO, len(lessons))

	for i, lesson := range lessons {
		lessonDTOs[i] = lesson.ToDTO()
	}

	return lessonDTOs
}
