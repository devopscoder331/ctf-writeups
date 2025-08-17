#!/usr/bin/env python3
"""
Simplified 5D Minesweeper API Server

Single FastAPI application with direct endpoints and simplified architecture.
Follows KISS, DRY, and YAGNI principles by removing unnecessary adapter layers
and complex session management.
"""

import logging
import uuid
from typing import List, Tuple, Optional
import uvicorn

from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect, Request, Query, Response
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from pydantic import BaseModel, Field

try:
    from minesweeper.config import (
        BOARD_SIZE, MINE_COUNT, HOST, PORT, CORS_ORIGINS,
        TOTAL_CELLS, MAX_NEIGHBORS, MINE_DENSITY
    )
    from minesweeper.game_logic import (
        get_or_create_session, restart_session, reveal_cell,
        get_game_stats, cleanup_old_sessions, get_session_count, get_all_mine_coords,
        toggle_flag_cell
    )
except ImportError:
    from minesweeper.config import (
        BOARD_SIZE, MINE_COUNT, HOST, PORT, CORS_ORIGINS,
        TOTAL_CELLS, MAX_NEIGHBORS, MINE_DENSITY
    )
    from minesweeper.game_logic import (
        get_or_create_session, restart_session, reveal_cell,
        get_game_stats, cleanup_old_sessions, get_session_count, get_all_mine_coords,
        toggle_flag_cell
    )

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Simple Pydantic models
class Coordinates(BaseModel):
    x: int = Field(..., ge=0, lt=BOARD_SIZE)
    y: int = Field(..., ge=0, lt=BOARD_SIZE)
    z: int = Field(..., ge=0, lt=BOARD_SIZE)
    w: int = Field(..., ge=0, lt=BOARD_SIZE)
    v: int = Field(..., ge=0, lt=BOARD_SIZE)



# Create FastAPI app
app = FastAPI(
    title="5D Minesweeper API",
    description=f"""
    ## 5D Minesweeper CTF Challenge

    ### Game Rules
    - **Board Size**: {BOARD_SIZE}×{BOARD_SIZE}×{BOARD_SIZE}×{BOARD_SIZE}×{BOARD_SIZE} = {TOTAL_CELLS:,} cells
    - **Mine Count**: {MINE_COUNT} mines ({MINE_DENSITY:.1%} density)
    - **Max Neighbors**: {MAX_NEIGHBORS} neighbors per cell

    ### Quick Start
    ```bash
    # Restart game
    curl -X POST "http://localhost:{PORT}/restart"

    # Reveal cells
    curl "http://localhost:{PORT}/reveal?x=0&y=0&z=0&w=0&v=0"
    ```
    """,
    version="2.0.0",
    docs_url="/docs"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# WebSocket connections
websocket_connections = {}

def get_client_id(request: Request, response: Optional[Response] = None) -> str:
    """Get or create client session ID using cookies."""
    # Check for existing session cookie
    session_id = request.cookies.get("minesweeper_session")

    if session_id:
        return session_id

    # Generate new session ID
    new_session_id = str(uuid.uuid4())

    # Set cookie if response object is available (HTTP requests)
    if response:
        response.set_cookie(
            key="minesweeper_session",
            value=new_session_id,
            max_age=86400,  # 24 hours
            httponly=True,  # Security: prevent JS access
            samesite="lax"  # CSRF protection
        )

    return new_session_id

@app.get("/api/health")
async def health_check():
    """Health check endpoint."""
    return {
        "status": "healthy",
        "game_config": {
            "board_size": BOARD_SIZE,
            "mine_count": MINE_COUNT,
            "total_cells": TOTAL_CELLS
        },
        "active_sessions": get_session_count()
    }

@app.post("/api/restart")
async def restart_game(request: Request, response: Response):
    """Restart game with new board."""
    client_id = get_client_id(request, response)

    try:
        session = restart_session(client_id)
        return {
            "success": True,
            "message": "Game restarted successfully",
            "board_size": BOARD_SIZE,
            "mine_count": MINE_COUNT,
            "generation_time": session['metadata']['generation_time']
        }
    except Exception as e:
        logger.error(f"Restart failed for {client_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/reveal")
async def reveal_cell_endpoint(
    request: Request,
    response: Response,
    x: int = Query(..., ge=0, lt=BOARD_SIZE),
    y: int = Query(..., ge=0, lt=BOARD_SIZE),
    z: int = Query(..., ge=0, lt=BOARD_SIZE),
    w: int = Query(..., ge=0, lt=BOARD_SIZE),
    v: int = Query(..., ge=0, lt=BOARD_SIZE)
):
    """Reveal a cell with traditional minesweeper mechanics."""
    client_id = get_client_id(request, response)

    try:
        session = get_or_create_session(client_id)
        result = reveal_cell(session, x, y, z, w, v)

        # Handle mine hit response
        if result.get('is_mine', False):
            return {
                "is_mine": True,
                "game_over": True,
                "game_state": result['game_state'],
                "coordinates": result['coordinates'],
                "message": result['message'],
                "total_reveals": result['total_reveals'],
                "mine_hit_coords": result['mine_hit_coords']
            }

        # Handle safe cell response
        response_data = {
            "is_mine": False,
            "count": result['neighbor_count'],
            "game_state": result['game_state'],
            "coordinates": result['coordinates'],
            "auto_revealed": result['auto_revealed'],
            "cascade_size": result['cascade_size'],
            "total_reveals": result['total_reveals']
        }

        # Add win message if game won
        if result['game_state'] == 'WON':
            response_data['message'] = result['message']
            response_data['total_safe_cells'] = result['total_safe_cells']
            response_data['flag'] = result.get('flag')

        return response_data

    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Reveal failed for {client_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))



