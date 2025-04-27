import re
from typing import Annotated
from uuid import UUID
from http import HTTPStatus

from dishka.integrations.base import FromDishka as Depends
from dishka.integrations.litestar import inject
from litestar import Controller, route, HttpMethod, status_codes
from litestar.exceptions import HTTPException
from litestar.params import Body, Parameter

from application.dto import NewUserDTO
from application.interactors import (
    GetCourseInteractor,
    NewUserInteractor,
    IsUserExistByEmailInteractor,
    IsUserExistByUsernameInteractor,
    AuthenticateUserInteractor,
    GetTicketInteractor,
    GetAllCoursesInteractor,
    GetCourseTicketInteractor,
    RandomCourseTicketInteractor,
)
from application.security import SecurityService
from controllers.schemas import (
    CourseSchema,
    UserRegisterRequestSchema,
    UserSchema,
    UserAuthRequestSchema,
    TokenSchema,
    TicketSchema,
    CourseTicketSchema,
    TokenDataSchema,
)
from infrastructure.models import User

from application.interactors import DeactivateUserByUUIDInteractor


class HTTPCourseController(Controller):
    path = "/course"

    @route(http_method=HttpMethod.GET)
    @inject
    async def get_all_courses(
        self,
        interactor: Depends[GetAllCoursesInteractor],
    ) -> list[CourseSchema]:
        course_dm_list = await interactor()
        if not course_dm_list:
            return []
        return [
            CourseSchema(
                id=str(course_dm.id),
                name=course_dm.name,
                description=course_dm.description,
                code=course_dm.code,
                is_active=course_dm.is_active,
            )
            for course_dm in course_dm_list
        ]

    @route(http_method=HttpMethod.GET, path="/{course_id:uuid}")
    @inject
    async def get_course(
        self,
        course_id: Annotated[UUID, Body(description="Course ID", title="Course ID")],
        interactor: Depends[GetCourseInteractor],
    ) -> CourseSchema:
        course_dm = await interactor(uuid=str(course_id))
        if not course_dm:
            raise HTTPException(
                status_code=HTTPStatus.NOT_FOUND, detail="Course not found"
            )
        return CourseSchema(
            id=str(course_dm.id),
            name=course_dm.name,
            description=course_dm.description,
            code=course_dm.code,
            is_active=course_dm.is_active,
        )

    @route(http_method=HttpMethod.GET, path="/{course_id:uuid}/tickets")
    @inject
    async def get_course_tickets(
        self,
        course_id: Annotated[UUID, Body(description="Course ID", title="Course ID")],
        get_course_tickets_interactor: Depends[GetCourseTicketInteractor],
        get_course_interactor: Depends[GetCourseInteractor],
    ) -> CourseTicketSchema:
        course_dm = await get_course_interactor(uuid=str(course_id))
        tickets = await get_course_tickets_interactor(course_id=str(course_id))
        if not tickets:
            raise HTTPException(
                status_code=HTTPStatus.NOT_FOUND, detail="Tickets not found"
            )
        return CourseTicketSchema(
            tickets=tickets,
            course=CourseSchema(
                id=str(course_dm.id),
                name=course_dm.name,
                description=course_dm.description,
                code=course_dm.code,
                is_active=course_dm.is_active,
            ),
        )

    @route(http_method=HttpMethod.GET, path="/{course_id:uuid}/roll")
    @inject
    async def random_course_ticket(
        self,
        course_id: Annotated[UUID, Body(description="Course ID", title="Course ID")],
        roll_interactor: Depends[RandomCourseTicketInteractor],
    ) -> TicketSchema:
        ticket = await roll_interactor(course_id=str(course_id))
        if not ticket:
            raise HTTPException(
                status_code=HTTPStatus.NOT_FOUND, detail="Tickets not found"
            )
        return TicketSchema(text=ticket)


