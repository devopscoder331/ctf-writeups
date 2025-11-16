from flask import Flask, render_template, session, redirect, url_for
from auth.routes import auth
from notes.routes import notes

import os

app = Flask(__name__)
app.secret_key = os.urandom(64)

# Register Blueprints
app.register_blueprint(auth)
app.register_blueprint(notes)

@app.route("/")
def index():
    return render_template("index.html")


if __name__ == '__main__':
    app.run()

