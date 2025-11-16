# Flask Frontend Configuration

import os

class Config:
    """Flask application configuration"""
    
    # Flask settings
    SECRET_KEY = os.getenv('FLASK_SECRET_KEY', 'SECRET_KEY_CHANGE_ME')
    DEBUG = os.getenv('FLASK_DEBUG', 'False').lower() == 'true'
    HOST = os.getenv('FLASK_HOST', '0.0.0.0')
    PORT = int(os.getenv('FLASK_PORT', 5000))
    
    # PHP Backend settings
    PHP_BACKEND_URL = os.getenv('PHP_BACKEND_URL', 'http://localhost:80')
    PHP_BACKEND_TIMEOUT = int(os.getenv('PHP_BACKEND_TIMEOUT', 20))
    
    # Session settings
    SESSION_PERMANENT = False
    SESSION_TYPE = 'filesystem'
    
