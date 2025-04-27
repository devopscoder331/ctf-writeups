package model

type LessonOwner string

const (
	LessonOwnerTeacherEnglish    LessonOwner = "teacher_english"
	LessonOwnerTeacherSocial     LessonOwner = "teacher_social_studies"
	LessonOwnerTeacherLiterature LessonOwner = "teacher_literature"
	LessonOwnerStudent           LessonOwner = "student"
)

func (l LessonOwner) String() string {
	return string(l)
}
