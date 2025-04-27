from datetime import datetime, timedelta
from typing import Optional
from uuid import UUID

from jose import JWTError, jwt
from pydantic import BaseModel
from passlib.context import CryptContext

from litestar.exceptions import NotAuthorizedException


from domain.entities import UserDM

DEFAULT_TIME_DELTA = timedelta(days=1)
ALGORITHM = "HS256"

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: UUID
    username: str
    is_admin: bool


class TokenData(BaseModel):
    user_id: UUID
    username: str
    is_admin: bool


class SecurityService:
    def __init__(self, secret_key: str, algorithm: str = "HS256"):
        self.secret_key = secret_key
        self.algorithm = algorithm

    def verify_password(self, plain_password: str, hashed_password: str) -> bool:
        return pwd_context.verify(plain_password, hashed_password)

    def get_password_hash(self, password: str) -> str:
        return pwd_context.hash(password)

    def encode_jwt_token(
        self, user: UserDM, expiration: timedelta = DEFAULT_TIME_DELTA
    ) -> str:
        payload = {
            "exp": datetime.now() + expiration,
            "iat": datetime.now(),
            "user_id": str(user.id),
            "username": user.username,
            "is_admin": user.is_admin,
        }
        token = jwt.encode(payload, self.secret_key, algorithm=self.algorithm)
        return token

    def verify_token(self, token: str) -> Optional[TokenData]:
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=[self.algorithm])
            user_id = UUID(payload.get("user_id"))
            username = payload.get("username")
            is_admin = payload.get("is_admin", False)

            if user_id is None or username is None:
                return None

            return TokenData(user_id=user_id, username=username, is_admin=is_admin)
        except JWTError:
            raise NotAuthorizedException("Invalid token")
