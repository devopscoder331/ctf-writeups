from datetime import timedelta

from fastapi import APIRouter, HTTPException, status

from app.schemas import ProfileCreateSchema, ProfileLoginSchema, TokenSchema, ProfileIDSchema
from app.utils import db, create_access_token

router = APIRouter()

ACCESS_TOKEN_EXPIRE_MINUTES = 30

@router.post('/register', response_model=ProfileIDSchema, status_code=201)
async def register_profile(profile: ProfileCreateSchema):
    if db.check_exists(profile.username):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=[{
            "type": "forbidden",
            "msg": "Пользователь уже существует"
        }])

    profile_id = db.register_profile(profile)

    return ProfileIDSchema(profile_id=profile_id)

@router.post("/login", response_model=TokenSchema)
async def login_profile(profile: ProfileLoginSchema):
    profile_id = db.login_profile(profile)
    if not profile_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=[{
            "type": "unauthorized",
            "msg": "Неправильное имя пользователя или пароль"
        }])

    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(data={"profile_id": profile_id}, expires_delta=access_token_expires)

    return TokenSchema(access_token=access_token, profile_id=profile_id)
