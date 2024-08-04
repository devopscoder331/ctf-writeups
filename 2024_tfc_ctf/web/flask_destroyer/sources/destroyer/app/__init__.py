from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from app.config import Config
from flask_session import Session

db = SQLAlchemy()

def create_app():
    from app import routes

    app = Flask(__name__)
    app.config.from_object(Config)

    db.init_app(app)
    Session(app)

    app.register_blueprint(routes.bp)

    with app.app_context() as ctx:
        ctx.push()
        
    return app