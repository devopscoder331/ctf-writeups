package user

import (
	"syllabus/internal/model"
	"syllabus/internal/utils"

	"gorm.io/gorm"
)

type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

func (p *Repository) Create(username, password string) (*model.User, error) {
	user := &model.User{
		Username: username,
		Password: utils.HashPassword(password),
	}

	if err := p.db.Create(&user).Error; err != nil {
		return nil, err
	}

	return user, nil
}

func (p *Repository) FindByID(id uint) (*model.User, error) {
	user := new(model.User)
	if err := p.db.Where("id = ?", id).First(&user).Error; err != nil {
		return nil, err
	}

	return user, nil
}

func (p *Repository) FindByUsername(username string) (*model.User, error) {
	user := new(model.User)
	if err := p.db.Where("username = ?", username).First(&user).Error; err != nil {
		return nil, err
	}

	return user, nil
}

func (p *Repository) Delete(id uint) error {
	if err := p.db.Where("user_id = ?", id).Delete(&model.Lesson{}).Error; err != nil {
		return err
	}

	return p.db.Delete(&model.User{}, id).Error
}

func (p *Repository) Migrate() error {
	return p.db.AutoMigrate(&model.User{})
}
