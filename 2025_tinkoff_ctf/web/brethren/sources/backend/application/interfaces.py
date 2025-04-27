from abc import abstractmethod
from typing import Protocol
from uuid import UUID

from domain.entities import CourseDM, UserDM


class CourseSaver(Protocol):
    @abstractmethod
    async def save(self, course: CourseDM) -> None: ...


class CourseReader(Protocol):
    @abstractmethod
    async def read_by_uuid(self, uuid: str) -> CourseDM | None: ...

    @abstractmethod
    async def read_all(self) -> list[CourseDM] | None: ...


class TicketReader(Protocol):
    @abstractmethod
    async def read_ticket_file(
        self, base_path: str, course_id: str, ticket_id: str
    ) -> str | None: ...

    @abstractmethod
    async def read_course_tickets(
        self, base_path: str, course_id: str
    ) -> str | None: ...

    @abstractmethod
    async def random_course_ticket(
        self, base_path: str, course_id: str
    ) -> str | None: ...


class UserSaver(Protocol):
    @abstractmethod
    async def save(self, user: UserDM) -> None: ...


class UserReader(Protocol):
    @abstractmethod
    async def authenticate(
        self, username: str, password_hash: str
    ) -> UserDM | None: ...

    @abstractmethod
    async def read_user_by_email(self, email: str) -> None: ...

    @abstractmethod
    async def read_user_by_username(self, username: str) -> None: ...

    @abstractmethod
    async def read_user_by_id(self, uuid: str) -> None: ...


class UserDeactivator(Protocol):
    @abstractmethod
    async def deactivate_user_by_id(self, uuid: str) -> None: ...


class UUIDGenerator(Protocol):
    def __call__(self) -> UUID: ...


class DBSession(Protocol):
    @abstractmethod
    async def commit(self) -> None: ...

    @abstractmethod
    async def flush(self) -> None: ...
