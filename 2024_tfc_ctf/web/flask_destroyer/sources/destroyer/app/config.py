import os
import secrets

class Config:
    SECRET_KEY = secrets.token_hex(32)
    SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or 'mysql://bob:password@localhost/destroyer'
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    TEMPLATES_AUTO_RELOAD = False
    SESSION_PERMANENT = True
    SESSION_TYPE = 'filesystem'
    WTF_CSRF_CHECK_DEFAULT = False