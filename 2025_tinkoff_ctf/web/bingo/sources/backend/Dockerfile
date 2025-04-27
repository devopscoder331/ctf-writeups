FROM node:18 AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM python:3.11

ENV PYTHONUNBUFFERED=1
WORKDIR /app
COPY backend/requirements.txt ./
RUN pip install -r requirements.txt
COPY backend/app/ ./app/
COPY backend/run.py ./
COPY --from=frontend-build /app/build ./frontend/build

COPY backend/flag.txt /tmp/
