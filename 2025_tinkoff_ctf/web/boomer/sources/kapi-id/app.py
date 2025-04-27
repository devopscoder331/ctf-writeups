#!/usr/bin/env python3
"""
Kapi ID: Social Network Biometric Verification System
This application processes paw biometric data for capybara account verification on Kapi ID
"""

from fastapi import FastAPI, Request, Form, Response, File, UploadFile
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
from fastapi.staticfiles import StaticFiles
from fastapi.responses import RedirectResponse
from typing import Optional
from lxml import etree
import os
import pickle
import base64
import json
import datetime
import psycopg2
from psycopg2.extras import DictCursor
from cryptography.fernet import Fernet

app = FastAPI(docs_url=None, redoc_url=None, openapi_url=None, title="Kapi ID - Social Network Verification")
templates = Jinja2Templates(directory="templates")

# Initialize PostgreSQL connection
def get_db_connection():
    try:
        host = os.environ.get('POSTGRES_HOST', 'biometric_db')
        port = int(os.environ.get('POSTGRES_PORT', 5432))
        dbname = os.environ.get('POSTGRES_DB', 'biometrics')
        user = os.environ.get('POSTGRES_USER', 'biometrics_user')
        
        # Read password from file with proper error handling
        try:
            with open('/run/secrets/db_password', 'r') as f:
                password = f.read().strip()
                if not password:
                    raise ValueError("Empty password file")
        except Exception as file_error:
            print(f"[-] Error reading password file: {file_error}")
            raise
        
        conn = psycopg2.connect(
            host=host,
            port=port,
            dbname=dbname,
            user=user,
            password=password
        )
        print("[+] Successfully connected to PostgreSQL database")
        return conn
    except Exception as e:
        print(f"[-] PostgreSQL connection error: {e}")
        return None

