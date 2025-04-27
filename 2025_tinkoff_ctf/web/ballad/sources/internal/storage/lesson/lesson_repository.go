package lesson

import (
	"syllabus/internal/model"

	"gorm.io/gorm"
)

type Repository struct {
	db *gorm.DB
}

func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

func (p *Repository) AllByUserID(userID uint) ([]*model.Lesson, error) {
	lessons := []*model.Lesson{}
	if err := p.db.Where("user_id = ?", userID).Find(&lessons).Error; err != nil {
		return nil, err
	}

	return lessons, nil
}

func (p *Repository) BulkFindByIDs(userID uint, ids []uint) ([]*model.Lesson, error) {
	if len(ids) == 0 {
		return []*model.Lesson{}, nil
	}

	lessons := []*model.Lesson{}
	if err := p.db.Where("id IN ?", ids).Where("user_id = ?", userID).Find(&lessons).Error; err != nil {
		return nil, err
	}

	return lessons, nil
}

func (p *Repository) FindByID(id uint) (*model.Lesson, error) {
	lesson := new(model.Lesson)
	if err := p.db.Where("id = ?", id).First(&lesson).Error; err != nil {
		return nil, err
	}

	return lesson, nil
}

func (p *Repository) Create(lesson *model.Lesson) (*model.Lesson, error) {
	if err := p.db.Create(&lesson).Error; err != nil {
		return nil, err
	}

	return lesson, nil
}

func (p *Repository) Update(id uint, lesson *model.Lesson) (*model.Lesson, error) {
	lesson.ID = id
	if err := p.db.Model(&model.Lesson{}).Where("id = ?", id).Updates(&lesson).Error; err != nil {
		return nil, err
	}

	return lesson, nil
}

func (p *Repository) Delete(id uint) error {
	if err := p.db.Delete(&model.Lesson{ID: id}).Error; err != nil {
		return err
	}

	return nil
}

func (p *Repository) Migrate() error {
	return p.db.AutoMigrate(&model.Lesson{})
}
