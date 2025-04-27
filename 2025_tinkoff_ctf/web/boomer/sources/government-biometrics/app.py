#!/usr/bin/env python3
"""
Government Biometrics Dashboard
Displays capybara biometric data for Kapibarovsk City Officials
"""

from fastapi import FastAPI, Request
from fastapi.templating import Jinja2Templates
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles
import psycopg2
from psycopg2.extras import DictCursor
import json
import os
import traceback
import uvicorn

app = FastAPI(docs_url=None, redoc_url=None, openapi_url=None, title="Government Biometrics Dashboard")
templates = Jinja2Templates(directory="templates")

# FAQ content
FAQ_CONTENT = [
    {
        "question": "What is the purpose of this system?",
        "answer": "The Kapi ID system is used by Kapibarovsk City Officials for paw identification and tracking of the local capybara population. The data is also used for marketing purposes to promote capybara-friendly city initiatives."
    },
    {
        "question": "How is the data collected?",
        "answer": "Biometric data is collected through our advanced paw scanning system, which measures various parameters including paw size, pad count, and claw length."
    },
    {
        "question": "Who has access to this data?",
        "answer": "Access is restricted to authorized Kapibarovsk City Officials and approved marketing partners."
    }
]

# Read flag file
def read_flag():
    try:
        flag_path = os.environ.get('FLAG_PATH', 'flag.txt')
        if os.path.exists(flag_path):
            with open(flag_path, 'r') as flag_file:
                flag_content = flag_file.read().strip()
                return flag_content
        else:
            print(f"[ERROR] Flag file not found at: {flag_path}")
            return "Flag file not found. Please create a flag.txt file."
    except Exception as e:
        print(f"[ERROR] Error reading flag: {str(e)}")
        return f"Error reading flag: {str(e)}"

# PostgreSQL connection
def get_db_connection():
    try:
        host = os.environ.get('POSTGRES_HOST', 'biometric_db')
        port = int(os.environ.get('POSTGRES_PORT', 5432))
        dbname = os.environ.get('POSTGRES_DB', 'biometrics')
        user = os.environ.get('POSTGRES_USER', 'biometrics_user')
        
        try:
            with open('/run/secrets/db_password', 'r') as f:
                password = f.read().strip()
                if not password:
                    raise ValueError("Empty password file")
        except Exception as file_error:
            print(f"[-] Error reading password file: {file_error}")
            raise
        
        print(f"[DEBUG] Connecting to PostgreSQL: host={host}, port={port}, dbname={dbname}, user={user}")
        
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
        traceback.print_exc()
        return None

@app.get("/", response_class=HTMLResponse)
async def index(request: Request):
    try:
        flag_content = read_flag()
        FAQ_CONTENT.append({"question":"What is the flag?","answer":flag_content})
        conn = get_db_connection()
        if not conn:
            return templates.TemplateResponse("index.html", {"request": request, "error": "Could not connect to database"})
        
        biometric_data = []
        visitor_stats = []
        unique_ips = set()
        
        try:
            with conn.cursor(cursor_factory=DictCursor) as cur:
                # Get all biometric data
                cur.execute("""
                    SELECT ip_address, timestamp, paw_data
                    FROM biometric_data
                    ORDER BY timestamp DESC
                """)
                rows = cur.fetchall()
                
                for row in rows:
                    biometric_data.append({
                        "ip": row['ip_address'],
                        "timestamp": row['timestamp'].isoformat(),
                        "paw_data": row['paw_data']
                    })
                    unique_ips.add(row['ip_address'])
                
                # Get visitor statistics
                cur.execute("""
                    SELECT ip_address, COUNT(*) as visit_count, MAX(timestamp) as last_visit
                    FROM biometric_data
                    GROUP BY ip_address
                    ORDER BY last_visit DESC
                """)
                stats = cur.fetchall()
                
                for stat in stats:
                    visitor_stats.append({
                        "ip": stat['ip_address'],
                        "visits": str(stat['visit_count']),
                        "last_visit": stat['last_visit'].isoformat()
                    })
        finally:
            conn.close()
        
        # Render template with the data
        return templates.TemplateResponse("index.html", {
            "request": request,
            "flag": flag_content,
            "tracked_ips": list(unique_ips),
            "biometric_data": biometric_data,
            "visitor_stats": visitor_stats,
            "faq": FAQ_CONTENT
        })
    except Exception as e:
        print(f"[ERROR] Error retrieving biometric data: {str(e)}")
        traceback.print_exc()
        return templates.TemplateResponse("index.html", {"request": request, "error": f"Error retrieving biometric data: {str(e)}"})

if __name__ == '__main__':
    uvicorn.run(app, host="0.0.0.0", port=8082) 
