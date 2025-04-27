from dishka import make_async_container
from dishka.integrations import litestar as litestar_integration
from litestar import Litestar
from litestar.config.cors import CORSConfig
from litestar.connection import ASGIConnection
from litestar.exceptions import NotAuthorizedException
from litestar.middleware import (
    DefineMiddleware,
    AbstractAuthenticationMiddleware,
    AuthenticationResult,
)
from litestar.openapi import OpenAPIConfig
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker

from application.security import SecurityService
from config import Config
from controllers.http import (
    HTTPCourseController,
    HTTPAuthController,
    HTTPTicketController,
    HTTPUserController, HTTPFlagController,
)
from infrastructure.models import User
from ioc import AppProvider

config = Config()
container = make_async_container(AppProvider(), context={Config: config})


class JWTAuthenticationMiddleware(AbstractAuthenticationMiddleware):
    async def authenticate_request(
        self, connection: ASGIConnection
    ) -> AuthenticationResult:
        auth_header = connection.headers.get("X-API-KEY")
        if not auth_header:
            raise NotAuthorizedException()

        security_service = await container.get(SecurityService)
        token = security_service.verify_token(auth_header)

        async_session = await container.get(async_sessionmaker[AsyncSession])
        async with async_session() as session:
            async with session.begin():
                result = await session.execute(
                    select(User).where(User.id == token.user_id).where(User.is_active == True)
                )
                user = result.scalar()
        if not user:
            raise NotAuthorizedException()

        return AuthenticationResult(user=user, auth=token)


def get_litestar_app() -> Litestar:
    auth_mw = DefineMiddleware(
        JWTAuthenticationMiddleware, exclude=["/auth", "/schema"]
    )
    litestar_app = Litestar(
        route_handlers=[
            HTTPCourseController,
            HTTPAuthController,
            HTTPTicketController,
            HTTPUserController,
            HTTPFlagController,
        ],
        middleware=[auth_mw],
        cors_config=CORSConfig(),
        openapi_config=OpenAPIConfig(title="Tickets API", version="1.0.0"),
    )
    litestar_integration.setup_dishka(container, litestar_app)
    return litestar_app


def get_app():
    litestar_app = get_litestar_app()
    return litestar_app
