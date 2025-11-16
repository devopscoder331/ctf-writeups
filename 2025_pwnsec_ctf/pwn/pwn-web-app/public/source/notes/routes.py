from flask import Blueprint, request, redirect, url_for, session
from utils.storage import get_user
from utils.models import Note


notes = Blueprint("notes", __name__)

def require_login():
    if not session.get("active"):
        return redirect(url_for("auth.login"))

@notes.route("/add_note", methods=['POST'])
def add_note():
    r = require_login()
    if r: return r

    user = get_user()
    choice = int(request.form.get("choice", 1))
    index = int(request.form.get("index", 0))
    content = request.form.get("content", "").encode('latin-1')

    manager = user.choose_storage(choice)
    manager[index] = Note(index,content)
    return "Note added\n"


@notes.route("/edit_note", methods=['POST'])
def edit_note():
    r = require_login()
    if r: return r

    user = get_user()
    choice = int(request.form.get("choice", 1))
    index = int(request.form.get("index", 0))
    data = request.form.get("data", "").encode('latin-1')
    start = int(request.form.get("start", 0))

    note = user.choose_storage(choice)[index]
    note[start] = data
    return "Edit done\n"


@notes.route("/reset", methods=['GET'])
def reset_manager():
    r = require_login()
    if r: return r

    user = get_user()
    choice = int(request.args.get("choice", 1))

    manager = user.choose_storage(choice)
    manager.reset()
    return "Manager reset\n"

@notes.route("/manager",methods=['GET','POST'])
def manager_controls():
    r = require_login()
    if r: return r

    user = get_user()

    if request.method == 'GET':
        choice = int(request.args.get('choice',1))
        try:
            index = int(request.args.get('index',0)) # base 10 please
            length = int(request.args.get('length'))
        except:
            return "Bad index or length"
        
        manager = user.choose_storage(choice)
        content = manager.read_name(index,length)
        return f"Manager name: {content.decode('latin-1')}\n"
    
    choice = int(request.form.get('choice',1))
    try:
        index = int(request.form.get('index',0))
        length = int(request.form.get('length'))
    except:
        return "Bad index or length"
    
    manager = user.choose_storage(choice)
    data = request.form.get('data').encode('latin-1')
    manager.set_name(index,length,data)
    return "Manager name set\n"
