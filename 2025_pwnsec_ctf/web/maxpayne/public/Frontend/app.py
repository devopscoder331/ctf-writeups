from flask import Flask, request, render_template, redirect, url_for, session, flash, jsonify, make_response
import requests
import os
from functools import wraps
from config import Config

app = Flask(__name__)
app.config.from_object(Config)
app.config['SESSION_COOKIE_HTTPONLY'] = False
# PHP backend configuration
PHP_BACKEND_URL = app.config['PHP_BACKEND_URL']
PHP_BACKEND_TIMEOUT = app.config['PHP_BACKEND_TIMEOUT']
@app.after_request
def fix_encoding(response):
    if "Vary" in response.headers:
        response.headers.pop("Content-Encoding")
    return response
def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'logged_in' not in session:
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated_function

def copy_php_headers_to_flask_response(flask_response, php_request_response):

    for header_name, header_value in php_request_response.headers.items():
        # Skip headers that Flask manages itself
        if header_name.lower() not in ['content-length', 'content-type', 'transfer-encoding']:
            flask_response.headers[header_name] = header_value
    return flask_response

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        
        # Send login request to PHP backend API
        try:
            response = requests.post(f'{PHP_BACKEND_URL}/login.php', 
                                   json={'username': username, 'password': password},
                                   timeout=PHP_BACKEND_TIMEOUT)
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    session['logged_in'] = True
                    session['username'] = username
                    session['user_id'] = data['user']['id']
                    session['is_admin'] = data['user']['is_admin']
                    session['php_session_cookie'] = response.cookies.get('PHPSESSID')
                    flash('Login successful!', 'success')
                    return redirect(url_for('dashboard'))
                else:
                    flash(data.get('message', 'Login failed'), 'error')
            else:
                try:
                    error_data = response.json()
                    flash(error_data.get('message', 'Login failed'), 'error')
                except:
                    flash('Login failed', 'error')
        except requests.RequestException as e:
            flash('Connection error to backend', 'error')
    
    return render_template('login.html')

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        
        # Send registration request to PHP backend API
        try:
            response = requests.post(f'{PHP_BACKEND_URL}/register.php',
                                   json={'username': username, 'password': password},
                                   timeout=PHP_BACKEND_TIMEOUT)
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    flash(data.get('message', 'Registration successful!'), 'success')
                    return redirect(url_for('login'))
                else:
                    flash(data.get('message', 'Registration failed'), 'error')
            else:
                try:
                    error_data = response.json()
                    flash(error_data.get('message', 'Registration failed'), 'error')
                except:
                    flash('Registration failed', 'error')
        except requests.RequestException as e:
            flash('Connection error to backend', 'error')
    
    return render_template('register.html')

@app.route('/dashboard')
@login_required
def dashboard():
    # Get dashboard data from PHP backend API
    try:
        cookies = {'PHPSESSID': session.get('php_session_cookie', '')}
        response = requests.get(f'{PHP_BACKEND_URL}/dashboard.php', 
                              cookies=cookies, 
                              timeout=PHP_BACKEND_TIMEOUT)
        
        if response.status_code == 200:
            data = response.json()
            if data.get('success'):
                # Create Flask response with template using JSON data
                flask_response = make_response(render_template('dashboard.html', 
                                             username=session.get('username'),
                                             notes=data.get('notes', []),
                                             all_notes=data.get('all_notes', []),
                                             is_admin=data['user'].get('is_admin', False),
                                             flag=data.get('flag')))
                
                copy_php_headers_to_flask_response(flask_response, response)
                
                return flask_response
            else:
                flash(data.get('message', 'Error accessing dashboard'), 'error')
                return redirect(url_for('login'))
        else:
            flash('Error accessing dashboard', 'error')
            return redirect(url_for('login'))
    except requests.RequestException as e:
        flash('Connection error to backend', 'error')
        return redirect(url_for('login'))

@app.route('/create_note', methods=['POST'])
@login_required
def create_note():
    title = request.form.get('title')
    content = request.form.get('content')
    
    try:
        cookies = {'PHPSESSID': session.get('php_session_cookie', '')}
        response = requests.post(f'{PHP_BACKEND_URL}/dashboard.php',
                               json={'action': 'create_note', 'title': title, 'content': content},
                               cookies=cookies,
                               timeout=PHP_BACKEND_TIMEOUT)
        
        if response.status_code == 200:
            data = response.json()
            if data.get('success'):
                flash(data.get('message', 'Note created successfully!'), 'success')
            else:
                flash(data.get('message', 'Failed to create note'), 'error')
        else:
            flash('Failed to create note', 'error')
    except requests.RequestException as e:
        flash('Connection error to backend', 'error')
    
    return redirect(url_for('dashboard'))

