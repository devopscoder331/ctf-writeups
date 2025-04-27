from application import security
from application import interfaces
from application.dto import NewCourseDTO, NewUserDTO
from config import Config
from domain import entities
from domain.entities import UserDM


class GetCourseInteractor:
    def __init__(
        self,
        course_gateway: interfaces.CourseReader,
    ) -> None:
        self._course_gateway = course_gateway

    async def __call__(self, uuid: str) -> entities.CourseDM | None:
        return await self._course_gateway.read_by_uuid(uuid)


class GetAllCoursesInteractor:
    def __init__(
        self,
        course_gateway: interfaces.CourseReader,
    ) -> None:
        self._course_gateway = course_gateway

    async def __call__(self) -> list[entities.CourseDM] | None:
        return await self._course_gateway.read_all()


class NewCourseInteractor:
    def __init__(
        self,
        db_session: interfaces.DBSession,
        course_gateway: interfaces.CourseSaver,
        uuid_generator: interfaces.UUIDGenerator,
    ) -> None:
        self._db_session = db_session
        self._course_gateway = course_gateway
        self._uuid_generator = uuid_generator

    async def __call__(self, dto: NewCourseDTO) -> str:
        uuid = str(self._uuid_generator())
        course = entities.CourseDM(
            id=uuid,
            name=dto.name,
            description=dto.description,
            code=dto.code,
            is_active=dto.is_active,
        )

        await self._course_gateway.save(course)
        await self._db_session.commit()
        return uuid


class NewUserInteractor:
    def __init__(
        self,
        db_session: interfaces.DBSession,
        user_gateway: interfaces.UserSaver,
        uuid_generator: interfaces.UUIDGenerator,
    ) -> None:
        self._db_session = db_session
        self._user_gateway = user_gateway
        self._uuid_generator = uuid_generator

    async def __call__(self, dto: NewUserDTO) -> UserDM:
        uuid = str(self._uuid_generator())
        user = entities.UserDM(
            id=uuid,
            username=dto.username,
            email=dto.email,
            hashed_password=dto.hashed_password,
            full_name=dto.full_name,
            is_active=dto.is_active,
            is_admin=False,
        )

        await self._user_gateway.save(user)
        await self._db_session.commit()
        return user


class AuthenticateUserInteractor:
    def __init__(
        self,
        db_session: interfaces.DBSession,
        user_gateway: interfaces.UserReader,
        security_service: security.SecurityService,
    ) -> None:
        self._db_session = db_session
        self._user_gateway = user_gateway
        self._security_service = security_service

    async def __call__(self, username: str, password: str) -> str | None:
        user = await self._user_gateway.authenticate(username, password)
        if user:
            self._security_service.verify_password(password, user.hashed_password)
            return self._security_service.encode_jwt_token(user)


class GetrUserByUUIDInteractor:
    def __init__(
        self,
        db_session: interfaces.DBSession,
        user_gateway: interfaces.UserReader,
    ) -> None:
        self._db_session = db_session
        self._user_gateway = user_gateway

    async def __call__(self, uuid: str) -> UserDM | None:
        user = await self._user_gateway.read_user_by_id(uuid)
        if user:
            return user


class IsUserExistByEmailInteractor:
    def __init__(
        self,
        db_session: interfaces.DBSession,
        user_gateway: interfaces.UserReader,
    ) -> None:
        self._db_session = db_session
        self._user_gateway = user_gateway

    async def __call__(self, email: str) -> bool:
        is_exist_by_email = await self._user_gateway.read_user_by_email(email)
        if is_exist_by_email:
            return True
        return False


class IsUserExistByUsernameInteractor:
    def __init__(
        self,
        db_session: interfaces.DBSession,
        user_gateway: interfaces.UserReader,
    ) -> None:
        self._db_session = db_session
        self._user_gateway = user_gateway

    async def __call__(self, username: str) -> bool:
        is_exist_by_username = await self._user_gateway.read_user_by_username(username)
        if is_exist_by_username:
            return True
        return False


class DeactivateUserByUUIDInteractor:
    def __init__(
            self,
            db_session: interfaces.DBSession,
            user_gateway: interfaces.UserDeactivator,
    ) -> None:
        self._db_session = db_session
        self._user_gateway = user_gateway

    async def __call__(self, uuid: str) -> None:
        await self._user_gateway.deactivate_user_by_id(uuid)



class GetTicketInteractor:
    def __init__(
        self,
        ticket_gateway: interfaces.TicketReader,
        config: Config,
    ) -> None:
        self._ticket_gateway = ticket_gateway
        self._config = config

    async def __call__(self, course_id: str, ticket_id: str) -> str | None:
        return await self._ticket_gateway.read_ticket_file(
            self._config.tickets_dir, course_id, ticket_id
        )


class GetCourseTicketInteractor:
    def __init__(
        self,
        ticket_gateway: interfaces.TicketReader,
        config: Config,
    ) -> None:
        self._ticket_gateway = ticket_gateway
        self._config = config

    async def __call__(self, course_id: str) -> str | None:
        return await self._ticket_gateway.read_course_tickets(
            self._config.tickets_dir, course_id
        )


class RandomCourseTicketInteractor:
    def __init__(
        self,
        ticket_gateway: interfaces.TicketReader,
        config: Config,
    ) -> None:
        self._ticket_gateway = ticket_gateway
        self._config = config

    async def __call__(self, course_id: str) -> str | None:
        return await self._ticket_gateway.random_course_ticket(
            self._config.tickets_dir, course_id
        )
