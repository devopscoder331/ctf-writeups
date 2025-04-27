package storage

import (
	"log"
	"os"

	"syllabus/internal/storage/lesson"
	"syllabus/internal/storage/user"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

type Database struct {
	db *gorm.DB

	lessons LessonRepository
	users   UserRepository
}

func DBConn(dsn string) (*gorm.DB, error) {
	gormLogger := logger.New(
		log.New(os.Stdout, "\r\n", log.LstdFlags),
		logger.Config{
			IgnoreRecordNotFoundError: true,
		},
	)

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{
		Logger: gormLogger,
	})
	if err != nil {
		return db, err
	}

	return db, err
}

func New(db *gorm.DB) *Database {
	return &Database{
		db:      db,
		lessons: lesson.NewRepository(db),
		users:   user.NewRepository(db),
	}
}

func (db *Database) Lessons() LessonRepository {
	return db.lessons
}

func (db *Database) Users() UserRepository {
	return db.users
}

func (db *Database) Ping() error {
	sqlDB, err := db.db.DB()
	if err != nil {
		return err
	}

	return sqlDB.Ping()
}