@app.route('/view_note/<note_id>')
@login_required
def view_note(note_id):
    try:
        cookies = {'PHPSESSID': session.get('php_session_cookie', '')}
        response = requests.get(f'{PHP_BACKEND_URL}/view_note.php?id={note_id}', 
                              cookies=cookies,
                              timeout=PHP_BACKEND_TIMEOUT)
        
        if response.status_code == 200:
            # Try to parse as JSON first (normal case)
            try:
                data = response.json()
                if data.get('success'):
                    # Create Flask response with template using JSON data
                    flask_response = make_response(render_template('view_note.html', 
                                                 note=data['note'],
                                                 viewer=data['viewer'],
                                                 note_id=note_id))
                    
                    # Copy all PHP headers including CSP
                    copy_php_headers_to_flask_response(flask_response, response)
                    
                    return flask_response
                else:
                    flash(data.get('message', 'Note not found'), 'error')
                    return redirect(url_for('dashboard'))
            
            except (ValueError, TypeError):

                html_content = f"""<!DOCTYPE html>
<html>
<head>
    <title>Note View</title>
</head>
<body>
{response.text}
</body>
</html>"""
                
                flask_response = make_response(html_content)
                

                copy_php_headers_to_flask_response(flask_response, response)
                
                flask_response.headers['Content-Type'] = 'text/html'
                
                return flask_response
        else:
            flash('Note not found or access denied', 'error')
            return redirect(url_for('dashboard'))
    
    except requests.RequestException as e:
        flash('Connection error to backend', 'error')
        return redirect(url_for('dashboard'))

@app.route('/report_note', methods=['POST'])
@login_required
def report_note():
    note_id = request.form.get('note_id')
    
    try:
        cookies = {'PHPSESSID': session.get('php_session_cookie', '')}
        response = requests.post(f'{PHP_BACKEND_URL}/dashboard.php',
                               json={'action': 'report_note', 'note_id': note_id},
                               cookies=cookies,
                               timeout=PHP_BACKEND_TIMEOUT)
        
        if response.status_code == 200:
            data = response.json()
            if data.get('success'):
                flash(data.get('message', 'Note reported successfully!'), 'success')
            else:
                flash(data.get('message', 'Failed to report note'), 'error')
        elif response.status_code == 401:
            flash('Session expired, please login again', 'error')
            session.clear()
            return redirect(url_for('login'))
        else:
            try:
                error_data = response.json()
                flash(f"Report failed: {error_data.get('message', 'Unknown error')}", 'error')
            except:
                flash(f'Report failed with status {response.status_code}', 'error')
    except requests.RequestException as e:
        flash(f'Connection error to backend: {str(e)}', 'error')
    
    return redirect(url_for('dashboard'))

@app.route('/admin_login', methods=['GET', 'POST'])
def admin_login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        
        try:
            response = requests.post(f'{PHP_BACKEND_URL}/admin-login.php',
                                   json={'username': username, 'password': password},
                                   timeout=PHP_BACKEND_TIMEOUT)
            
            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    session['logged_in'] = True
                    session['username'] = username
                    session['user_id'] = data['user']['id']
                    session['is_admin'] = True
                    session['php_session_cookie'] = response.cookies.get('PHPSESSID')
                    flash('Admin login successful!', 'success')
                    return redirect(url_for('dashboard'))
                else:
                    flash(data.get('message', 'Invalid admin credentials'), 'error')
            else:
                try:
                    error_data = response.json()
                    flash(error_data.get('message', 'Invalid admin credentials'), 'error')
                except:
                    flash('Invalid admin credentials', 'error')
        except requests.RequestException as e:
            flash('Connection error to backend', 'error')
    
    return render_template('admin_login.html')

@app.route('/logout')
def logout():
    # Clear Flask session
    session.clear()
    
    # Call PHP logout
    try:
        requests.get(f'{PHP_BACKEND_URL}/logout.php')
    except:
        pass 
    
    flash('Logged out successfully', 'success')
    return redirect(url_for('index'))

