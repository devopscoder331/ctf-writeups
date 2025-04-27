from flask import Flask, render_template, request, redirect, url_for, session, flash, jsonify
from flask_session import Session
import os
from datetime import datetime
import hashlib
import secrets
import re
import redis
import logging
from functools import wraps
from db_cache import db_cache

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger(__name__)

app = Flask(__name__)
app.config['SECRET_KEY'] = os.environ.get('SECRET_KEY', secrets.token_hex(32))
app.config['SESSION_TYPE'] = 'redis'
app.config['SESSION_REDIS'] = redis.from_url('redis://redis:6379/0')
app.config['SESSION_PERMANENT'] = True
app.config['PERMANENT_SESSION_LIFETIME'] = 3600
Session(app)

app.jinja_env.globals.update(min=min, max=max)

def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not session.get('user_id'):
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated_function

def hash_password(password, salt=None):
    if salt is None:
        salt = secrets.token_hex(16)
    hash_obj = hashlib.sha256()
    hash_obj.update((password + str(salt)).encode())
    return salt, hash_obj.hexdigest()

def validate_weather_data(temperature, humidity, description):
    errors = []
    
    try:
        temp = float(temperature)
        if temp < -273.15:
            errors.append("Temperature cannot be below absolute zero (-273.15°C)")
        elif temp > 100:
            errors.append("Temperature seems too high. Please check your input.")
    except ValueError:
        errors.append("Temperature must be a valid number")
    
    try:
        hum = int(humidity)
        if hum < 0 or hum > 100:
            errors.append("Humidity must be between 0 and 100")
    except ValueError:
        errors.append("Humidity must be a valid integer")
    
    if not description or len(description.strip()) == 0:
        errors.append("Description is required")
    elif len(description) > 500:
        errors.append("Description is too long (maximum 500 characters)")
    
    return errors

def generate_slug(description, created_at, id):
    words = re.sub(r'[^\w\s-]', '', description.lower()).split()[:5]
    slug_base = '-'.join(words)
    
    username = session.get('username', 'user')
    return f"{slug_base}-{username}"

@app.route('/')
@login_required
def index():
    page = request.args.get('page', 1, type=int)
    per_page = 10
    offset = (page - 1) * per_page
    
    total_result = db_cache.execute_one(
        'SELECT COUNT(*) FROM weather_reports WHERE user_id = %s',
        (session['user_id'],)
    )
    total_reports = total_result[0]
    total_pages = (total_reports + per_page - 1) // per_page
    
    reports = db_cache.execute('''
        SELECT w.id, w.user_id, w.temperature, w.humidity, w.description, w.created_at, w.slug, u.username 
        FROM weather_reports w 
        JOIN users u ON w.user_id = u.id 
        WHERE w.user_id = %s OR w.id = 1
        ORDER BY w.created_at DESC
        LIMIT %s OFFSET %s
    ''', (session['user_id'], per_page, offset))
    
    formatted_reports = []
    for report in reports:
        formatted_reports.append({
            'id': report[0],
            'user_id': report[1],
            'temperature': report[2],
            'humidity': report[3],
            'description': report[4],
            'created_at': report[5],
            'slug': report[6],
            'username': report[7]
        })
    
    return render_template('index.html', 
                         reports=formatted_reports, 
                         current_page=page,
                         total_pages=total_pages,
                         total_reports=total_reports,
                         per_page=per_page)

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        
        if not username or not password:
            flash('Please enter both username and password', 'error')
            return redirect(url_for('login'))
        
        salt_result = db_cache.execute_one(
            'SELECT password_salt FROM users WHERE username = %s',
            (username,)
        )

        if not salt_result:
            flash('Invalid username or password', 'error')
            return redirect(url_for('login'))

        salt = salt_result[0]
        
        hash_result = db_cache.execute_one(
            'SELECT password_hash FROM users WHERE username = %s',
            (username,)
        )
        hash_value = hash_result[0]
        
        user_id_result = db_cache.execute_one(
            'SELECT id FROM users WHERE username = %s',
            (username,)
        )
        user_id = user_id_result[0]
        
        if hash_password(password, salt)[1] == hash_value:
            session.clear()
            session['user_id'] = user_id
            session['username'] = username
            session.modified = True
            return redirect(url_for('index'))
        
        flash('Invalid username or password', 'error')
    return render_template('login.html')

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        
        if not username or not password:
            flash('Please enter both username and password', 'error')
            return redirect(url_for('register'))
        
        if len(username) < 9 or len(username) > 50:
            flash('Username must be between 9 and 50 characters', 'error')
            return redirect(url_for('register'))
        
        if len(password) < 9:
            flash('Password must be at least 9 characters long', 'error')
            return redirect(url_for('register'))
        
        existing_user = db_cache.execute_one(
            'SELECT id FROM users WHERE username = %s',
            (username,)
        )
        if existing_user:
            flash('Username already exists', 'error')
            return redirect(url_for('register'))
        
        salt, password_hash = hash_password(password)
        db_cache.execute(
            'INSERT INTO users (username, password_salt, password_hash) VALUES (%s, %s, %s)',
            (username, salt, password_hash)
        )
        
        flash('Registration successful! Please login.', 'success')
        return redirect(url_for('login'))
    return render_template('register.html')