# Initialize database tables
def init_db():
    conn = get_db_connection()
    if conn:
        try:
            with conn.cursor() as cur:
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS biometric_data (
                        id SERIAL PRIMARY KEY,
                        ip_address VARCHAR(45),
                        timestamp TIMESTAMP,
                        paw_data JSONB
                    )
                """)
                conn.commit()
                print("[+] Database tables initialized")
        except Exception as e:
            print(f"[-] Error initializing database: {e}")
        finally:
            conn.close()

# Initialize database on startup
init_db()

# Get or generate encryption key
def get_encryption_key():
    key_path = "/tmp/key"
    if os.path.exists(key_path):
        # Load existing key from file
        with open(key_path, "rb") as key_file:
            return key_file.read().strip()
    else:
        # Generate a new key and save it
        new_key = Fernet.generate_key()
        with open(key_path, "wb") as key_file:
            key_file.write(new_key)
        return new_key

# Initialize encryption key and Fernet
ENCRYPTION_KEY = get_encryption_key()
fernet = Fernet(ENCRYPTION_KEY)

# Store biometric data in PostgreSQL
def store_biometric_data(user_ip, biometric_data):
    """Save user IP and biometric data to PostgreSQL"""
    conn = get_db_connection()
    if conn:
        try:
            with conn.cursor() as cur:
                cur.execute(
                    "INSERT INTO biometric_data (ip_address, timestamp, paw_data) VALUES (%s, %s, %s)",
                    (user_ip, datetime.datetime.now(), json.dumps(biometric_data))
                )
                conn.commit()
                print(f"[+] Stored biometric data for IP: {user_ip}")
        except Exception as e:
            print(f"[-] Error storing biometric data: {e}")
        finally:
            conn.close()

# Generate cookie from data
def save_pickled_data_to_cookie(data):
    """Save data as encrypted pickle object to cookie"""
    pickled_data = pickle.dumps(data)
    encrypted_data = fernet.encrypt(pickled_data)
    # Convert to base64 for cookie storage
    return base64.b64encode(encrypted_data).decode('utf-8')

# Load data from cookie
def load_pickled_data_from_cookie(cookie_data):
    """Load data from encrypted pickle object in cookie"""
    if not cookie_data:
        return None
    try:
        # Decode base64 and decrypt
        encrypted_data = base64.b64decode(cookie_data)
        pickled_data = fernet.decrypt(encrypted_data)
        return pickle.loads(pickled_data)
    except Exception as e:
        print(f"Error loading pickled data from cookie: {e}")
        return None

# Biometric data parser
def parse_xml(xml_content):
    """
    Parses XML content with biometric data
    """
    try:
        # Creating XML parser that allows external entities
        parser = etree.XMLParser(resolve_entities=True, no_network=False, dtd_validation=True)
        
        # Parse the XML data
        root = etree.fromstring(xml_content.encode(), parser)
        
        # Extract and return data from the XML
        result = {}
        for element in root:
            if len(element) > 0:  # If element has children
                # Handle nested elements
                nested_data = {}
                for child in element:
                    nested_data[child.tag] = child.text.strip() if child.text else ""
                result[element.tag] = nested_data
            else:
                # Store the element text value
                result[element.tag] = element.text.strip() if element.text else ""
        
        return result
    except Exception as e:
        return {"error": str(e)}

@app.get("/", response_class=HTMLResponse)
async def index_get(request: Request):
    # Get user data from cookie
    cookie_data = request.cookies.get('user_data')
    saved_data = load_pickled_data_from_cookie(cookie_data)
    pretty_saved_data = json.dumps(saved_data, indent=4) if saved_data else None
    
    return templates.TemplateResponse("index.html", {
        "request": request,
        "conversion_result": None,
        "saved_data": pretty_saved_data,
        "xml_content": ""
    })

@app.post("/", response_class=HTMLResponse)
async def index_post(request: Request, xml_content: str = Form(...)):
    # Handle XML verification
    print(f"[DEBUG] Processing biometric data: {xml_content[:100]}...")
    conversion_result = parse_xml(xml_content)
    print(f"[DEBUG] Verification result: {conversion_result}")
    
    # Get user IP address
    user_ip = request.client.host
    print(f"[DEBUG] User IP: {user_ip}")
    
    # Store biometric verification data in PostgreSQL
    if conversion_result and (not isinstance(conversion_result, dict) or 'error' not in conversion_result):
        print(f"[DEBUG] Storing biometric verification data for IP: {user_ip}")
        store_biometric_data(user_ip, conversion_result)
        print(f"[DEBUG] Biometric verification data stored successfully")
    else:
        print(f"[DEBUG] Not storing biometric data - invalid result: {conversion_result}")
    
    # Get user data from cookie
    cookie_data = request.cookies.get('user_data')
    saved_data = load_pickled_data_from_cookie(cookie_data)
    
    # Format JSON prettily for display
    pretty_conversion_result = json.dumps(conversion_result, indent=4) if conversion_result else None
    pretty_saved_data = json.dumps(saved_data, indent=4) if saved_data else None
    
    # Create response
    response = templates.TemplateResponse("index.html", {
        "request": request,
        "conversion_result": pretty_conversion_result,
        "saved_data": pretty_saved_data,
        "xml_content": xml_content
    })
    
    # Save new data to cookie
    cookie_value = save_pickled_data_to_cookie(conversion_result)
    response.set_cookie(key="user_data", value=cookie_value, httponly=True)
    
    return response

@app.get("/example", response_class=HTMLResponse)
async def example(request: Request):
    # Sample XML for user guidance
    example_xml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE root [
<!ELEMENT root (paw_id, species, measurements, notes)>
<!ELEMENT paw_id (#PCDATA)>
<!ELEMENT species (#PCDATA)>
<!ELEMENT measurements (length, width, pad_count, claw_length)>
<!ELEMENT length (#PCDATA)>
<!ELEMENT width (#PCDATA)>
<!ELEMENT pad_count (#PCDATA)>
<!ELEMENT claw_length (#PCDATA)>
<!ELEMENT notes (#PCDATA)>
]>
<root>
    <paw_id>KBV-2024-001</paw_id>
    <species>Hydrochoerus hydrochaeris</species>
    <measurements>
        <length>12.5</length>
        <width>8.3</width>
        <pad_count>4</pad_count>
        <claw_length>2.1</claw_length>
    </measurements>
    <notes>Healthy adult specimen from Kapibarovsk Central Park</notes>
</root>"""
    
    return templates.TemplateResponse("example.html", {
        "request": request,
        "example_xml": example_xml
    })

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8081, reload=True) 
