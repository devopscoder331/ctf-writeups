# Use an official Python runtime as a parent image
FROM python:3.12-alpine

# Set the working directory to /app
WORKDIR /app

# Install UV
COPY --from=ghcr.io/astral-sh/uv:latest /uv /uvx /bin/

COPY uv.lock pyproject.toml /app/
RUN uv sync --frozen --no-dev

# Copy source code
COPY minesweeper/ /app/minesweeper/


# Expose port
EXPOSE 5000

# Run the app
CMD ["uv", "run", "uvicorn", "minesweeper.app:app", "--host", "0.0.0.0", "--port", "5000"]
