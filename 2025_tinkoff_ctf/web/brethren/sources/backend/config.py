from os import environ as env

from pydantic import Field, BaseModel


class PostgresConfig(BaseModel):
    host: str = Field(alias="POSTGRES_HOST")
    port: int = Field(alias="POSTGRES_PORT")
    login: str = Field(alias="POSTGRES_USER")
    password: str = Field(alias="POSTGRES_PASSWORD")
    database: str = Field(alias="POSTGRES_DB")


class Config(BaseModel):
    postgres: PostgresConfig = Field(default_factory=lambda: PostgresConfig(**env))
    secret_key: str = env.get("SECRET_KEY") or "FOU*MDAYF79ydn7gasf&A*BN&SF&TNASIg"
    tickets_dir: str = "tickets"