@app.route('/api/notes')
@login_required
def api_notes():
    """API endpoint to get notes data as JSON"""
    try:
        cookies = {'PHPSESSID': session.get('php_session_cookie', '')}
        response = requests.get(f'{PHP_BACKEND_URL}/dashboard.php', 
                              cookies=cookies,
                              timeout=PHP_BACKEND_TIMEOUT)
        
        if response.status_code == 200:
            data = response.json()
            if data.get('success'):
                return jsonify({
                    'status': 'success',
                    'message': 'Data fetched from PHP backend API',
                    'notes': data.get('notes', []),
                    'all_notes': data.get('all_notes', []),
                    'user': data.get('user', {}),
                    'flag': data.get('flag')
                })
            else:
                return jsonify({'status': 'error', 'message': data.get('message', 'API error')})
        else:
            return jsonify({'status': 'error', 'message': f'HTTP {response.status_code}'})
    except requests.RequestException as e:
        return jsonify({'status': 'error', 'message': str(e)})

@app.route('/status', methods=['GET', 'POST'])
def status():
    is_authenticated = bool(session.get('logged_in'))
    username = session.get('username') if is_authenticated else None
    is_admin = bool(session.get('is_admin')) if is_authenticated else False

    client_ip = request.headers.get("X-Forwarded-For", request.remote_addr)
    
    # Get user-specified level from form data or query parameter
    if request.method == 'POST':
        level = request.form.get('level')
    else:
        level = request.args.get('level')
    
    # If no level provided, ask user to input it
    if not level:
        return jsonify({
            "ok": False,
            "message": "Please provide a status level parameter"
        }), 400

    # Base response structure
    response_data = {
        "ok": True,
        "level": level
    }
    
    # Different information based on level
    level_lower = level.lower()
    
    if level_lower == "basic":
        response_data.update({
            "status": "online",
            "timestamp": request.environ.get('REQUEST_TIME', 'unknown')
        })
    
    elif level_lower == "auth":
        response_data.update({
            "auth": {
                "session_authenticated": is_authenticated,
                "username": username if is_authenticated else "anonymous"
            }
        })
    
    elif level_lower == "detailed":
        response_data.update({
            "auth": {
                "session_authenticated": is_authenticated,
                "username": username,
                "is_admin": is_admin
            },
            "meta": {
                "method": request.method,
                "client_ip": client_ip,
                "user_agent": request.headers.get('User-Agent', 'unknown')
            }
        })
    
    elif level_lower == "admin":
        if is_admin:
            response_data.update({
                "auth": {
                    "session_authenticated": is_authenticated,
                    "username": username,
                    "is_admin": is_admin,
                    "user_id": session.get('user_id')
                },
                "meta": {
                    "method": request.method,
                    "client_ip": client_ip,
                    "user_agent": request.headers.get('User-Agent', 'unknown'),
                    "session_cookie": session.get('php_session_cookie', 'none')
                },
                "system": {
                    "backend_url": PHP_BACKEND_URL,
                    "backend_timeout": PHP_BACKEND_TIMEOUT
                }
            })
        else:
            response_data = {
                "ok": False,
                "message": "Admin level access denied - insufficient privileges",
                "level": level
            }
    
    elif level_lower == "debug":
        response_data.update({
            "auth": {
                "session_authenticated": is_authenticated,
                "username": username,
                "is_admin": is_admin,
                "user_id": session.get('user_id'),
                "full_session": dict(session)
            },
            "meta": {
                "method": request.method,
                "client_ip": client_ip,
                "user_agent": request.headers.get('User-Agent', 'unknown'),
                "all_headers": dict(request.headers),
                "remote_addr": request.remote_addr
            },
            "system": {
                "backend_url": PHP_BACKEND_URL,
                "backend_timeout": PHP_BACKEND_TIMEOUT,
                "flask_config": {
                    "debug": app.config.get('DEBUG'),
                    "host": app.config.get('HOST'),
                    "port": app.config.get('PORT')
                }
            }
        })
    
    else:
        # Default level - minimal info
        response_data.update({
            "status": "online",
            "auth": {
                "authenticated": is_authenticated
            },
            "meta": {
                "method": request.method
            }
        })

    response = jsonify(response_data)
    
    response.headers["Level"] = level
    return response

@app.route('/blocked')
def blocked():
    return jsonify({
        "error": "Access Denied",
        "message": "This endpoint is currently blocked"
    }), 403

if __name__ == '__main__':
    print(f"Starting Flask Frontend...")
    print(f"Frontend URL: http://{app.config['HOST']}:{app.config['PORT']}")
    print(f"PHP Backend URL: {PHP_BACKEND_URL}")
    print(f"Debug Mode: {app.config['DEBUG']}")
    
    app.run(
        debug=app.config['DEBUG'], 
        host=app.config['HOST'], 
        port=app.config['PORT']
    )