class HTTPTicketController(Controller):
    path = "/ticket"

    @route(http_method=HttpMethod.GET)
    @inject
    async def get_ticket(
        self,
        course_id: Annotated[str, Body(description="Course ID", title="Course ID")],
        ticket_id: Annotated[str, Body(description="Ticket ID", title="Ticket ID")],
        get_ticket_interactor: Depends[GetTicketInteractor],
    ) -> TicketSchema:
        try:
            ticket = await get_ticket_interactor(
                course_id=str(course_id), ticket_id=str(ticket_id)
            )
            if not ticket:
                raise HTTPException(
                    status_code=HTTPStatus.NOT_FOUND, detail="Ticket not found"
                )

            return TicketSchema(text=ticket)
        except (FileNotFoundError, PermissionError):
            raise HTTPException(
                status_code=HTTPStatus.NOT_FOUND, detail="Ticket not found"
            )


class HTTPAuthController(Controller):
    path = "/auth"

    @route(http_method=HttpMethod.POST, path="/register")
    @inject
    async def register_user(
        self,
        data: Annotated[UserRegisterRequestSchema, Body()],
        security_service: Depends[SecurityService],
        new_user_interactor: Depends[NewUserInteractor],
        user_exist_by_email_interactor: Depends[IsUserExistByEmailInteractor],
        user_exist_by_username_interactor: Depends[IsUserExistByUsernameInteractor],
    ) -> UserSchema:
        if (
            len(data.password) < 8
            or not re.search(r"\d", data.password)
            or not re.search(r"[A-Za-z]", data.password)
        ):
            raise HTTPException(
                status_code=HTTPStatus.BAD_REQUEST,
                detail="Password is too weak. It must be at least 8 characters long and contain chars and at least one digit",
            )

        existing_user_by_email = await user_exist_by_email_interactor(data.email)
        existing_user_by_username = await user_exist_by_username_interactor(
            data.username
        )

        if existing_user_by_email:
            raise HTTPException(
                status_code=status_codes.HTTP_400_BAD_REQUEST,
                detail="User with this email already exists",
            )

        if existing_user_by_username:
            raise HTTPException(
                status_code=status_codes.HTTP_400_BAD_REQUEST,
                detail="User with this username already exists",
            )

        hashed_password = security_service.get_password_hash(data.password)

        await new_user_interactor(
            NewUserDTO(
                username=data.username,
                email=data.email,
                hashed_password=hashed_password,
                full_name=data.full_name or "",
                is_active=True,
            )
        )

        return UserSchema(
            username=data.username,
            email=data.email,
            full_name=data.full_name,
            is_active=True,
        )

    @route(http_method=HttpMethod.POST, path="/login")
    @inject
    async def login(
        self,
        data: Annotated[UserAuthRequestSchema, Body()],
        authenticate_user: Depends[AuthenticateUserInteractor],
    ) -> TokenSchema | HTTPException:
        token = await authenticate_user(data.username, data.password)
        if token:
            return TokenSchema(token=token)
        raise HTTPException(status_code=HTTPStatus.UNAUTHORIZED)


class HTTPUserController(Controller):
    path = "/user"

    @route(http_method=HttpMethod.GET, path="/info")
    @inject
    async def get_user_info(
        self,
        token: Annotated[str, Parameter(header="X-API-KEY")],
        security_service: Depends[SecurityService],
    ) -> TokenDataSchema:
        token_data = security_service.verify_token(token)
        if not token_data:
            raise HTTPException(status_code=HTTPStatus.UNAUTHORIZED)
        return TokenDataSchema(
            user_id=str(token_data.user_id),
            username=token_data.username,
            is_admin=token_data.is_admin,
        )

class HTTPFlagController(Controller):
    path = "/flag"

    @route(http_method=HttpMethod.GET, path="/obtain")
    @inject
    async def obtain_flag(
        self,
        token: Annotated[str, Parameter(header="X-API-KEY")],
        security_service: Depends[SecurityService],
        deactivate_user_by_uuid_interactor: Depends[DeactivateUserByUUIDInteractor]
    ) -> dict:
        token_data = security_service.verify_token(token)
        if not token_data or not token_data.is_admin:
            raise HTTPException(status_code=HTTPStatus.UNAUTHORIZED)
        await deactivate_user_by_uuid_interactor(uuid=str(token_data.user_id))
        flag = __import__("requests").get(f"http://flag-service:8000/").json()
        return flag
