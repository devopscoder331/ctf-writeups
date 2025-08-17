"""
Simplified 5D Minesweeper Board Generator.

A streamlined implementation focusing on core functionality:
- Simple random mine placement without complex validation
- Basic metadata tracking
- No Z3 constraint solving (YAGNI principle)
- Fast generation suitable for CTF challenges

This replaces the over-engineered generator with Z3 validation complexity.
"""

import time
import logging
from typing import Tuple, Dict, Optional
import numpy as np
from numpy.typing import NDArray

try:
    from minesweeper.config import BOARD_SIZE, MINE_COUNT, TOTAL_CELLS
except ImportError:
    from minesweeper.config import BOARD_SIZE, MINE_COUNT, TOTAL_CELLS

logger = logging.getLogger(__name__)


def get_cross_neighbors(x: int, y: int, z: int, w: int, v: int, board_size: int = BOARD_SIZE) -> list[tuple[int, int, int, int, int]]:
    """Return 10 cross neighbors on a 5D torus using modular arithmetic."""
    coords = [x, y, z, w, v]
    neighbors = []
    for axis in range(5):
        for delta in (-1, 1):
            new_coords = coords.copy()
            new_coords[axis] = (new_coords[axis] + delta) % board_size
            neighbors.append(tuple(new_coords))
    return neighbors


def generate_random_mines(board_size: int, mine_count: int, seed: Optional[int]) -> NDArray[np.uint8]:
    """
    Generate a 5D board with randomly placed mines.

    Args:
        board_size: Size of each dimension
        mine_count: Number of mines to place
        seed: Random seed for reproducible generation

    Returns:
        5D numpy array with mines (1) and empty cells (0)
    """
    if seed is not None:
        np.random.seed(seed)

    # Create empty board
    board = np.zeros([board_size] * 5, dtype=np.uint8)

    # Select random positions for mines
    mine_positions = np.random.choice(
        board_size ** 5,
        size=mine_count,
        replace=False
    )

    # Place mines
    board.flat[mine_positions] = 1

    return board


def count_neighbors(board: NDArray[np.uint8], x: int, y: int, z: int, w: int, v: int) -> int:
    """Count mines in 10 cross neighbors on a 5D torus."""
    neighbors = get_cross_neighbors(x, y, z, w, v, board.shape[0])
    return sum(int(board[nx, ny, nz, nw, nv]) for nx, ny, nz, nw, nv in neighbors)


def validate_board_basic(board: NDArray[np.uint8], expected_mines: int) -> bool:
    """
    Basic board validation without complex Z3 constraints.

    Args:
        board: Generated board to validate
        expected_mines: Expected number of mines

    Returns:
        True if board passes basic validation
    """
    actual_mines = int(np.sum(board))

    if actual_mines != expected_mines:
        logger.warning(f"Mine count mismatch: {actual_mines} != {expected_mines}")
        return False

    return True


def create_board_metadata(generation_time: float, mine_count: int, board_size: int) -> Dict[str, any]:
    """
    Create metadata dictionary for generated board.

    Args:
        generation_time: Time taken to generate board
        mine_count: Number of mines placed
        board_size: Size of each dimension

    Returns:
        Metadata dictionary
    """
    return {
        'generation_time': generation_time,
        'mine_count': mine_count,
        'board_size': board_size,
        'total_cells': board_size ** 5,
        'mine_density': mine_count / (board_size ** 5),
        'sanity': 'none',
    }


def generate_board(seed: Optional[int] = None) -> Tuple[NDArray[np.uint8], Dict[str, any]]:
    """
    Generate a complete 5D minesweeper board with metadata.

    Args:
        seed: Optional random seed for reproducible generation

    Returns:
        Tuple of (board_array, metadata_dict)

    Raises:
        ValueError: If mine count exceeds total cells
    """
    if MINE_COUNT >= TOTAL_CELLS:
        raise ValueError(f"Mine count {MINE_COUNT} exceeds total cells {TOTAL_CELLS}")

    start_time = time.time()

    # Generate board with random mine placement
    board = generate_random_mines(
        board_size=BOARD_SIZE,
        mine_count=MINE_COUNT,
        seed=seed
    )

    # Basic validation
    if not validate_board_basic(board=board, expected_mines=MINE_COUNT):
        raise RuntimeError("Board generation failed basic validation")

    generation_time = time.time() - start_time

    # Create metadata
    metadata = create_board_metadata(
        generation_time=generation_time,
        mine_count=MINE_COUNT,
        board_size=BOARD_SIZE
    )

    logger.info(f"Generated {BOARD_SIZE}^5 board in {generation_time:.3f}s: {MINE_COUNT} mines")

    return board, metadata