@app.route('/logout')
def logout():
    session.clear()
    session.modified = True
    return redirect(url_for('login'))

@app.route('/report/<slug>')
@login_required
def view_report(slug):
    report_result = db_cache.execute_one('''
        SELECT w.id, w.user_id, w.temperature, w.humidity, w.description, w.created_at, w.slug, u.username 
        FROM weather_reports w 
        JOIN users u ON w.user_id = u.id 
        WHERE w.slug = %s
    ''', (slug,))
    
    if not report_result:
        flash('Report not found', 'error')
        return redirect(url_for('index'))
    
    report = {
        'id': report_result[0],
        'user_id': report_result[1],
        'temperature': report_result[2],
        'humidity': report_result[3],
        'description': report_result[4],
        'created_at': report_result[5],
        'slug': report_result[6],
        'username': report_result[7]
    }
    
    return render_template('report.html', report=report)

@app.route('/add_report', methods=['POST'])
@login_required
def add_report():
    data = request.json
    temperature = data.get('temperature')
    humidity = data.get('humidity')
    description = data.get('description')
    
    errors = validate_weather_data(temperature, humidity, description)
    if errors:
        return jsonify({'error': errors}), 400
    
    try:
        result = db_cache.execute_one(
            'INSERT INTO weather_reports (user_id, temperature, humidity, description) VALUES (%s, %s, %s, %s) RETURNING id, created_at',
            (session['user_id'], float(temperature), int(humidity), description.strip())
        )
        
        report_id, created_at = result[0], result[1]
        slug = generate_slug(description, created_at, report_id)
        
        db_cache.execute(
            'UPDATE weather_reports SET slug = %s WHERE id = %s',
            (slug, report_id)
        )
        
        return jsonify({'message': 'Report added successfully', 'slug': slug})
    except Exception as e:
        return jsonify({'error': 'Database error occurred'}), 500

@app.route('/api/report_description/<slug>')
@login_required
def api_report_description(slug):
    try:
        result = db_cache.execute_one('SELECT description FROM weather_reports WHERE slug = %s', (slug,))
        
        if not result:
            return jsonify({'error': 'Report not found'}), 404
            
        description = result[0]
        return jsonify({'description': description})
    except Exception as e:
        logger.error(f"Error fetching report: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/admin')
@login_required
def admin():
    if session.get('username') != 'admin':
        flash('Access denied', 'error')
        return redirect(url_for('index'))
    
    return render_template('admin.html', flag='tctf{XXXXXXXXXXXXXXXXXXXXXXXXXX}')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000) 
