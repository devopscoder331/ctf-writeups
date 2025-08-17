"""
Stateless 5D Minesweeper Game Logic Core.

This module provides pure functions for game logic without complex state management.
Follows KISS, DRY, and YAGNI principles by using simple types and avoiding over-engineering.
"""

import hashlib
import hmac
import time
from typing import Dict, List, Tuple, Optional, Set, Any
import numpy as np
from numpy.typing import NDArray

try:
    from minesweeper.config import BOARD_SIZE, MINE_COUNT, SECRET_KEY, MAX_NEIGHBORS, CTF_FLAG
    from minesweeper.generator import generate_board, count_neighbors, get_cross_neighbors
except ImportError:
    from minesweeper.config import BOARD_SIZE, MINE_COUNT, SECRET_KEY, MAX_NEIGHBORS, CTF_FLAG
    from minesweeper.generator import generate_board, count_neighbors, get_cross_neighbors

# Type aliases for clarity
Coordinates = Tuple[int, int, int, int, int]
GameBoard = NDArray[np.uint8]

# Game state constants
GAME_PLAYING = 'PLAYING'
GAME_WON = 'WON'
GAME_LOST = 'LOST'


def validate_coordinates(x: int, y: int, z: int, w: int, v: int) -> bool:
    """
    Validate that coordinates are within board bounds.

    Args:
        x, y, z, w, v: 5D coordinates

    Returns:
        True if coordinates are valid
    """
    return all(0 <= coord < BOARD_SIZE for coord in [x, y, z, w, v])


def create_game_session(seed: Optional[int] = None) -> Dict[str, Any]:
    """
    Create a new game session with fresh board.

    Args:
        seed: Optional random seed for reproducible boards

    Returns:
        Game session dictionary
    """
    board, metadata = generate_board(seed=seed)

    return {
        'board': board,
        'revealed_cells': set(),
        'flagged_cells': set(),
        'game_state': GAME_PLAYING,
        'mine_hit_coords': None,
        'created_at': time.time(),
        'reveals_used': 0,
        'metadata': metadata
    }



def cascade_reveal_5d(game_session: Dict[str, Any], start_coords: Coordinates) -> List[Tuple[Coordinates, int]]:
    """
    Perform cascade reveal for cells with 0 neighbors in 5D space.

    Args:
        game_session: Current game session
        start_coords: Starting coordinates for cascade

    Returns:
        List of (coordinates, neighbor_count) tuples that were auto-revealed
    """
    board = game_session['board']
    revealed_cells = game_session['revealed_cells']

    queue = [start_coords]
    auto_revealed = []
    processed = set()

    while queue:
        current = queue.pop(0)

        if current in processed or current in revealed_cells:
            continue

        processed.add(current)
        x, y, z, w, v = current

        # Count neighbors for current cell
        neighbor_count = count_neighbors(board, x, y, z, w, v)

        # Add to revealed cells and auto-revealed list
        revealed_cells.add(current)
        auto_revealed.append((current, neighbor_count))

        # If no neighboring mines, add unrevealed neighbors to queue
        if neighbor_count == 0:
            neighbors = get_cross_neighbors(x, y, z, w, v)
            for neighbor in neighbors:
                nx, ny, nz, nw, nv = neighbor
                if board[nx, ny, nz, nw, nv]:
                    continue
                if neighbor not in revealed_cells and neighbor not in processed:
                    queue.append(neighbor)

    return auto_revealed


def cascade_reveal_neighbors_only(game_session: Dict[str, Any], start_coords: Coordinates) -> List[Tuple[Coordinates, int]]:
    """
    Perform cascade reveal for neighbors only, excluding the start coordinate.

    Args:
        game_session: Current game session
        start_coords: Starting coordinates (already revealed manually)

    Returns:
        List of (coordinates, neighbor_count) tuples that were auto-revealed (excluding start)
    """
    board = game_session['board']
    revealed_cells = game_session['revealed_cells']

    # Start with neighbors of the manually clicked cell
    initial_neighbors = get_cross_neighbors(*start_coords)
    queue = [coord for coord in initial_neighbors if coord not in revealed_cells]
    auto_revealed = []
    processed = set()

    while queue:
        current = queue.pop(0)

        if current in processed or current in revealed_cells:
            continue

        processed.add(current)
        x, y, z, w, v = current

        # Count neighbors for current cell
        neighbor_count = count_neighbors(board, x, y, z, w, v)

        # Add to revealed cells and auto-revealed list
        revealed_cells.add(current)
        auto_revealed.append((current, neighbor_count))

        # If no neighboring mines, add unrevealed neighbors to queue
        if neighbor_count == 0:
            neighbors = get_cross_neighbors(x, y, z, w, v)
            for neighbor in neighbors:
                nx, ny, nz, nw, nv = neighbor
                if board[nx, ny, nz, nw, nv]:
                    continue
                if neighbor not in revealed_cells and neighbor not in processed:
                    queue.append(neighbor)

    return auto_revealed


