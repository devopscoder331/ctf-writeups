package api

type ApiError struct {
	Message string `json:"error"`
}

func (e ApiError) Error() string {
	return e.Message
}

// Common errors
func NewDbError() ApiError {
	return ApiError{Message: "Ошибка базы данных"}
}

func NewIdNotFoundError() ApiError {
	return ApiError{Message: "ID не найден"}
}

func NewWrongIdTypeError() ApiError {
	return ApiError{Message: "Неверный тип ID, требуется целое число"}
}

func NewWrongJsonDataError() ApiError {
	return ApiError{Message: "Неправильные данные JSON"}
}

// Auth errors
func NewUsernameAlreadyExistsError() ApiError {
	return ApiError{Message: "Пользователь с таким именем уже зарегистрирован. Пожалуйста, выберите другое имя."}
}

func NewEmptyCredentialsError() ApiError {
	return ApiError{Message: "Требуются имя пользователя и пароль"}
}

func NewShortCredentialsError() ApiError {
	return ApiError{Message: "Имя пользователя и пароль должны быть не менее 9 символов"}
}

func NewUserNotFoundError() ApiError {
	return ApiError{Message: "Пользователь не найден"}
}

func NewWrongCredentialsError() ApiError {
	return ApiError{Message: "Неверные учетные данные"}
}

func NewUnauthorizedError() ApiError {
	return ApiError{Message: "Пользователь не авторизован"}
}

func NewNoPermissionsError() ApiError {
	return ApiError{Message: "У вас нет прав для выполнения этого действия"}
}

// Lesson errors
func NewNoIDsProvidedError() ApiError {
	return ApiError{Message: "Не предоставлены ID уроков"}
}

func NewNotEligibleForLearningError() ApiError {
	return ApiError{Message: "Вы не можете начать обучение. Убедитесь, что у вас есть необходимое количество уроков для каждого типа преподавателя и отсутствуют конфликты позиций."}
}

func NewPositionCollisionError() ApiError {
	return ApiError{Message: "Позиция уже занята. Пожалуйста, выберите другую позицию."}
}
