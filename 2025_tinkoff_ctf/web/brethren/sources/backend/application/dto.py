from dataclasses import dataclass


@dataclass(slots=True)
class NewCourseDTO:
    name: str
    description: str
    code: str
    is_active: bool


@dataclass(slots=True)
class NewUserDTO:
    username: str
    email: str
    hashed_password: str
    full_name: str
    is_active: bool
