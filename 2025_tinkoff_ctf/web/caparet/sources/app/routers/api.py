from fastapi import APIRouter, HTTPException, Request

from app.models import TicketRequest, TicketResponse, Ticket, PLAYS, get_play_by_id
from app.utils.crypto import encrypt_ticket, decrypt_ticket
from app.utils.flag import get_flag

router = APIRouter(prefix="/api", tags=["api"])


@router.get("/plays")
async def get_plays():
    """Получить список доступных спектаклей"""
    return [
        {
            "id": play.id,
            "title": play.title,
            "description": play.description,
            "image": play.image
        }
        for play in PLAYS
    ]


@router.post("/book", response_model=TicketResponse)
async def book_ticket(request: Request, ticket_request: TicketRequest):
    """Забронировать билет на спектакль"""
    if not ticket_request.name.strip():
        raise HTTPException(status_code=400, detail='Имя не может быть пустым')

    play = get_play_by_id(ticket_request.play_id)
    if not play:
        raise HTTPException(status_code=404, detail="Спектакль не найден")

    if play.only_for_adults and ticket_request.age >= 18:
        raise HTTPException(status_code=400, detail='Для бронирования билета на взрослый спектакль необходимо иметь паспорт и предоставить его в кассу.')
    
    ticket_data = Ticket(
        play_id=ticket_request.play_id,
        name=ticket_request.name,
        comment=ticket_request.comment,
        age=ticket_request.age,
    )
    
    token = encrypt_ticket(ticket_data.model_dump())
    
    base_url = str(request.base_url).split('/api')[0].rstrip('/')
    view_url = f"{base_url}/#t/{token}"
    
    return TicketResponse(token=token, view_url=view_url)


@router.get("/ticket/{token}")
async def get_ticket(token: str):
    """Получить информацию о билете по токену"""
    ticket_data = decrypt_ticket(token)

    if "error" in ticket_data:
        raise HTTPException(status_code=400, detail="Недействительный токен билета")
    
    play = get_play_by_id(ticket_data.get("play_id"))
    if not play:
        raise HTTPException(status_code=404, detail="Спектакль не найден")

    eligible = False
    secret_word = ""

    if play.only_for_adults:
        if ticket_data.get("age") >= 18:
            eligible = True
            secret_word = get_flag()
    else:
        eligible = True

    return {
        "ticket": ticket_data,
        "play": {
            "id": play.id,
            "title": play.title,
            "description": play.description,
            "image": play.image
        },
        "eligible": eligible,
        "secret_word": secret_word,
    }
