from flask import Flask

from blueprints.calc import calc_page
from blueprints.file_manager import file_manager_pages

app = Flask(__name__)
app.register_blueprint(calc_page, url_prefix="/calc")
app.register_blueprint(file_manager_pages, url_prefix="/files")

@app.route("/")
def index():
    return "Hello world"


if __name__ == "__main__":
    app.run("0.0.0.0", port=5000)
