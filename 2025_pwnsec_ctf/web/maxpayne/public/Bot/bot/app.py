from flask import Flask, request, jsonify, render_template
import subprocess
import os
import threading
import time
from pathlib import Path
import re

app = Flask(__name__)

# Configuration from environment variables
APP_NAME = os.getenv('APPNAME', 'XSS Bot')
APP_URL = os.getenv('APPURL', 'http://localhost:5000')
APP_URL_REGEX = os.getenv('APPURLREGEX', r'^http://localhost:5000/view_note/')
APP_LIMIT = int(os.getenv('APPLIMIT', 5))
APP_LIMIT_TIME = int(os.getenv('APPLIMITTIME', 60))

# Rate limiting storage
visit_log = []
lockfile = Path("bot4.lock")

def cleanup_old_visits():
    """Remove visits older than APP_LIMIT_TIME seconds"""
    global visit_log
    current_time = time.time()
    visit_log = [visit_time for visit_time in visit_log if current_time - visit_time < APP_LIMIT_TIME]

def is_rate_limited():
    """Check if we're currently rate limited"""
    cleanup_old_visits()
    return len(visit_log) >= APP_LIMIT

def add_visit():
    """Add current visit to the log"""
    visit_log.append(time.time())

def is_valid_url(url):
    """Validate URL against the configured regex"""
    try:
        return bool(re.match(APP_URL_REGEX, url))
    except re.error:
        return False

@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == 'GET':
        cleanup_old_visits()
        return render_template('index.html',
            app_name=APP_NAME,
            app_url=APP_URL,
            url_pattern=APP_URL_REGEX,
            rate_limit=f'{APP_LIMIT} visits per {APP_LIMIT_TIME} seconds',
            current_visits=len(visit_log),
            locked=lockfile.exists()
        )
    
    # Handle POST request for bot visit
    url = request.form.get('url')
    
    if not url:
        return jsonify({'success': False, 'error': 'URL parameter is required'}), 400
    
    # Validate URL format
    if not is_valid_url(url):
        return jsonify({
            'success': False, 
            'error': f'Invalid URL format. URL must match pattern: {APP_URL_REGEX}'
        }), 400
    
    # Check rate limiting
    if is_rate_limited():
        return jsonify({
            'success': False,
            'error': f'Rate limit exceeded. Maximum {APP_LIMIT} visits per {APP_LIMIT_TIME} seconds'
        }), 429
    
    # Check if bot is already running
    if lockfile.exists():
        return jsonify({
            'success': False,
            'error': 'Bot is currently busy. Please try again later.'
        }), 503
    
    # Add visit to rate limit log
    add_visit()
    
    # Start bot visit in background
    def run_bot():
        try:
            result = subprocess.run(
                ['python3', 'bot.py', url],
                capture_output=True,
                text=True,
                timeout=60,  # 60 second timeout
                cwd=os.path.dirname(os.path.abspath(__file__))
            )
            print(f"Bot visit completed for {url}")
            print(f"Bot output: {result.stdout}")
            if result.stderr:
                print(f"Bot errors: {result.stderr}")
        except subprocess.TimeoutExpired:
            print(f"Bot visit timed out for {url}")
        except Exception as e:
            print(f"Error running bot for {url}: {e}")
    
    # Run bot in background thread
    thread = threading.Thread(target=run_bot)
    thread.daemon = True
    thread.start()
    
    return jsonify({
        'success': True,
        'message': f'Bot will visit {url}',
        'url': url,
        'estimated_time': '10-30 seconds'
    })

@app.route('/status')
def status():
    cleanup_old_visits()
    return jsonify({
        'app_name': APP_NAME,
        'app_url': APP_URL,
        'url_pattern': APP_URL_REGEX,
        'rate_limit': f'{APP_LIMIT} visits per {APP_LIMIT_TIME} seconds',
        'current_visits': len(visit_log),
        'visits_remaining': max(0, APP_LIMIT - len(visit_log)),
        'locked': lockfile.exists(),
        'visit_log': visit_log[-10:]  # Last 10 visits
    })

@app.errorhandler(404)
def not_found(error):
    return jsonify({'success': False, 'error': 'Endpoint not found'}), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({'success': False, 'error': 'Internal server error'}), 500

if __name__ == '__main__':
    print(f"Starting {APP_NAME}")
    print(f"App URL: {APP_URL}")
    print(f"URL Pattern: {APP_URL_REGEX}")
    print(f"Rate Limit: {APP_LIMIT} visits per {APP_LIMIT_TIME} seconds")
    print("Bot server starting on http://0.0.0.0:3000")
    
    app.run(host='0.0.0.0', port=3000, debug=False)
