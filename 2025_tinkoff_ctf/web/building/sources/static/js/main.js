function getRoleName(roleId) {
    switch (roleId) {
        case 1: return 'Администратор';
        case 2: return 'Арендатор';
        case 3: return 'Менеджер';
        case 4: return 'Пользователь';
        default: return 'Неизвестно';
    }
}