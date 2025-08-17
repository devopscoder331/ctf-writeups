"""
Centralized configuration for 5D Minesweeper CTF Challenge.

This module contains all configuration constants in one place,
following DRY principle and making maintenance easier.
"""

import os
from typing import Final

from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# Game Parameters
BOARD_SIZE: Final[int] = 3
"""Size of each dimension (3^5 = 243 total cells)"""

MINE_COUNT: Final[int] = 60

# Server Configuration
# Load CTF flag from environment variable
CTF_FLAG: Final[str] = os.getenv("CTF_FLAG", "CTFZONE{default_flag_for_development}")
"""CTF flag loaded from environment variable"""

SECRET_KEY: Final[str] = os.getenv("SECRET_KEY", "ctf-5d-minesweeper-2025")
"""Secret key for flag generation"""

HOST: Final[str] = "0.0.0.0"
"""Server host address"""

PORT: Final[int] = 5000
"""Server port"""

# CORS Origins (for frontend support)
CORS_ORIGINS: Final[list[str]] = [
    "http://localhost:3000",
    "http://localhost:5173",
    "http://127.0.0.1:3000",
    "http://127.0.0.1:5173",
    "http://localhost:8080",
    "http://127.0.0.1:8080",
    "https://minesweeper.webs.ctf.ad",
    "http://minesweeper.webs.ctf.ad"
]

# Computed Values
TOTAL_CELLS: Final[int] = BOARD_SIZE ** 5
"""Total number of cells in the board"""

MAX_NEIGHBORS: Final[int] = 10
"""Neighbors per cell with cross adjacency on torus"""

MINE_DENSITY: Final[float] = MINE_COUNT / TOTAL_CELLS
"""Mine density as percentage"""
