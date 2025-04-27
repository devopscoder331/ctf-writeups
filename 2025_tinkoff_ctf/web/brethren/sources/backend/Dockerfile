FROM python:3.10-slim

WORKDIR /app

RUN apt-get update && apt-get install -y gcc \
    && apt-get install -y libpq-dev \
    && rm -rf /var/lib/apt/lists/*

RUN pip install poetry

COPY pyproject.toml poetry.lock ./

RUN poetry config virtualenvs.create false \
    && poetry install --no-interaction --no-ansi

COPY .. .

CMD sh -c "poetry run alembic upgrade head; \
           poetry run uvicorn --factory main:get_app \
           --host 0.0.0.0 --port 8000 \
           --workers 4 --loop uvloop \
           --http httptools --limit-concurrency 1000 \
           --backlog 2048"