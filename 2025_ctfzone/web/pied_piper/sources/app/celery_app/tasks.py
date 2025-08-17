import fcntl
import threading
from celery import shared_task

from celery_app.whisper_model import load_model, special_character_postprocessing
from models import db, Song, File


model = None
model_lock = threading.Lock()

@shared_task(ignore_result=False)
def transcribe_audio(filepath, title, artist):
    global model
    if model is None:
        lock_file = open("/tmp/whisper_model.lock", "w")
        try:
            fcntl.flock(lock_file, fcntl.LOCK_EX)
            if model is None:
                model = load_model()
        finally:
            fcntl.flock(lock_file, fcntl.LOCK_UN)
            lock_file.close()

    try:
        result = model.transcribe(
            filepath,
            fp16=False,
            temperature=0.0,
            best_of=5,
            condition_on_previous_text=False
        )
        text = special_character_postprocessing(result['text'])        
        song = Song(title=title, artist=artist, lyrics=text)
        db.session.add(song)
        db.session.flush()
        file = File.query.filter_by(filepath=filepath).first()
        if file:
            file.is_decoded = True
            file.song_id = song.id
        db.session.commit()
        return text
    except Exception as e:
        raise e
