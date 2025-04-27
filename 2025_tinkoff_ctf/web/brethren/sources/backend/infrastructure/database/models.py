from datetime import datetime
import uuid

from sqlalchemy import String, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship


class Base(DeclarativeBase):
    pass


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(
        String, primary_key=True, default=lambda: str(uuid.uuid4())
    )
    username: Mapped[str] = mapped_column(String, unique=True, nullable=False)
    email: Mapped[str] = mapped_column(String, unique=True, nullable=False)
    hashed_password: Mapped[str] = mapped_column(String, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime, default=datetime.utcnow, nullable=False
    )
    role: Mapped[str] = mapped_column(String, nullable=False)

    courses = relationship("Course", secondary="user_courses", back_populates="users")


class Course(Base):
    __tablename__ = "courses"

    id: Mapped[str] = mapped_column(
        String, primary_key=True, default=lambda: str(uuid.uuid4())
    )
    name: Mapped[str] = mapped_column(String, nullable=False)
    description: Mapped[str] = mapped_column(String, nullable=False)
    code: Mapped[str] = mapped_column(String, unique=True, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    users = relationship("User", secondary="user_courses", back_populates="courses")
    tickets = relationship("Ticket", back_populates="course")


class Ticket(Base):
    __tablename__ = "tickets"

    id: Mapped[str] = mapped_column(
        String, primary_key=True, default=lambda: str(uuid.uuid4())
    )
    course_id: Mapped[str] = mapped_column(
        String, ForeignKey("courses.id"), nullable=False
    )
    title: Mapped[str] = mapped_column(String, nullable=False)
    file_path: Mapped[str] = mapped_column(String, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime, default=datetime.utcnow, nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False
    )

    course = relationship("Course", back_populates="tickets")


class UserCourse(Base):
    __tablename__ = "user_courses"

    user_id: Mapped[str] = mapped_column(
        String, ForeignKey("users.id"), primary_key=True
    )
    course_id: Mapped[str] = mapped_column(
        String, ForeignKey("courses.id"), primary_key=True
    )
    role: Mapped[str] = mapped_column(String, nullable=False)  # student, teacher
    joined_at: Mapped[datetime] = mapped_column(
        DateTime, default=datetime.utcnow, nullable=False
    )
