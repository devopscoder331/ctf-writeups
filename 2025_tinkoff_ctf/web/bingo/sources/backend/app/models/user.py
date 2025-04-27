import pickle
import os
from datetime import datetime
from app import bcrypt
import uuid

class User:
    def __init__(self, username=None):
        self.id = str(uuid.uuid4())
        self.username = username
        self.password_hash = None
        self.created_at = datetime.utcnow()

    def set_password(self, password):
        self.password_hash = bcrypt.generate_password_hash(password).decode('utf-8')

    def check_password(self, password):
        return bcrypt.check_password_hash(self.password_hash, password)

    def to_dict(self):
        return {
            'id': self.id,
            'username': self.username,
            'created_at': self.created_at.isoformat()
        }

    @staticmethod
    def get_storage_path():
        storage_dir = '/tmp/storage/users'
        os.makedirs(storage_dir, exist_ok=True)
        return storage_dir

    def save(self):
        filepath = os.path.join(self.get_storage_path(), f'user_{self.id}.pickle')
        with open(filepath, 'wb') as f:
            pickle.dump(self, f)
        return self

    @classmethod
    def load(cls, user_id):
        filepath = os.path.join(cls.get_storage_path(), f'user_{user_id}.pickle')
        try:
            with open(filepath, 'rb') as f:
                return pickle.load(f)
        except FileNotFoundError:
            return None

    @classmethod
    def find_by_username(cls, username):
        storage_dir = cls.get_storage_path()
        for filename in os.listdir(storage_dir):
            if filename.endswith('.pickle'):
                with open(os.path.join(storage_dir, filename), 'rb') as f:
                    user = pickle.load(f)
                    if user.username == username:
                        return user
        return None 