import bcrypt
from app import db
from app.models import User
from sqlalchemy import text

def get_user_by_username_password(username, password):
    """Fetch user by username."""
    query = "SELECT * FROM user WHERE username = \"{}\" AND password = \"{}\"".format(username, password)
    result = db.session.execute(text(query))
    row = result.fetchone()

    if row:
        user = User(id = row[0], credentials = f'{row[1]}:{row[2]}')
        return user

    return None