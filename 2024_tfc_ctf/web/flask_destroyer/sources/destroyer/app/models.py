from flask_sqlalchemy import SQLAlchemy
from app import db
from app.functions import strtok


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(255), unique=True, nullable=False)
    password = db.Column(db.String(255), nullable=False)

    def __init__(self, id, credentials):
        self.id = id
        self.username = strtok(credentials, ':')[0]
        self.password = strtok(credentials, ':')[1]

    def __repr__(self) -> str:
        return f'User(id={self.id}, username={self.username}, password={self.password})'