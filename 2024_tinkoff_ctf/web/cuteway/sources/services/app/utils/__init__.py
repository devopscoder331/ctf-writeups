import re
import hashlib
from typing import Optional
from datetime import datetime, timedelta, UTC

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from passlib.context import CryptContext
from jose import JWTError, jwt

from app.config import settings
from app.schemas import ProfileCreateSchema, ProfileLoginSchema, ProfileDataSchema

ALGORITHM = "HS256"

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="api/login")

class Database:
    def __init__(self, file_path):
        self.file_path = file_path

        with open(self.file_path, 'a') as file:
            pass

        if not self.check_exists('prepRod1970'):
            with open(self.file_path, 'a') as file:
                file.write("Антон:Александрович:prepRod1970:<bcrypt-hash-REDACTED>:e44341f22af741240fea9c98277e1430:Мужчина в полном рассвете сил. ПрепРодаватель. Продам диплом на любую тему. Недорого. Ваш промокод на первую покупку: tctf{REDACTED}\n")

    def register_profile(self, profile: ProfileCreateSchema) -> str:
        hashed_password = pwd_context.hash(profile.password)
        profile_id = hashlib.md5(profile.username.encode()).hexdigest()

        with open(self.file_path, 'a') as file:
            file.write(f"{profile.first_name}:{profile.second_name}:{profile.username}:{hashed_password}:{profile_id}:{profile.descriptions or ''}\n")

        return profile_id

    def login_profile(self, profile: ProfileLoginSchema) -> Optional[str]:
        with open(self.file_path, 'r') as file:
            for line in file.read().splitlines():
                match = re.search(rf"^.*?:.*?:{profile.username}:(.*?):(.*?):.*?$", line)

                if match:
                    hashed_password, profile_id = match.groups()

                    try:
                        if pwd_context.verify(profile.password, hashed_password):
                            return profile_id
                    except:
                        return None
        return None
    
    def check_exists(self, username: str) -> bool:
        with open(self.file_path, 'r') as file:
            for line in file.read().splitlines():
                match = re.match(rf"^.*?:.*?:{username}:.*?:.*?:.*?$", line)
                if match: return True
            return False

    def get_profile_data(self, profile_id: str) -> Optional[ProfileDataSchema]:
        with open(self.file_path, 'r') as file:
            for line in file.read().splitlines():
                match = re.search(rf"^(.*?):(.*?):.*?:.*?:{profile_id}:(.*?)$", line)

                if match:
                    first_name, second_name, descriptions =  match.groups()

                    return ProfileDataSchema(first_name=first_name, second_name=second_name, descriptions=descriptions, profile_id=profile_id)
        return None

db = Database(settings.DATABASE_FILE)

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.now(UTC) + expires_delta
    else:
        expire = datetime.now(UTC) + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

async def validate_token(token: str = Depends(oauth2_scheme)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[ALGORITHM])
        profile_id: str = payload.get("profile_id")
        if profile_id is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    profile = db.get_profile_data(profile_id)
    if not profile:
        raise credentials_exception
    return profile
