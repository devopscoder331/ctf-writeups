#!/usr/bin/env python3
"""
Test script to verify cookie-based session management for the minesweeper backend.
This tests both HTTP endpoints and WebSocket connections.
"""

import requests
import json
import time
import uuid
from websocket import create_connection
from typing import Dict, Any

BASE_URL = "http://localhost:5000"
WS_URL = "ws://localhost:5000/ws"

def test_http_session_persistence():
    """Test that HTTP sessions persist using cookies."""
    print("\n=== Testing HTTP Session Persistence ===")

    # Create a session to maintain cookies
    session = requests.Session()

    # First request - should create a new session
    response1 = session.post(f"{BASE_URL}/api/restart")
    print(f"First restart response: {response1.status_code}")
    print(f"Cookies after first request: {session.cookies}")

    # Check if we got a session cookie
    session_cookie = session.cookies.get('minesweeper_session')
    if session_cookie:
        print(f"✅ Session cookie created: {session_cookie}")
    else:
        print("❌ No session cookie found!")
        return False

    # Second request - should use the same session
    response2 = session.get(f"{BASE_URL}/api/stats")
    print(f"Stats response: {response2.status_code}")

    # Verify the same cookie is used
    if session.cookies.get('minesweeper_session') == session_cookie:
        print("✅ Session cookie persisted across requests")
    else:
        print("❌ Session cookie changed!")
        return False

    return True

def test_different_clients_different_sessions():
    """Test that different clients get different sessions."""
    print("\n=== Testing Different Clients Get Different Sessions ===")

    # Client 1
    session1 = requests.Session()
    response1 = session1.post(f"{BASE_URL}/api/restart")
    cookie1 = session1.cookies.get('minesweeper_session')

    # Client 2
    session2 = requests.Session()
    response2 = session2.post(f"{BASE_URL}/api/restart")
    cookie2 = session2.cookies.get('minesweeper_session')

    print(f"Client 1 session: {cookie1}")
    print(f"Client 2 session: {cookie2}")

    if cookie1 and cookie2 and cookie1 != cookie2:
        print("✅ Different clients get different sessions")
        return True
    else:
        print("❌ Clients got the same session or no sessions!")
        return False

def test_websocket_connection():
    """Test WebSocket connection works with cookie-based sessions."""
    print("\n=== Testing WebSocket Connection ===")

    try:
        # First establish HTTP session to get a cookie
        session = requests.Session()
        session.post(f"{BASE_URL}/api/restart")
        session_cookie = session.cookies.get('minesweeper_session')

        if not session_cookie:
            print("❌ Could not establish HTTP session")
            return False

        print(f"HTTP session established: {session_cookie}")

        # Create WebSocket connection with cookie
        # Note: websocket-client library automatically handles cookies from the same domain
        headers = {"Cookie": f"minesweeper_session={session_cookie}"}
        ws = create_connection(WS_URL, header=headers, timeout=5)

        print("✅ WebSocket connected successfully")

        # Send a test message
        test_message = {
            "type": "stats",
            "data": {}
        }
        ws.send(json.dumps(test_message))

        # Wait for response
        response = ws.recv()
        response_data = json.loads(response)

        print(f"WebSocket response: {response_data}")

        ws.close()
        print("✅ WebSocket communication successful")
        return True

    except Exception as e:
        print(f"❌ WebSocket test failed: {e}")
        return False

def test_reveal_functionality():
    """Test that the reveal functionality works with sessions."""
    print("\n=== Testing Reveal Functionality ===")

    session = requests.Session()

    # Restart game
    restart_response = session.post(f"{BASE_URL}/api/restart")
    if restart_response.status_code != 200:
        print(f"❌ Failed to restart game: {restart_response.status_code}")
        return False

    print("✅ Game restarted successfully")

    # Try to reveal a cell
    reveal_response = session.get(f"{BASE_URL}/api/reveal", params={
        "x": 0, "y": 0, "z": 0, "w": 0, "v": 0
    })

    if reveal_response.status_code == 200:
        result = reveal_response.json()
        print(f"✅ Cell revealed: {result}")
        return True
    else:
        print(f"❌ Failed to reveal cell: {reveal_response.status_code}")
        return False

def test_session_isolation():
    """Test that sessions are properly isolated between clients."""
    print("\n=== Testing Session Isolation ===")

    # Client 1 - restart and reveal a cell
    session1 = requests.Session()
    session1.post(f"{BASE_URL}/api/restart")
    reveal1 = session1.get(f"{BASE_URL}/api/reveal", params={
        "x": 0, "y": 0, "z": 0, "w": 0, "v": 0
    })
    stats1_before = session1.get(f"{BASE_URL}/api/stats").json()

    # Client 2 - restart game (should not affect client 1)
    session2 = requests.Session()
    session2.post(f"{BASE_URL}/api/restart")

    # Check client 1's stats again - should be unchanged
    stats1_after = session1.get(f"{BASE_URL}/api/stats").json()

    if stats1_before == stats1_after:
        print("✅ Sessions properly isolated - client 2 restart didn't affect client 1")
        return True
    else:
        print("❌ Session isolation failed")
        print(f"Stats before: {stats1_before}")
        print(f"Stats after: {stats1_after}")
        return False

def main():
    """Run all tests."""
    print("🧪 Testing Cookie-Based Session Management")
    print("=" * 50)

    tests = [
        test_http_session_persistence,
        test_different_clients_different_sessions,
        test_reveal_functionality,
        test_session_isolation,
        test_websocket_connection,
    ]

    passed = 0
    total = len(tests)

    for test in tests:
        try:
            if test():
                passed += 1
            time.sleep(0.5)  # Small delay between tests
        except Exception as e:
            print(f"❌ Test {test.__name__} failed with exception: {e}")

    print("\n" + "=" * 50)
    print(f"🏁 Test Results: {passed}/{total} tests passed")

    if passed == total:
        print("🎉 All tests passed! Cookie-based sessions are working correctly.")
        return True
    else:
        print("❌ Some tests failed. Check the implementation.")
        return False

if __name__ == "__main__":
    main()
