package storage

type Store interface {
	Lessons() LessonRepository
	Users() UserRepository
	Ping() error
}
