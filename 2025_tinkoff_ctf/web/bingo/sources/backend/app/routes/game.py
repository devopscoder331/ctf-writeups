from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.models.bingo_card import BingoCard
import random
import os
import pickle
import base64

bp = Blueprint('game', __name__, url_prefix='/api/game')


def validate_bingo_card(numbers):
    if not isinstance(numbers, list) or len(numbers) != 5:
        return False

    valid_numbers = []
    star_count = 0

    for i, row in enumerate(numbers):
        if not isinstance(row, list) or len(row) != 5:
            return False
        for j, num in enumerate(row):
            if num == "*":
                if i != 2 or j != 2:
                    return False
                star_count += 1
                if star_count > 1:
                    return False
            else:
                if not isinstance(num, int) or num < 1 or num > 75:
                    return False
                valid_numbers.append(num)

    if numbers[2][2] != "*":
        return False

    if len(set(valid_numbers)) != len(valid_numbers):
        return False

    return True

def check_win(card_numbers, drawn_numbers):
    drawn_set = set(drawn_numbers)
    
    for row in card_numbers:
        if all(num in drawn_set or num == "*" for num in row):
            return True
            
    for col in range(5):
        if all(card_numbers[row][col] in drawn_set or card_numbers[row][col] == "*" 
              for row in range(5)):
            return True
            
    if all(card_numbers[i][i] in drawn_set or card_numbers[i][i] == "*"
          for i in range(5)):
        return True
    if all(card_numbers[i][4-i] in drawn_set or card_numbers[i][4-i] == "*" 
          for i in range(5)):
        return True
        
    return False

@bp.route('/card', methods=['POST'])
@jwt_required()
def create_card():
    data = request.get_json()
    numbers = data.get('numbers')
    background_image = data.get('background_image')
    magic_mantras = data.get('magic_mantras')
    user_id = get_jwt_identity()
    
    if not validate_bingo_card(numbers):
        return jsonify({'error': 'Invalid BINGO card format'}), 400
    
    card = BingoCard(user_id=user_id)
    card.set_numbers(numbers)
    
    filepath = os.path.join(BingoCard.get_storage_path(user_id), f'card_{card.id}.pickle')
    open(filepath, 'w').close()
    
    if background_image:
        card.set_background(background_image)
        
    if magic_mantras:
        card.set_magic_mantras(magic_mantras)
    
    pickle.dump(card, open(filepath, 'r+b'))
    
    return jsonify(card.to_dict()), 201

@bp.route('/card/<card_id>/background', methods=['PUT'])
@jwt_required()
def update_card_background(card_id):
    user_id = get_jwt_identity()
    filepath = os.path.join(BingoCard.get_storage_path(user_id), f'card_{card_id}.pickle')
    
    try:
        card = pickle.load(open(filepath, 'r+b'))
        if not card or card.user_id != user_id:
            return jsonify({'error': 'Unauthorized'}), 403
    except Exception:
        return jsonify({'error': 'Card not found'}), 404

    data = request.get_json()
    background_image = data.get('background_image')
    
    if not background_image:
        return jsonify({'error': 'Background image is required'}), 400

    card.set_background(background_image)
    
    pickle.dump(card, open(filepath, 'r+b'))
    
    return jsonify(card.to_dict())

@bp.route('/card/<card_id>/mantras', methods=['PUT'])
@jwt_required()
def update_card_mantras(card_id):
    user_id = get_jwt_identity()
    filepath = os.path.join(BingoCard.get_storage_path(user_id), f'card_{card_id}.pickle')
    
    try:
        card = pickle.load(open(filepath, 'r+b'))
        if not card or card.user_id != user_id:
            return jsonify({'error': 'Unauthorized'}), 403
    except Exception:
        return jsonify({'error': 'Card not found'}), 404

    data = request.get_json()
    magic_mantras = data.get('magic_mantras')
    
    if not magic_mantras:
        return jsonify({'error': 'Magic mantras are required'}), 400

    card.set_magic_mantras(magic_mantras)
    
    pickle.dump(card, open(filepath, 'r+b'))
    
    return jsonify(card.to_dict())

@bp.route('/card/<card_id>/background', methods=['GET'])
@jwt_required()
def get_card_background(card_id):
    user_id = get_jwt_identity()
    filepath = os.path.join(BingoCard.get_storage_path(user_id), f'card_{card_id}.pickle')
    
    try:
        card = pickle.load(open(filepath, 'r+b'))
        if not card or card.user_id != user_id:
            return jsonify({'error': 'Unauthorized'}), 403
        background_image = 'data:;base64,' + base64.b64encode(card.background_data).decode().replace('\n', '') if card.background_data else None
        return jsonify({
            'background_image': background_image,
            'background_size': card.background_size
        })
    except Exception:
        return jsonify({'error': 'Card not found'}), 404

@bp.route('/cards', methods=['GET'])
@jwt_required()
def get_user_cards():
    user_id = get_jwt_identity()
    cards = BingoCard.get_all_by_user(user_id=user_id, is_played=False)
    return jsonify([card.to_dict() for card in cards])

@bp.route('/play/<card_id>', methods=['POST'])
@jwt_required()
def play_game(card_id):
    user_id = get_jwt_identity()
    card = BingoCard.load(card_id)
    
    if not card:
        return jsonify({'error': 'Card not found'}), 404
    
    if card.user_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403
        
    if card.is_completed:
        return jsonify({'error': 'Game already completed'}), 400

    if card.is_played:
        return jsonify({'error': 'Card has already been played'}), 400

    if not card.winning_numbers:
        numbers = list(range(1, 76))
        random.shuffle(numbers)
        card.set_winning_numbers(numbers[:25])
    card_numbers = card.get_numbers()
    winning_numbers = card.get_winning_numbers()
    
    if check_win(card_numbers, winning_numbers):
        card.save()
        return jsonify({
            'status': 'win',
            'card': card.to_dict(),
            'drawn_numbers': winning_numbers
        })
    card.is_completed = True
    card.save()

    return jsonify({
        'status': 'ongoing',
        'card': card.to_dict(),
        'drawn_numbers': winning_numbers
    })

@bp.route('/apply/<card_id>', methods=['POST'])
@jwt_required()
def apply_card(card_id):
    user_id = get_jwt_identity()
    card = BingoCard.load(card_id)
    
    if not card:
        return jsonify({'error': 'Card not found'}), 404
    
    if card.user_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403

    if not card.winning_numbers:
        return jsonify({'error': 'Card has not been played yet'}), 400

    card.is_played = True
    card.save()
    
    return jsonify({'status': 'success'}) 
