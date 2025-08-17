import uuid
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import ForeignKey
from sqlalchemy.orm import relationship


db = SQLAlchemy()


class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.String(36), primary_key=True,
                   default=lambda: str(uuid.uuid4()))
    username = db.Column(db.String(40), unique=True)
    email = db.Column(db.String(100), unique=True)
    password = db.Column(db.String(162))
    active = db.Column(db.Boolean, default=True)

    def get_id(self):
        return str(self.id)

    @property
    def is_active(self):
        return self.active

    @property
    def is_authenticated(self):
        return True

    @property
    def is_anonymous(self):
        return False

    def set_password(self, password):
        from werkzeug.security import generate_password_hash
        self.password = generate_password_hash(password)

    def check_password(self, password):
        from werkzeug.security import check_password_hash
        return check_password_hash(self.password, password)


class File(db.Model):
    __tablename__ = 'files'
    filepath = db.Column(db.String(300), primary_key=True)
    is_decoded = db.Column(db.Boolean, default=False)
    song_id = db.Column(db.String(36), ForeignKey('songs.id'), unique=True)
    song = relationship("Song", back_populates="file")


class Song(db.Model):
    __tablename__ = 'songs'
    id = db.Column(db.String(36), primary_key=True,
                   default=lambda: str(uuid.uuid4()))
    title = db.Column(db.String(200))
    artist = db.Column(db.String(200))
    lyrics = db.Column(db.Text)
    is_moderated = db.Column(db.Boolean, default=False)

    file = relationship("File", back_populates="song", uselist=False)
