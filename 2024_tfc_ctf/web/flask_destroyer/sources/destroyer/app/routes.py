from flask import render_template, url_for, flash, redirect, request, Blueprint, current_app, jsonify, session
from app import db
from app.forms import LoginForm
from app.database import get_user_by_username_password
from app.models import User
import os

bp = Blueprint('main', __name__)
registered_templates = os.listdir('app/templates')

@bp.route('/login', methods=['GET', 'POST'])
def login():
    form = LoginForm(request.form, meta={'csrf': False})
    if form.validate_on_submit():
        user = get_user_by_username_password(form.username.data, form.password.data)
        if user:
            session['id'] = user.id
            return redirect(url_for('main.home'))
        else:
            flash('Login Unsuccessful. Please check username and password', 'danger')

    return render_template('login.html', title='Login', form=form)

@bp.get('/logout')
def logout():
    session['id'] = None
    return redirect(url_for('main.login'))

# Define a route with a URL parameter
@bp.route('/<string:page>')
@bp.route('/', defaults={'page': 'home.html'})
def home(page):
    if not session.get('id'):
        return redirect(url_for('main.login'))

    page = os.path.basename(page)
    if page not in registered_templates or not os.path.isfile(os.path.join('app/templates', page)):
        return render_template('home.html')

    return render_template(page)