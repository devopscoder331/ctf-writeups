import os
from flask import jsonify
from functools import wraps

def handle_exceptions(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        try:
            return func(*args, **kwargs)
        except Exception as err:
            return jsonify([{
                "error": str(err)
            }]), 500
    return wrapper


def is_allowed_file_format(file_mime):
    return file_mime in [
        'audio/mpeg',       # MP3
        'audio/wav',        # WAV
        'audio/ogg',        # OGG
        'audio/x-wav',      # WAV
    ]


def is_allowed_file_extension(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in {'mp3', 'wav', 'ogg'}


def is_allowed_file_size(file):
    MAX_SONG_FILE_SIZE = 3 * 1024 * 1024  # 3 MB
    file.seek(0, os.SEEK_END)
    file_length = file.tell()
    file.seek(0)
    print(file_length)
    print(file_length <= MAX_SONG_FILE_SIZE)
    return file_length <= MAX_SONG_FILE_SIZE