@app.get("/api/stats")
async def get_stats_endpoint(request: Request, response: Response):
    """Get current game statistics."""
    client_id = get_client_id(request, response)

    try:
        session = get_or_create_session(client_id)
        return get_game_stats(session)
    except Exception as e:
        logger.error(f"Stats failed for {client_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/mines")
async def get_mine_locations_endpoint(request: Request, response: Response):
    """Get all mine locations (only available when game is over)."""
    client_id = get_client_id(request, response)

    try:
        session = get_or_create_session(client_id)

        # Only allow mine locations when game is over
        if session['game_state'] != 'LOST':
            raise HTTPException(status_code=403, detail="Mine locations only available when game is over")

        mine_coords = get_all_mine_coords(session)

        return {
            "success": True,
            "game_state": session['game_state'],
            "mine_locations": mine_coords,
            "total_mines": len(mine_coords)
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Mine locations failed for {client_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    """WebSocket endpoint for real-time game interaction."""
    await websocket.accept()

    # Extract session from WebSocket cookies
    cookies = websocket.cookies
    client_id = cookies.get("minesweeper_session")

    if not client_id:
        # Generate new session for WebSocket-only clients
        client_id = str(uuid.uuid4())

    websocket_connections[client_id] = websocket

    logger.info(f"WebSocket connected: {client_id}")

    try:
        while True:
            data = await websocket.receive_json()
            message_type = data.get("type")
            message_data = data.get("data", {})

            try:
                if message_type == "reveal":
                    coords = (message_data["x"], message_data["y"], message_data["z"],
                             message_data["w"], message_data["v"])
                    logger.info(f"🎯 Client {client_id} requesting reveal for cell {coords}")

                    session = get_or_create_session(client_id)

                    # Log current session state before reveal
                    revealed_count = len(session['revealed_cells'])
                    logger.info(f"📊 Session state before reveal: {revealed_count} cells revealed, "
                               f"game_state={session['game_state']}")

                    # Check if cell is already revealed (debugging sync issues)
                    if coords in session['revealed_cells']:
                        logger.warning(f"🚨 SYNC ISSUE: Cell {coords} already in revealed_cells set")

                    result = reveal_cell(
                        session,
                        message_data["x"], message_data["y"], message_data["z"],
                        message_data["w"], message_data["v"]
                    )

                    # Log result details
                    logger.info(f"✅ Reveal result for {coords}: mine={result.get('is_mine', False)}, "
                               f"cascade_size={result.get('cascade_size', 0)}, "
                               f"total_reveals={result.get('total_reveals', 0)}")

                    # Handle mine hit
                    if result.get('is_mine', False):
                        await websocket.send_json({
                            "type": "mine_hit",
                            "data": {
                                "is_mine": True,
                                "game_over": True,
                                "game_state": result['game_state'],
                                "coordinates": result['coordinates'],
                                "message": result['message'],
                                "total_reveals": result['total_reveals'],
                                "mine_hit_coords": result['mine_hit_coords']
                            }
                        })
                    else:
                        # Handle safe cell reveal
                        response_data = {
                            "is_mine": False,
                            "count": result['neighbor_count'],
                            "game_state": result['game_state'],
                            "coordinates": result['coordinates'],
                            "auto_revealed": result['auto_revealed'],
                            "cascade_size": result['cascade_size'],
                            "total_reveals": result['total_reveals']
                        }

                        # Add win message if game won
                        if result['game_state'] == 'WON':
                            response_data['message'] = result['message']
                            response_data['total_safe_cells'] = result['total_safe_cells']
                            response_data['flag'] = result.get('flag')

                        logger.info(f"📤 Sending cell_revealed response for {coords}")
                        await websocket.send_json({
                            "type": "cell_revealed",
                            "data": response_data
                        })

                        # Send cascade reveal event if there were auto-reveals
                        if result['cascade_size'] > 0:
                            logger.info(f"🌊 Sending cascade_reveal for {result['cascade_size']} auto-revealed cells")
                            await websocket.send_json({
                                "type": "cascade_reveal",
                                "data": {
                                    "auto_revealed": result['auto_revealed'],
                                    "cascade_size": result['cascade_size']
                                }
                            })

                        # Send game won event if game is won
                        if result['game_state'] == 'WON':
                            await websocket.send_json({
                                "type": "game_won",
                                "data": {
                                    "message": result['message'],
                                    "total_safe_cells": result['total_safe_cells'],
                                    "total_reveals": result['total_reveals'],
                                    "flag": result.get('flag')
                                }
                            })



                elif message_type == "restart":
                    session = restart_session(client_id)
                    await websocket.send_json({
                        "type": "game_restarted",
                        "data": {"message": "Game restarted successfully"}
                    })

                elif message_type == "stats":
                    session = get_or_create_session(client_id)
                    stats = get_game_stats(session)
                    await websocket.send_json({
                        "type": "stats",
                        "data": stats
                    })

                elif message_type == "flag":
                    session = get_or_create_session(client_id)
                    result = toggle_flag_cell(
                        session,
                        message_data["x"], message_data["y"], message_data["z"],
                        message_data["w"], message_data["v"]
                    )

                    await websocket.send_json({
                        "type": "cell_flagged",
                        "data": result
                    })

                else:
                    await websocket.send_json({
                        "type": "error",
                        "data": {"message": f"Unknown message type: {message_type}"}
                    })

            except ValueError as e:
                error_msg = str(e)
                logger.warning(f"🚨 ValueError for {client_id}: {error_msg}")

                # Special logging for "already revealed" errors to track sync issues
                if "already revealed" in error_msg:
                    session = get_or_create_session(client_id)
                    revealed_count = len(session['revealed_cells'])
                    logger.error(f"🔍 SYNC DEBUG - Session has {revealed_count} revealed cells")
                    logger.error(f"🔍 SYNC DEBUG - Last 10 revealed cells: {list(session['revealed_cells'])[-10:]}")

                await websocket.send_json({
                    "type": "error",
                    "data": {"message": error_msg}
                })
            except Exception as e:
                logger.error(f"💥 Unexpected WebSocket error for {client_id}: {e}")
                logger.error(f"💥 Error type: {type(e).__name__}")
                import traceback
                logger.error(f"💥 Traceback: {traceback.format_exc()}")
                await websocket.send_json({
                    "type": "error",
                    "data": {"message": "Internal server error"}
                })

    except WebSocketDisconnect:
        logger.info(f"WebSocket disconnected: {client_id}")
    finally:
        websocket_connections.pop(client_id, None)

@app.on_event("startup")
async def startup_event():
    """Initialize on startup."""
    logger.info("🚀 Starting 5D Minesweeper API Server")
    logger.info(f"📊 Board: {BOARD_SIZE}^5 = {TOTAL_CELLS:,} cells")
    logger.info(f"💣 Mines: {MINE_COUNT} ({MINE_DENSITY:.1%} density)")
    logger.info(f"🔗 WebSocket: ws://localhost:{PORT}/ws")
    logger.info(f"📖 Docs: http://localhost:{PORT}/docs")

@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup on shutdown."""
    logger.info("🛑 Shutting down 5D Minesweeper API Server")
    cleaned = cleanup_old_sessions(max_age_hours=0)
    logger.info(f"🧹 Cleaned up {cleaned} sessions")

if __name__ == "__main__":
    uvicorn.run(
        "unified_main:app",
        host=HOST,
        port=PORT,
        reload=True,
        log_level="info"
    )
