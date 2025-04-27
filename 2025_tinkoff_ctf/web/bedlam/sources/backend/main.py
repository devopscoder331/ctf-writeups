from docx import Document
from flask import Flask, request, jsonify
from uuid import uuid4
import requests
import random
import string
import io
import re
import os


app = Flask(__name__)

FLIGHT_MAP = {
    "CAP656": {
        "from": "SYD",
        "to": "LAX",
        "date": "2025-04-24",
        "departure": "2025-04-24 10:00",
        "arrival": "2025-04-24 12:00",
        "gate": "12",
        "terminal": "1",
    },
    "CAP657": {
        "from": "CBR",
        "to": "BYD",
        "date": "2025-04-24",
        "departure": "2025-04-24 13:30",
        "arrival": "2025-04-24 14:45",
        "gate": "7",
        "terminal": "2",
    },
    "CAP777": {
        "from": "KJFK",
        "to": "CDG",
        "date": "2025-04-26",
        "departure": "2025-04-26 01:12",
        "arrival": "2025-04-26 23:34",
        "gate": "2",
        "terminal": "3",
    },
}


@app.route("/boardingpass", methods=["POST"])
def print_boardingpass():
    data = request.json
    flight_number = data.get("flight_number", "CAP656")
    if flight_number not in FLIGHT_MAP:
        return jsonify({"message": "Invalid flight number"}), 400

    passengerName = data.get("passenger", "John Brains").split(" ")
    data["passenger"] = " ".join(name for name in passengerName if name.isalpha())
    data["seat"] = str(random.randint(1, 30)) + random.choice(
        string.ascii_uppercase[0:5]
    )
    data.update(
        {
            key: FLIGHT_MAP[flight_number][key]
            for key in [
                "from",
                "to",
                "departure",
                "arrival",
                "gate",
                "terminal",
                "date",
            ]
        }
    )
    r = requests.post(
        "http://internal-balancer/generate",
        headers={"X-Trace-Id": str(uuid4())},
        data=data,
    )
    if r.status_code != 200:
        return jsonify({"message": "Failed to generate boarding pass"}), 500
    return r.content, 200


@app.route("/business-lounge", methods=["POST"])
def business_lounge():
    data = request.json
    passenger = data["passenger"]
    count = data["guests"]
    order_id = "".join(random.choices(string.ascii_letters + string.digits, k=18))
    return jsonify({"orderId": order_id})


@app.route("/process-booking", methods=["POST"])
def process_booking():
    if 'file' not in request.files:
        return jsonify({"error": "No file provided"}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No file selected"}), 400
    
    if not file.filename.endswith('.docx'):
        return jsonify({"error": "File must be a .docx file"}), 400
    
    MAX_FILE_SIZE = 3 * 1024 * 1024 
    
    content_length = request.content_length
    if content_length and content_length > MAX_FILE_SIZE:
        return jsonify({"error": f"File size exceeds the limit of {MAX_FILE_SIZE / (1024 * 1024)}MB"}), 400
    
    try:
        doc = Document(io.BytesIO(file.read()))
        
        full_text = ""
        for paragraph in doc.paragraphs:
            full_text += paragraph.text + "\n"
        
        # Look for booking reference like №777
        booking_match = re.search(r'№(\d+)', full_text)
        
        if booking_match:
            booking_ref = booking_match.group(1)
            return jsonify({
                "message": f"Your application for booking #{booking_ref} for return has been accepted"
            }), 200
        else:
            return jsonify({"error": "No booking reference found in the document"}), 400
            
    except Exception as e:
        return jsonify({"error": f"Error processing document"}), 500


@app.route("/repair_panel", methods=["POST"])
def repair_panel():
    user_code = request.json.get("code")
    with open("/repair_code") as f:
        instruction = f.read()
        code = re.search(r'code: ([A-z0-9_-]{14,36})\.', instruction)
        if user_code == code.group(1):
            FLAG = os.getenv("FLAG")
            return jsonify({"message": f"System restored: {FLAG}"}), 200
        else:
            return jsonify({"error": f"Code is incorrect."}), 400

server = app

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8001)
