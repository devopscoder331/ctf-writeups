import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = os.getenv("SECRET_KEY", "/*REDACTED*/")

AES_KEY = os.getenv("AES_KEY", "/*REDACTED1234*/")

DEBUG = os.getenv("DEBUG", "False").lower() in ("true", "1", "t")

HOST = os.getenv("HOST", "0.0.0.0")
PORT = int(os.getenv("PORT", "8000")) 

FLAG = "tctf{/*REDACTED*/}"