def check_win_condition(game_session: Dict[str, Any]) -> bool:
    """
    Check if player has won the game.

    Args:
        game_session: Current game session

    Returns:
        True if all non-mine cells are revealed
    """
    total_safe_cells = BOARD_SIZE ** 5 - MINE_COUNT
    revealed_count = len(game_session['revealed_cells'])
    return revealed_count >= total_safe_cells


def toggle_flag_cell(game_session: Dict[str, Any], x: int, y: int, z: int, w: int, v: int) -> Dict[str, Any]:
    """
    Toggle flag state for a cell.

    Args:
        game_session: Current game session
        x, y, z, w, v: Cell coordinates

    Returns:
        Response dictionary with flag status

    Raises:
        ValueError: If coordinates invalid or game over
    """
    # Validate coordinates
    if not validate_coordinates(x, y, z, w, v):
        raise ValueError("Invalid coordinates")

    # Check game state
    if game_session['game_state'] != GAME_PLAYING:
        raise ValueError("Cannot flag cells - game is over")

    coords = (x, y, z, w, v)

    # Toggle flag state
    flagged_cells = game_session['flagged_cells']
    if coords in flagged_cells:
        flagged_cells.remove(coords)
        is_flagged = False
        action = "unflagged"
    else:
        flagged_cells.add(coords)
        is_flagged = True
        action = "flagged"

    return {
        'is_flagged': is_flagged,
        'action': action,
        'coordinates': {'x': x, 'y': y, 'z': z, 'w': w, 'v': v},
        'total_flagged': len(flagged_cells)
    }


def reveal_cell(game_session: Dict[str, Any], x: int, y: int, z: int, w: int, v: int) -> Dict[str, Any]:
    """
    Reveal a cell with traditional minesweeper mechanics.

    Args:
        game_session: Current game session
        x, y, z, w, v: Cell coordinates to reveal

    Returns:
        Result dictionary with game state and reveal information

    Raises:
        ValueError: If coordinates invalid, cell already revealed, or game over
    """
    # Check if game is over
    if game_session['game_state'] != GAME_PLAYING:
        raise ValueError(f"Game is over (state: {game_session['game_state']}). Please restart to play again.")

    # Validate coordinates
    if not validate_coordinates(x, y, z, w, v):
        raise ValueError(f"Invalid coordinates: ({x}, {y}, {z}, {w}, {v})")

    coords = (x, y, z, w, v)

    # Check if already revealed
    if coords in game_session['revealed_cells']:
        raise ValueError(f"Cell {coords} already revealed")


    # Check for mine hit FIRST
    board = game_session['board']
    is_mine = bool(board[x, y, z, w, v])

    if is_mine:
        # Mine hit - game over
        game_session['game_state'] = GAME_LOST
        game_session['mine_hit_coords'] = coords
        game_session['revealed_cells'].add(coords)
        game_session['reveals_used'] += 1

        return {
            'is_mine': True,
            'game_over': True,
            'game_state': GAME_LOST,
            'coordinates': {'x': x, 'y': y, 'z': z, 'w': w, 'v': v},
            'message': 'Game Over - Mine Hit!',
            'total_reveals': game_session['reveals_used'],
            'mine_hit_coords': {'x': x, 'y': y, 'z': z, 'w': w, 'v': v}
        }

    # Safe cell - count neighbors
    neighbor_count = count_neighbors(board, x, y, z, w, v)

    # Update session state for manual reveal
    game_session['revealed_cells'].add(coords)
    game_session['reveals_used'] += 1

    # Auto-reveal cascade if no neighboring mines
    auto_revealed = []
    if neighbor_count == 0:
        # FIXED: Use neighbors-only cascade to avoid remove/re-add synchronization issue
        cascade_results = cascade_reveal_neighbors_only(game_session, coords)
        auto_revealed = [
            {
                'coords': {'x': c[0], 'y': c[1], 'z': c[2], 'w': c[3], 'v': c[4]},
                'count': count
            }
            for c, count in cascade_results
        ]

    # Check win condition
    if check_win_condition(game_session):
        game_session['game_state'] = GAME_WON

    # Prepare response
    response = {
        'is_mine': False,
        'neighbor_count': neighbor_count,
        'game_state': game_session['game_state'],
        'coordinates': {'x': x, 'y': y, 'z': z, 'w': w, 'v': v},
        'auto_revealed': auto_revealed,
        'cascade_size': len(auto_revealed),
        'total_reveals': game_session['reveals_used']
    }

    # Add win message if game won
    if game_session['game_state'] == GAME_WON:
        response['message'] = 'Congratulations! All mines found!'
        response['total_safe_cells'] = len(game_session['revealed_cells'])
        response['flag'] = CTF_FLAG

    return response


