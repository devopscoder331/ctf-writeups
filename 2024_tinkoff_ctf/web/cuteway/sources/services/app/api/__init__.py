from fastapi import APIRouter

from app.api import auth, card

router = APIRouter()
router.include_router(auth.router, prefix='', tags=['auth'])
router.include_router(card.router, prefix='/card', tags=['card'])
