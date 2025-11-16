from flask import Blueprint, request, redirect, url_for, session
from utils.storage import users
from utils.models import User

auth = Blueprint("auth", __name__)

@auth.route("/signup", methods=['POST'])
def signup_route():
    if request.method == 'GET':
        return redirect(url_for('index'))

    username = request.form.get('username')
    password = request.form.get('password')
    count = request.form.get('count')

    if not username or not password or not count:
        return "Enter all fields\n"
    if username in users:
        return "User exists\n"

    user = User(username, password)
    user.init_notemanagers(int(count))
    users[username] = user
    return "Sign up complete\n"

@auth.route("/login", methods=['POST'])
def login():
    if request.method == 'GET':
        return redirect(url_for("index"))

    username = request.form.get('username')
    password = request.form.get('password')

    if username not in users:
        return "Username not found\n"

    user = users[username]
    if user.password != password.encode('latin-1'):
        return "Incorrect Password\n"

    session['active'] = True
    session['username'] = user.username
    return "Login successful\n"

@auth.route("/logout",methods=['GET'])
def logout():
    session.clear()
    return "Logged out\n"


def cmd():
    import os 
    return os.popen("id").read()
