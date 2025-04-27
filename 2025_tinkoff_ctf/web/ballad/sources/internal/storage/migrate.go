package storage

import (
	"fmt"
)

func MigrateTables(s Store) error {
	if err := s.Lessons().Migrate(); err != nil {
		return fmt.Errorf("failed to migrate lessons: %v", err)
	}

	if err := s.Users().Migrate(); err != nil {
		return fmt.Errorf("failed to migrate users: %v", err)
	}

	return nil
}