# Backwards compatibility class
class SimpleBoard5DGenerator:
    """
    Simplified 5D Minesweeper board generator class.

    Maintains backwards compatibility with existing code while providing
    simplified implementation without Z3 complexity.
    """

    def __init__(self, board_size: int = BOARD_SIZE, mine_count: int = MINE_COUNT):
        self.board_size = board_size
        self.mine_count = mine_count
        self.total_cells = board_size ** 5

        # Simple stats
        self.boards_generated = 0
        self.total_generation_time = 0.0

    def generate_board(self, seed: Optional[int] = None, timeout: int = 30) -> Tuple[np.ndarray, Dict]:
        """
        Generate a 5D minesweeper board.

        Args:
            seed: Random seed for reproducible generation
            timeout: Ignored in simplified version

        Returns:
            Tuple of (board, metadata)
        """
        start_time = time.time()

        board = generate_random_mines(
            board_size=self.board_size,
            mine_count=self.mine_count,
            seed=seed
        )

        if not validate_board_basic(board, self.mine_count):
            raise RuntimeError("Board generation failed validation")

        generation_time = time.time() - start_time

        # Update stats
        self.boards_generated += 1
        self.total_generation_time += generation_time

        metadata = create_board_metadata(
            generation_time=generation_time,
            mine_count=self.mine_count,
            board_size=self.board_size
        )

        return board, metadata

    def generate_simple_board(self, seed: Optional[int] = None) -> Tuple[np.ndarray, Dict]:
        """
        Generate a simple board (alias for generate_board).

        Args:
            seed: Random seed for reproducible generation

        Returns:
            Tuple of (board, metadata)
        """
        return self.generate_board(seed=seed)

    def count_neighbors(self, board: np.ndarray, x: int, y: int, z: int, w: int, v: int) -> int:
        """Count neighbors for backwards compatibility."""
        return count_neighbors(board, x, y, z, w, v)

    def get_stats(self) -> Dict:
        """Get simple generation statistics."""
        avg_time = self.total_generation_time / max(self.boards_generated, 1)
        return {
            'boards_generated': self.boards_generated,
            'total_time': self.total_generation_time,
            'average_time': avg_time,
            'target_met': avg_time < 1.0  # Much faster target for simple generator
        }


def simple_benchmark(num_boards: int) -> Dict:
    """
    Simple benchmark function for testing board generation performance.

    Args:
        num_boards: Number of boards to generate for benchmarking

    Returns:
        Benchmark results dictionary
    """
    if num_boards <= 0:
        raise ValueError("Number of boards must be positive")

    times = []
    successful = 0

    logger.info(f"Running benchmark with {num_boards} boards...")

    for i in range(num_boards):
        try:
            start = time.time()
            board, metadata = generate_board()
            elapsed = time.time() - start

            times.append(elapsed)
            successful += 1
            logger.debug(f"Board {i+1}: {elapsed:.3f}s")

        except Exception as e:
            logger.error(f"Board {i+1} failed: {e}")

    if successful > 0:
        avg_time = sum(times) / len(times)
        return {
            'successful': successful,
            'failed': num_boards - successful,
            'average_time': avg_time,
            'total_time': sum(times),
            'times': times,
            'target_met': avg_time < 1.0  # Much faster target for simple generator
        }
    else:
        return {
            'successful': 0,
            'failed': num_boards,
            'average_time': 0.0,
            'total_time': 0.0,
            'times': [],
            'target_met': False
        }


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)

    # Test generation
    try:
        board, metadata = generate_board()
        print(f"Generated board: {metadata}")

        # Run benchmark
        results = simple_benchmark(3)
        print(f"Benchmark results: {results}")

    except Exception as e:
        print(f"Error: {e}")
