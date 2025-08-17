import os
from flask import Flask
from flask_migrate import Migrate
from celery import Celery, Task
from flask_login import LoginManager

from models import db, User


migrate = Migrate()


def celery_init_app(app: Flask) -> Celery:
    class FlaskTask(Task):
        def __call__(self, *args: object, **kwargs: object) -> object:
            with app.app_context():
                return self.run(*args, **kwargs)

    celery_app = Celery(app.name, task_cls=FlaskTask)
    celery_app.config_from_object(app.config["CELERY"])
    celery_app.set_default()
    app.extensions["celery"] = celery_app
    return celery_app


def create_app():
    app = Flask(__name__)
    app.config.update(
        SECRET_KEY=os.getenv('SECRET_KEY', 'asdfsa3dfny2e'),
        UPLOAD_FOLDER='uploads',
        GOOGLE_SECRET_KEY=os.getenv('GOOGLE_SECRET_KEY', ''),
        SQLALCHEMY_DATABASE_URI=os.getenv(
            'DATABASE_URL', 'postgresql://postgres:postgres@db:5432/flask_db'),
        SQLALCHEMY_TRACK_MODIFICATIONS=False,
        CELERY=dict(
            broker_url=os.getenv('REDIS_URL', 'redis://redis:6379/0'),
            result_backend=os.getenv('REDIS_URL', 'redis://redis:6379/0'),
            task_ignore_result=True
        ),
        MAX_CONTENT_LENGTH=16 * 1024 * 1024
    )
    os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
    db.init_app(app)
    migrate.init_app(app, db)

    celery_init_app(app)

    with app.app_context():
        from routes import routes_bp
        app.register_blueprint(routes_bp)

    return app


app = create_app()
login_manager = LoginManager()
login_manager.login_view = 'routes.admin_login_view'
login_manager.login_message_category = 'error'


@login_manager.user_loader
def load_user(user_id):
    return User.query.get(user_id)


login_manager.init_app(app)
