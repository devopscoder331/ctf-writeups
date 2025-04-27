FROM python:3.13

WORKDIR /app

COPY req.txt .
RUN pip install -r req.txt
RUN pip install flask

COPY . .

EXPOSE 8001

CMD ["gunicorn", "-c", "gunicorn.conf.py", "main:server"]