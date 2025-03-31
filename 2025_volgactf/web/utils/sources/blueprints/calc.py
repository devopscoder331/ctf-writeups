from flask import Blueprint, request
from subprocess import Popen, PIPE
import shlex

calc_page = Blueprint("calc_page", __name__)

@calc_page.route("/")
def index():
    return "index"


@calc_page.route("/execute", methods=["POST"])
def execute():
    expr = request.form.get("expression", "")
    args = ["./calc.sh", expr]
    p = Popen(shlex.join(args), shell=True, stdout=PIPE)
    result, err = p.communicate()
    return result
