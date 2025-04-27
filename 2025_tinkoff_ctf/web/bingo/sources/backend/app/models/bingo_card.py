import pickle
import os
from datetime import datetime
import json
import uuid
import base64

class BingoCard:
    def __init__(self, user_id=None):
        self.id = str(uuid.uuid4())
        self.background_data = None
        self.background_size = 0
        self.magic_mantras = []
        self.user_id = user_id
        self.numbers = None
        self.created_at = datetime.utcnow()
        self.is_completed = False
        self.winning_numbers = None
        self.is_played = False
        self.is_active = True

    def set_numbers(self, numbers_grid):
        self.numbers = json.dumps(numbers_grid)

    def get_numbers(self):
        return json.loads(self.numbers)

    def set_winning_numbers(self, numbers):
        self.winning_numbers = json.dumps(numbers)

    def get_winning_numbers(self):
        return json.loads(self.winning_numbers) if self.winning_numbers else []

    def set_background(self, image_data):
        if image_data and image_data.startswith('data:image'):
            raw_data = base64.b64decode(image_data.split(',')[1])
            self.background_data = raw_data
            self.background_size = len(raw_data)
        else:
            self.background_data = None
            self.background_size = 0

    def set_magic_mantras(self, mantras):
        self.magic_mantras = mantras if mantras else []
        
    def get_magic_mantras(self):
        return self.magic_mantras if self.magic_mantras else []

    def to_dict(self):
        background_image = 'data:;base64,' + base64.b64encode(self.background_data).decode().replace('\n', '') if self.background_data else None
        return {
            'id': self.id,
            'user_id': self.user_id,
            'numbers': self.get_numbers(),
            'created_at': self.created_at.isoformat(),
            'is_completed': self.is_completed,
            'winning_numbers': self.get_winning_numbers() if self.winning_numbers else None,
            'is_played': self.is_played,
            'is_active': bool(self.winning_numbers and not self.is_completed),
            'background_image': background_image,
            'background_size': self.background_size,
            'magic_mantras': self.get_magic_mantras()
        }

    @staticmethod
    def get_storage_path(user_id=None):
        base_dir = '/tmp/storage/bingo_cards'
        if user_id:
            storage_dir = os.path.join(base_dir, str(user_id))
        else:
            storage_dir = base_dir
        os.makedirs(storage_dir, exist_ok=True)
        return storage_dir

    def save(self):
        filepath = os.path.join(self.get_storage_path(self.user_id), f'card_{self.id}.pickle')
        if not os.path.exists(filepath):
            open(filepath, 'w').close()
        pickle.dump(self, open(filepath, 'r+b'))
        return self

    @classmethod
    def load(cls, card_id):
        base_dir = cls.get_storage_path()
        for user_dir in os.listdir(base_dir):
            filepath = os.path.join(base_dir, user_dir, f'card_{card_id}.pickle')
            if os.path.exists(filepath):
                try:
                    return pickle.load(open(filepath, 'r+b'))
                except Exception:
                    return None
        return None

    @classmethod
    def get_all_by_user(cls, user_id, is_played=None):
        cards = []
        storage_dir = cls.get_storage_path(user_id)
        for filename in os.listdir(storage_dir):
            if filename.endswith('.pickle'):
                try:
                    card = pickle.load(open(os.path.join(storage_dir, filename), 'r+b'))
                    if card.user_id == user_id:
                        if is_played is None or card.is_played == is_played:
                            cards.append(card)
                except Exception:
                    continue
        return cards 