def get_mine_coordinates(board: GameBoard) -> List[Coordinates]:
    """
    Extract all mine coordinates from board.

    Args:
        board: Game board array

    Returns:
        List of coordinates where mines are located
    """
    mine_positions = np.where(board == 1)
    return list(zip(*mine_positions))





def get_game_stats(game_session: Dict[str, Any]) -> Dict[str, Any]:
    """
    Get current game statistics.

    Args:
        game_session: Current game session

    Returns:
        Statistics dictionary
    """
    session_age = time.time() - game_session['created_at']

    return {
        'revealed_cells': len(game_session['revealed_cells']),
        'total_cells': BOARD_SIZE ** 5,
        'mine_count': MINE_COUNT,
        'max_neighbors': MAX_NEIGHBORS,
        'reveals_used': game_session['reveals_used'],
        'session_age_seconds': session_age,
        'board_metadata': game_session['metadata']
    }


def is_mine_at(game_session: Dict[str, Any], x: int, y: int, z: int, w: int, v: int) -> bool:
    """
    Check if there's a mine at given coordinates (for testing/admin purposes).

    Args:
        game_session: Current game session
        x, y, z, w, v: Coordinates to check

    Returns:
        True if mine exists at coordinates
    """
    if not validate_coordinates(x, y, z, w, v):
        return False

    return bool(game_session['board'][x, y, z, w, v])


# Simple in-memory storage for stateless approach
_active_sessions: Dict[str, Dict[str, Any]] = {}


def get_or_create_session(client_id: str) -> Dict[str, Any]:
    """
    Get existing session or create new one for client.

    Args:
        client_id: Unique client identifier

    Returns:
        Game session dictionary
    """
    if client_id not in _active_sessions:
        _active_sessions[client_id] = create_game_session()

    return _active_sessions[client_id]


def restart_session(client_id: str, seed: Optional[int] = None) -> Dict[str, Any]:
    """
    Restart game session with new board.

    Args:
        client_id: Client identifier
        seed: Optional seed for reproducible board

    Returns:
        New game session dictionary
    """
    _active_sessions[client_id] = create_game_session(seed=seed)
    return _active_sessions[client_id]


def get_all_mine_coords(game_session: Dict[str, Any]) -> List[Dict[str, int]]:
    """
    Get all mine coordinates in API format (for game over display).

    Args:
        game_session: Current game session

    Returns:
        List of mine coordinates in API format
    """
    mine_coords = get_mine_coordinates(game_session['board'])
    return [
        {'x': int(x), 'y': int(y), 'z': int(z), 'w': int(w), 'v': int(v)}
        for x, y, z, w, v in mine_coords
    ]


def cleanup_old_sessions(max_age_hours: int = 24) -> int:
    """
    Remove sessions older than specified age.

    Args:
        max_age_hours: Maximum session age in hours

    Returns:
        Number of sessions cleaned up
    """
    current_time = time.time()
    max_age_seconds = max_age_hours * 3600

    old_sessions = [
        client_id for client_id, session in _active_sessions.items()
        if current_time - session['created_at'] > max_age_seconds
    ]

    for client_id in old_sessions:
        del _active_sessions[client_id]

    return len(old_sessions)


def get_session_count() -> int:
    """Get current number of active sessions."""
    return len(_active_sessions)
