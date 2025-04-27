package models

import (
	"time"
)

type RoomStatus int

// TODO: handle status everywhere
const (
	Available RoomStatus = iota
	Occupied
	Maintenance
)

type Room struct {
	ID         int64      `json:"id"`
	RoomNumber string     `json:"room_number"`
	Status     RoomStatus `json:"status"`
	RenterID   *int64     `json:"renter_id,omitempty"`
	CreatedAt  time.Time  `json:"created_at"`
	UpdatedAt  time.Time  `json:"updated_at"`
}

type RoomAssignment struct {
	ID        int64      `json:"id"`
	RoomID    int64      `json:"room_id"`
	RenterID  int64      `json:"renter_id"`
	StartDate time.Time  `json:"start_date"`
	EndDate   *time.Time `json:"end_date,omitempty"`
	CreatedAt time.Time  `json:"created_at"`
}
