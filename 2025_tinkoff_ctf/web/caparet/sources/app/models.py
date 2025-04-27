from pydantic import BaseModel, Field
from typing import Optional


class Play(BaseModel):
    id: int
    title: str
    description: str
    image: str
    only_for_adults: bool
    secret_word_needed: bool


class TicketRequest(BaseModel):
    play_id: int
    name: str = Field(..., min_length=1, max_length=50)
    age: int = Field(..., ge=1, le=100)
    comment: Optional[str] = Field(None, max_length=200)


class TicketResponse(BaseModel):
    token: str
    view_url: str


class Ticket(BaseModel):
    play_id: int
    name: str
    age: int
    comment: Optional[str] = None


PLAYS = [
    Play(
        id=1,
        title="Капаре",
        description="Основное шоу нашего театра",
        image="/images/capare.png",
        only_for_adults=True,
        secret_word_needed=True
    ),
    Play(
        id=2,
        title="Детский утренник",
        description="Праздничное представление для самых маленьких",
        image="/images/matinee.png",
        only_for_adults=False,
        secret_word_needed=False
    )
]


def get_play_by_id(play_id: int) -> Optional[Play]:
    """Получить спектакль по ID"""
    for play in PLAYS:
        if play.id == play_id:
            return play
    return None 