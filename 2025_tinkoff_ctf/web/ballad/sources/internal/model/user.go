package model

import (
	"time"

	"gorm.io/gorm"
)

type User struct {
	ID        uint `gorm:"primary_key"`
	CreatedAt time.Time
	UpdatedAt time.Time
	DeletedAt gorm.DeletedAt `gorm:"index"`

	Username string `gorm:"unique"`
	Password string

	Lessons []*Lesson
}

type UserDTO struct {
	ID       uint   `json:"id"`
	Username string `json:"username"`
}

func (u *User) ToDTO() *UserDTO {
	return &UserDTO{
		ID:       u.ID,
		Username: u.Username,
	}
}
