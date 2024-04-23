import re
from typing import Optional

import requests
from pydantic import BaseModel, field_validator
from pydantic_core import PydanticCustomError

from app.config import settings

class ProfileLoginSchema(BaseModel):
    username: str
    password: str

    @field_validator('username')
    def validate_username(cls, v):
        pattern = re.compile(r"^\w+$")
        if not pattern.match(v):
            raise PydanticCustomError(
                "string_pattern_mismatch",
                "Поле должно соответствовать шаблону '{pattern}'",
                dict(pattern=pattern.pattern))
        return v
    
    @field_validator('username', 'password')
    def validate_length(cls, v):
        if len(v) < 8:
            raise PydanticCustomError(
                "string_too_short",
                "Поле должно содержать более чем 8 символов")
        return v

class ProfileCreateSchema(ProfileLoginSchema):
    first_name: str
    second_name: str
    g_recaptcha_response: str
    descriptions: Optional[str] = ""

    @field_validator('g_recaptcha_response')
    def validate_recaptcha(cls, v):
        payload = {
            'secret': settings.RECAPTCHA_SECRET_KEY,
            'response': v
        }
        response = requests.post('https://www.google.com/recaptcha/api/siteverify', data=payload)
        result = response.json()

        if not result.get('success'):
            raise PydanticCustomError(
                "recaptcha_error",
                "Не удалось решить reCAPTCHA")
        return v

    @field_validator('first_name', 'second_name')
    def validate_names(cls, v):
        pattern = re.compile(r"^[ёЁА-яa-zA-Z ]+$")
        if not pattern.match(v):
            raise PydanticCustomError(
                "string_pattern_mismatch",
                "Поле должно соответствовать шаблону '{pattern}'",
                dict(pattern=pattern.pattern))
        return v
    
    @field_validator('descriptions')
    def validate_descriptions(cls, v):
        pattern = re.compile(r"^[\S ]*$")
        if not pattern.match(v):
            raise PydanticCustomError(
                "string_pattern_mismatch",
                "Поле должно соответствовать шаблону '{pattern}'",
                dict(pattern=pattern.pattern))
        return v
    
    @field_validator('first_name', 'second_name', 'descriptions', 'username', 'password')
    def validate_oversize(cls, v):
        if len(v) > 256:
            raise PydanticCustomError(
                "string_too_long",
                "Поле должно содержать менее чем 256 символов")
        return v

class TokenSchema(BaseModel):
    access_token: str
    profile_id: str

class ProfileIDSchema(BaseModel):
    profile_id: str

class ProfileDataSchema(BaseModel):
    first_name: str
    second_name: str
    profile_id: str
    descriptions: Optional[str]
