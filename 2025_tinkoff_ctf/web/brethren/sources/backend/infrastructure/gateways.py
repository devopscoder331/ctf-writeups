import random
from pathlib import Path

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.sql import text

from application.interfaces import (
    CourseReader,
    CourseSaver,
    UserSaver,
    UserReader,
    TicketReader,
)
from domain.entities import CourseDM, UserDM

from application.interfaces import UserDeactivator


class CourseGateway(
    CourseReader,
    CourseSaver,
):
    def __init__(self, session: AsyncSession):
        self._session = session

    async def read_by_uuid(self, uuid: str) -> CourseDM | None:
        query = text("SELECT * FROM courses WHERE id = :uuid")
        result = await self._session.execute(
            statement=query,
            params={"uuid": uuid},
        )
        row = result.fetchone()
        if not row:
            return None
        return CourseDM(
            id=row.id,
            name=row.name,
            description=row.description,
            code=row.code,
            is_active=row.is_active,
        )

    async def read_all(self) -> list[CourseDM] | None:
        query = text("SELECT * FROM courses")
        result = await self._session.execute(statement=query)
        rows = result.fetchall()
        return [
            CourseDM(
                id=row.id,
                name=row.name,
                description=row.description,
                code=row.code,
                is_active=row.is_active,
            )
            for row in rows
        ]

    async def save(self, course: CourseDM) -> None:
        query = text(
            "INSERT INTO courses (id, name, description, code, is_active) VALUES (:id, :name, :description, :code, :is_active)"
        )
        await self._session.execute(
            statement=query,
            params={
                "id": course.id,
                "name": course.name,
                "description": course.description,
                "code": course.code,
                "is_active": course.is_active,
            },
        )
        await self._session.commit()


class UserGateway(UserSaver, UserReader, UserDeactivator):
    def __init__(self, session: AsyncSession):
        self._session = session

    async def authenticate(self, username: str, password_hash: str) -> UserDM | None:
        query = text("SELECT * FROM users WHERE username = :username")
        result = await self._session.execute(
            statement=query,
            params={"username": username, "password_hash": password_hash},
        )
        row = result.fetchone()
        if not row:
            return None
        return UserDM(
            id=row.id,
            username=row.username,
            email=row.email,
            hashed_password=row.password_hash,
            full_name=row.full_name,
            is_active=row.is_active,
            is_admin=row.is_admin,
        )

    async def save(self, user: UserDM) -> None:
        query = text(
            "INSERT INTO users (id, username, email, password_hash, full_name, is_active, is_admin) VALUES (:id, :username, :email, :password_hash, :full_name, :is_active, :is_admin)"
        )
        await self._session.execute(
            statement=query,
            params={
                "id": user.id,
                "username": user.username,
                "email": user.email,
                "password_hash": user.hashed_password,
                "full_name": user.full_name,
                "is_active": user.is_active,
                "is_admin": user.is_admin,
            },
        )
        await self._session.commit()

    async def read_user_by_id(self, uuid: str) -> UserDM | None:
        query = text("SELECT * FROM users WHERE id = :uuid")

        result = await self._session.execute(
            statement=query,
            params={"uuid": uuid},
        )

        row = result.fetchone()
        if not row:
            return None

        return UserDM(
            id=row.id,
            username=row.username,
            email=row.email,
            hashed_password=row.password_hash,
            full_name=row.full_name,
            is_active=row.is_active,
            is_admin=row.is_admin,
        )

    async def read_user_by_email(self, email: str) -> UserDM | None:
        query = text("SELECT * FROM users WHERE email = :email")

        result = await self._session.execute(
            statement=query,
            params={"email": email},
        )

        row = result.fetchone()
        if not row:
            return None

        return UserDM(
            id=row.id,
            username=row.username,
            email=row.email,
            hashed_password=row.password_hash,
            full_name=row.full_name,
            is_active=row.is_active,
            is_admin=row.is_admin,
        )

    async def read_user_by_username(self, username: str) -> UserDM | None:
        query = text("SELECT * FROM users WHERE username = :username")

        result = await self._session.execute(
            statement=query,
            params={"username": username},
        )

        row = result.fetchone()
        if not row:
            return None

        return UserDM(
            id=row.id,
            username=row.username,
            email=row.email,
            hashed_password=row.password_hash,
            full_name=row.full_name,
            is_active=row.is_active,
            is_admin=row.is_admin,
        )

    async def deactivate_user_by_id(self, uuid: str) ->  None:
        query = text("UPDATE users SET is_active=FALSE WHERE id = :uuid")

        await self._session.execute(
            statement=query,
            params={"uuid": uuid},
        )
        await self._session.commit()

class TicketGateway(TicketReader):
    async def read_course_tickets(self, base_path: str, course_id: str) -> list | None:
        course_path = Path(base_path) / Path(course_id.replace("../", "replaced"))

        try:
            if not course_path.is_dir():
                raise FileNotFoundError("Directory does not exist")

            return [
                str(f.relative_to(course_path))
                for f in course_path.rglob("*")
                if f.is_file()
            ]
        except (FileNotFoundError, PermissionError):
            raise FileNotFoundError("Error accessing file")

    async def read_ticket_file(
        self, base_path: str, course_id: str, ticket_id: str
    ) -> str | None:
        file_path = Path(base_path) / (course_id + "/" + ticket_id).replace(
            "../", "replaced"
        )

        try:
            if not file_path.is_file():
                raise FileNotFoundError("File not found")

            return file_path.read_text()
        except (FileNotFoundError, PermissionError):
            raise FileNotFoundError("Error accessing file")

    async def random_course_ticket(self, base_path: str, course_id: str) -> str | None:
        course_path = Path(base_path) / Path(course_id.replace("../", "replaced"))

        try:
            if not course_path.is_dir():
                raise FileNotFoundError("Directory does not exist")

            return await self.read_ticket_file(
                base_path=base_path,
                course_id=course_id,
                ticket_id=random.choice(
                    [
                        str(f.relative_to(course_path))
                        for f in course_path.rglob("*")
                        if f.is_file()
                    ]
                ),
            )
        except (FileNotFoundError, PermissionError):
            raise FileNotFoundError("Error accessing file")
