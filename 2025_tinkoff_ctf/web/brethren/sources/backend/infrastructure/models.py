from datetime import datetime
from typing import Optional
from uuid import uuid4, UUID

from sqlalchemy import DateTime, func, String, ForeignKey, Text
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
    )


class Course(Base):
    __tablename__ = "courses"

    id: Mapped[UUID] = mapped_column(primary_key=True, default=uuid4)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    description: Mapped[Optional[str]] = mapped_column(Text)
    code: Mapped[str] = mapped_column(String(50), unique=True, nullable=False)
    is_active: Mapped[bool] = mapped_column(default=True)


class User(Base):
    __tablename__ = "users"

    id: Mapped[UUID] = mapped_column(primary_key=True, default=uuid4)
    username: Mapped[str] = mapped_column(String(64), unique=True, nullable=False)
    email: Mapped[str] = mapped_column(String(255), unique=True, nullable=False)
    password_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    full_name: Mapped[Optional[str]] = mapped_column(String(255))
    is_active: Mapped[bool] = mapped_column(default=True)
    is_admin: Mapped[bool] = mapped_column(default=False)


class UserCourse(Base):
    __tablename__ = "user_courses"

    user_id: Mapped[UUID] = mapped_column(ForeignKey("users.id"), primary_key=True)
    course_id: Mapped[UUID] = mapped_column(ForeignKey("courses.id"), primary_key=True)
