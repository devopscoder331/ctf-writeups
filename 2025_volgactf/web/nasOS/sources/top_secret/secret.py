from flask import Flask
from os import environ

app = Flask(__name__)

FLAG = environ.get("FLAG", "flag{test_flag_for_cool_boys}")

@app.route("/top_secret")
def top_secret():
    return FLAG

if __name__ == '__main__':
   app.run(host='0.0.0.0', port=3689)