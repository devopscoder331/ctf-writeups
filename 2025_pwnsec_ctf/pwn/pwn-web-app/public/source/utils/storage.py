from flask import session

users = {}
def get_user():
    return users.get(session['username'].decode('latin-1'))
