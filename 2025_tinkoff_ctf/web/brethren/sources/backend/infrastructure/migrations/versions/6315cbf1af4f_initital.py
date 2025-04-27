"""initital

Revision ID: 6315cbf1af4f
Revises:
Create Date: 2025-03-06 22:47:48.570517

"""

from typing import Sequence, Union


# revision identifiers, used by Alembic.
revision: str = "6315cbf1af4f"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    pass


def downgrade() -> None:
    """Downgrade schema."""
    pass
