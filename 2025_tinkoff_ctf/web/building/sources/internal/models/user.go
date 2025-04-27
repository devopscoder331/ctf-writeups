package models

import (
	"time"
)

type Role int

const (
	Administrator Role = 1
	Renter        Role = 2
	Manager       Role = 3
	RegularUser   Role = 4
)

type User struct {
	ID           int64     `json:"id"`
	Username     string    `json:"username"`
	PasswordHash string    `json:"-"`
	RoleID       Role      `json:"role_id"`
	RoomID       *int64    `json:"room_id,omitempty"`
	CardID       *int64    `json:"card_id,omitempty"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}