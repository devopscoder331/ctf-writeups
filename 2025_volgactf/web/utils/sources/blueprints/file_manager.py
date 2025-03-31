from flask import Blueprint, request, send_from_directory
from pathlib import Path
import glob
import os

FILES_DIR = Path(os.getcwd()) / "files"

file_manager_pages = Blueprint("file_manager_pages", __name__)

@file_manager_pages.route("/")
def index():
    return glob.glob("./files/*")

@file_manager_pages.route("/upload", methods=["POST"])
def upload_file():
    file = request.files.get("file")

    if file.content_length > 128:
        return "Too long"

    if not file:
        return "Please, upload file!"
    path = (FILES_DIR / Path(file.filename)).resolve()
    if path.parent.name != "files":
        return "Error"
    
    file.save(path)
    return "Done!"

@file_manager_pages.route("/<filename>")
def get_file(filename):
    return send_from_directory("files", filename)
    

@file_manager_pages.route("/rename", methods=["POST"])
def rename_file():
    old_name = request.form.get("old_name")
    new_name = request.form.get("new_name")
    if not old_name or not new_name:
        return "Please, specify params"
    
    if "/.." in old_name or "../" in old_name:
        return "Error"

    if "/.." in new_name or "../" in new_name:
        return "Error"

    old_file_path = FILES_DIR / old_name
    new_file_path = FILES_DIR / new_name

    old_file_path.rename(new_file_path)

    return "Done!"
