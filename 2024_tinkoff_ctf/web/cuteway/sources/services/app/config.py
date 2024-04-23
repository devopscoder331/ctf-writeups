import os

from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    BASE_DIR: str = os.path.abspath(os.path.dirname(__file__))

    DATABASE_FILE: str = os.getenv("DATABASE_FILE", "database/profiles.txt")

    SECRET_KEY: bytes = os.getenv("SECRET_KEY", os.urandom(10))

    RECAPTCHA_SECRET_KEY: str = os.getenv("RECAPTCHA_SECRET_KEY")

settings = Settings()
