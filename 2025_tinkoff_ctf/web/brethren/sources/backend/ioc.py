from typing import AsyncIterable
from uuid import uuid4

from dishka import Provider, Scope, provide, AnyOf, from_context
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker

from application import interfaces
from application.interactors import (
    GetCourseInteractor,
    NewCourseInteractor,
    NewUserInteractor,
    IsUserExistByEmailInteractor,
    IsUserExistByUsernameInteractor,
    AuthenticateUserInteractor,
    GetrUserByUUIDInteractor,
    GetTicketInteractor,
    GetAllCoursesInteractor,
    GetCourseTicketInteractor,
    RandomCourseTicketInteractor, DeactivateUserByUUIDInteractor,
)
from application.security import SecurityService
from config import Config
from infrastructure.database import new_session_maker
from infrastructure.gateways import CourseGateway, UserGateway, TicketGateway


class AppProvider(Provider):
    config = from_context(provides=Config, scope=Scope.APP)

    @provide(scope=Scope.APP)
    def get_uuid_generator(self) -> interfaces.UUIDGenerator:
        return uuid4

    @provide(scope=Scope.APP)
    def get_session_maker(self, config: Config) -> async_sessionmaker[AsyncSession]:
        return new_session_maker(config.postgres)

    @provide(scope=Scope.REQUEST)
    async def get_session(
        self, session_maker: async_sessionmaker[AsyncSession]
    ) -> AsyncIterable[
        AnyOf[
            AsyncSession,
            interfaces.DBSession,
        ]
    ]:
        async with session_maker() as session:
            yield session

    @provide(scope=Scope.APP)
    def get_security_service(self, config: Config) -> SecurityService:
        return SecurityService(secret_key=config.secret_key)

    course_gateway = provide(
        CourseGateway,
        scope=Scope.REQUEST,
        provides=AnyOf[interfaces.CourseReader, interfaces.CourseSaver],
    )

    user_gateway = provide(
        UserGateway,
        scope=Scope.REQUEST,
        provides=AnyOf[interfaces.UserReader, interfaces.UserSaver, interfaces.UserDeactivator],
    )

    ticket_gateway = provide(
        TicketGateway, scope=Scope.REQUEST, provides=interfaces.TicketReader
    )

    get_course_interactor = provide(GetCourseInteractor, scope=Scope.REQUEST)
    create_new_course_interactor = provide(NewCourseInteractor, scope=Scope.REQUEST)
    new_user_interactor = provide(NewUserInteractor, scope=Scope.REQUEST)
    is_user_exists_by_email_interactor = provide(
        IsUserExistByEmailInteractor, scope=Scope.REQUEST
    )
    is_user_exists_by_username_interactor = provide(
        IsUserExistByUsernameInteractor, scope=Scope.REQUEST
    )
    authenticate_user_interactor = provide(
        AuthenticateUserInteractor, scope=Scope.REQUEST
    )
    get_user_by_uuid_interactor = provide(GetrUserByUUIDInteractor, scope=Scope.REQUEST)
    get_ticket_interactor = provide(GetTicketInteractor, scope=Scope.REQUEST)
    get_course_ticket_interactor = provide(
        GetCourseTicketInteractor, scope=Scope.REQUEST
    )
    get_all_courses_interactor = provide(GetAllCoursesInteractor, scope=Scope.REQUEST)
    enroll_course_ticket_interactor = provide(
        RandomCourseTicketInteractor, scope=Scope.REQUEST
    )
    deactivate_user_by_uuid_interactor = provide(
        DeactivateUserByUUIDInteractor, scope=Scope.REQUEST
    )
