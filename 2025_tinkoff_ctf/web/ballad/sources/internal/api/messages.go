package api

type Message struct {
	Message string `json:"message"`
}

// auth messages
func NewUserCreatedMessage() Message {
	return Message{
		Message: "Пользователь успешно создан",
	}
}

func NewLoginMessage() Message {
	return Message{
		Message: "Вы успешно вошли в систему",
	}
}

func NewLogoutMessage() Message {
	return Message{
		Message: "Вы успешно вышли из системы",
	}
}

// lesson messages
func NewLessonDeletedMessage() Message {
	return Message{
		Message: "Урок успешно удалён",
	}
}

func NewLessonsDeletedMessage() Message {
	return Message{
		Message: "Уроки успешно удалены",
	}
}

func NewStartLearningSuccessMessage(message string) Message {
	return Message{
		Message: message,
	}
}
