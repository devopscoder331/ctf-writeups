import time
import os
import requests
from flask import Blueprint, request, jsonify, render_template, abort, current_app, make_response, redirect, url_for
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import check_password_hash
from werkzeug.utils import secure_filename

from celery_app.tasks import transcribe_audio
from models import db, Song, File, User
from functions import handle_exceptions, is_allowed_file_extension, is_allowed_file_format, is_allowed_file_size


routes_bp = Blueprint('routes', __name__)


@routes_bp.route("/", methods=["GET"])
def index():
    moderated_songs = Song.query.filter_by(is_moderated=True).all()

    songs = [
        {
            "id": song.id,
            "title": song.title,
            "artist": song.artist,
            "lyrics": song.lyrics
        }
        for song in moderated_songs
    ]

    return render_template('index.html', songs=songs)


@routes_bp.route("/song/<song_id>", methods=["GET"])
def song_view(song_id):
    song = Song.query.filter(
        Song.id == song_id,
        Song.is_moderated == True
    ).first()

    if not song:
        abort(404)

    song_data = {
        "id": song.id,
        "title": song.title,
        "artist": song.artist,
        "lyrics": song.lyrics
    }

    return render_template('song.html', song=song_data)


@routes_bp.route("/song/upload", methods=["POST"])
@handle_exceptions
def upload_song_method():

    captcha_response = requests.post('https://www.google.com/recaptcha/api/siteverify', data={
        'secret': current_app.config['GOOGLE_SECRET_KEY'],
        'response': request.form.get('g-recaptcha-response')
    })
    captcha_result = captcha_response.json()
    if not captcha_result.get('success'):
        return jsonify({'error': 'CAPTCHA verification failed.'}), 500

    if 'audio_file' not in request.files:
        return jsonify({'error': 'An audio file must be attached'}), 500

    file = request.files['audio_file']
    if file and file.filename == '':
        return jsonify({'error': 'The file name must not be empty'}), 500

    if file and not is_allowed_file_size(file):
        return jsonify({'error': 'Invalid file size. The file size must be less than 3Mb.'}), 500

    if file and (not is_allowed_file_extension(file.filename) or not is_allowed_file_format(file.mimetype)):
        return jsonify({'error': 'Invalid file format. The file must be in: MP3, WAV, or OGG.'}), 500

    filename = secure_filename(f'{int(time.time() * 1000000)}_{file.filename}')
    filepath = os.path.join(current_app.config['UPLOAD_FOLDER'], filename)
    file.save(filepath)

    fileobj = File(filepath=filepath)
    db.session.add(fileobj)
    db.session.flush()
    db.session.commit()

    title = request.form.get('title')
    artist = request.form.get('artist')
    transcribe_audio.delay(filepath, title, artist)

    return jsonify({'status': 'ok'}), 200


@routes_bp.route("/admin/login", methods=["GET", "POST"])
def admin_login_view():
    if current_user.is_authenticated:
        return redirect(url_for('routes.moderation_list_view'))
    error = None
    if request.method == 'POST':
        try:
            email = request.form.get('email')
            password = request.form.get('password')

            user = User.query.filter_by(email=email).first()

            if user and check_password_hash(user.password, password):
                login_user(user)
                next_page = request.args.get(
                    'next') or url_for('routes.moderation_list_view')
                return redirect(next_page)
            else:
                error = 'Invalid username or password'
        except Exception as err:
            error = str(err)
    return render_template('auth.html',  error=error)


@routes_bp.route("/admin/logout")
@login_required
def admin_logout():
    logout_user()
    return redirect(url_for('routes.index'))


@routes_bp.route("/admin/moderation_list", methods=["GET"])
@login_required
@handle_exceptions
def moderation_list_view():
    songs = Song.query.order_by(
        Song.is_moderated.asc(), Song.title.asc()).all()
    return render_template('moderation_list.html', songs=songs)


@routes_bp.route("/admin/moderation_list/<id>", methods=["GET"])
@login_required
@handle_exceptions
def moderation_item_view(id):
    return render_template('moderate_song.html', id=id)


@routes_bp.route("/admin/api/song/<id>", methods=["GET"])
@login_required
@handle_exceptions
def get_song_data(id):
    song = Song.query.get_or_404(id)
    return jsonify({
        "song": {
            "title": song.title,
            "artist": song.artist,
            "lyrics": song.lyrics,
            "is_moderated": song.is_moderated
        }
    })


@routes_bp.route("/admin/api/song/<id>", methods=["POST"])
@login_required
@handle_exceptions
def update_song_data(id):
    song = Song.query.get_or_404(id)
    data = request.get_json()

    song.title = data.get('title', song.title)
    song.artist = data.get('artist', song.artist)
    song.lyrics = data.get('lyrics', song.lyrics)

    db.session.commit()
    return jsonify({
        "success": True,
        "song": {
            "title": song.title,
            "artist": song.artist,
            "lyrics": song.lyrics
        }
    })


@routes_bp.route("/admin/api/song/<id>/approve", methods=["GET"])
@login_required
@handle_exceptions
def approve_song(id):
    song = Song.query.get_or_404(id)
    song.is_moderated = True
    db.session.commit()
    return jsonify({"success": True, "is_moderated": True})


@routes_bp.route("/admin/api/song/<id>/decline", methods=["GET"])
@login_required
@handle_exceptions
def decline_song(id):
    song = Song.query.get_or_404(id)
    song.is_moderated = False
    db.session.commit()
    return jsonify({"success": True, "is_moderated": False})


@routes_bp.route("/admin/api/flag", methods=["GET"])
@login_required
@handle_exceptions
def get_flag():
    return jsonify({"flag": os.getenv('FLAG', 'ctfzone{test}'), })


@routes_bp.app_errorhandler(404)
def page_not_found(e):
    return render_template('404.html'), 404
