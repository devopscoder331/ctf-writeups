from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers import api
from app.config import DEBUG

app = FastAPI(
    title="Капаре",
    description="Онлайн-киоск театра Капаре",
    version="0.1.0",
    debug=DEBUG,
    # Disable docs in production
    docs_url="/docs" if DEBUG else None,
    redoc_url="/redoc" if DEBUG else None,
    openapi_url="/openapi.json" if DEBUG else None
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api.router)

@app.get("/")
async def root():
    return {"message": "Добро пожаловать в онлайн-киоск театра Капаре."} 