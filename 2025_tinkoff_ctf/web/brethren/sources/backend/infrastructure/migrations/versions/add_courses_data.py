"""add courses data

Revision ID: add_courses_data
Revises: 5fb714d70134
Create Date: 2024-03-08 19:45:00.000000

"""

from typing import Sequence, Union
from uuid import UUID, uuid4

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID as PG_UUID

# revision identifiers, used by Alembic.
revision: str = "add_courses_data"
down_revision: Union[str, None] = "5fb714d70134"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def _create_course(uuid: str, name: str, code: str, description: str) -> None:
    op.execute(
        sa.text(
            """
            INSERT INTO courses (id, name, code, description, is_active, created_at, updated_at)
            VALUES (cast(:id as uuid), :name, :code, :description, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """
        ).bindparams(id=uuid, name=name, code=code, description=description)
    )


def upgrade() -> None:
    # Создаем курсы
    _create_course(
        "2a7b6e4d-9c1f-4f8a-b5d3-e8f2c9a1d0b3",
        "Капибарология",
        "CAPYBAROLOGY",
        "Основы и практика изучения капибар",
    )
    _create_course(
        "3f9d2e8c-6b5a-4c7d-9e1f-7d8a2b4c5f9a",
        "Водные развлечения",
        "WATER_FUN",
        "Изучение водных активностей и их влияния на капибар",
    )
    _create_course(
        "f836803c-825a-4303-b519-8c05354f8c44",
        "Социальная психология грызунов",
        "RODENT_PSYCH",
        "Исследование социального поведения и психологии грызунов",
    )
    _create_course(
        "4e1d8f5a-2b3c-4a9e-8d7f-6c0b9e3d2a1c",
        "Травоведение и гастрономия",
        "GRASS_GASTRO",
        "Изучение пищевых предпочтений и питания капибар",
    )
    _create_course(
        "5b2c9d7e-1a4f-4e8b-9c6d-3f5e8a2b1d0c",
        "Искусство релаксации",
        "RELAX_ART",
        "Техники и методы расслабления от капибар",
    )
    _create_course(
        "6d4e8f2a-3b5c-4d9e-7a8f-1c2b3e4d5f6a",
        "Межвидовая дипломатия",
        "SPECIES_DIPL",
        "Основы межвидового взаимодействия и коммуникации",
    )
    _create_course(
        "7f1e9d3b-8c4a-5b6d-2e7f-9a8b7c6d5e4f",
        "Акватическая механика",
        "AQUA_MECH",
        "Изучение движения и физики в водной среде",
    )
    _create_course(
        "8a9b7c5d-6e4f-3d2e-1f9a-8b7c6d5e4f3a",
        "Теория милоты",
        "CUTE_THEORY",
        "Исследование принципов и практик очарования",
    )
    _create_course(
        "9c8d7e6f-5a4b-3c2d-1e9f-8a7b6c5d4e3b",
        "Экологическое взаимодействие",
        "ECO_INTERACT",
        "Изучение взаимодействия капибар с окружающей средой",
    )


def downgrade() -> None:
    # Удаляем все созданные курсы
    op.execute(sa.text("DELETE FROM courses"))
