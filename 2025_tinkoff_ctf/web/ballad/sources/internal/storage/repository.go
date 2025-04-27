package storage

import "syllabus/internal/model"

type LessonRepository interface {
	AllByUserID(id uint) ([]*model.Lesson, error)
	BulkFindByIDs(userID uint, ids []uint) ([]*model.Lesson, error)
	FindByID(id uint) (*model.Lesson, error)
	Create(lesson *model.Lesson) (*model.Lesson, error)
	Update(id uint, lesson *model.Lesson) (*model.Lesson, error)
	Delete(id uint) error
	Migrate() error
}

type UserRepository interface {
	Create(username, password string) (*model.User, error)
	FindByID(id uint) (*model.User, error)
	FindByUsername(username string) (*model.User, error)
	Delete(id uint) error
	Migrate() error
}
