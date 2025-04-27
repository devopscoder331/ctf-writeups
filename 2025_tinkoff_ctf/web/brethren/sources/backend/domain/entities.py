from dataclasses import dataclass


@dataclass(slots=True)
class UserDM:
    id: str
    username: str
    email: str
    hashed_password: str
    full_name: str
    is_active: bool
    is_admin: bool


@dataclass(slots=True)
class CourseDM:
    id: str
    name: str
    description: str
    code: str
    is_active: bool


@dataclass(slots=True)
class TicketDM:
    id: str
    course_id: str
    title: str
    file_path: str


@dataclass(slots=True)
class UserCourseDM:
    user_id: str
    course_id: str
