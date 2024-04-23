FROM python:3-alpine

WORKDIR /code/app

COPY ./requirements.txt .

RUN pip install --no-cache-dir --upgrade -r requirements.txt

COPY . .
