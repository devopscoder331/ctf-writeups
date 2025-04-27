from pydantic import BaseModel, EmailStr


class CourseSchema(BaseModel):
    id: str
    name: str
    description: str
    code: str
    is_active: bool


class CourseTicketSchema(BaseModel):
    tickets: list
    course: CourseSchema


class TicketSchema(BaseModel):
    text: str


class UserAuthRequestSchema(BaseModel):
    username: str
    password: str


class TokenSchema(BaseModel):
    token: str


class TokenDataSchema(BaseModel):
    user_id: str
    username: str
    is_admin: bool


class UserRegisterRequestSchema(BaseModel):
    username: str
    email: EmailStr
    password: str
    full_name: str | None = None


class UserSchema(BaseModel):
    username: str
    email: EmailStr
    full_name: str | None
    is_active: bool
