import httpx
from os import environ
from fastapi import FastAPI, Request, Form, HTTPException
from fastapi.responses import HTMLResponse, JSONResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from starlette.middleware.sessions import SessionMiddleware
from urllib.parse import urlparse
from socket import getservbyname

app = FastAPI()

LOGIN_SECRET_KEY = environ.get("LOGIN_SECRET_KEY", "potatoalienkey")
SESSION_SECRET_KEY = environ.get("SESSION_SECRET_KEY", "authorizedalienkey")

app.add_middleware(SessionMiddleware, secret_key=SESSION_SECRET_KEY)

app.mount("/static", StaticFiles(directory="static"), name="static")
templates = Jinja2Templates(directory="templates")


NEED_USERNAME = "nasOSgalacticAdmin"
NEED_PASSWORD = "Persiki!"

def validate(value: str, key: str):
    result = 0
    for i, c in enumerate(value):
        result ^= ord(c) ^ ord(key[i % len(key)])
    print(result)
    return result

def check_address(address: str):
    parsed_addr = urlparse(address)
    
    parsed_addr = parsed_addr._replace(query="")
    
    if not "nasa" in parsed_addr.geturl():
        return None, "Only nasa addresses allowed!"
    
    if parsed_addr.port:
        return None, "No explicit ports allowed!"
    
    parsed_scheme = parsed_addr.scheme
    port = getservbyname(parsed_scheme)
    
    if port == 443:
        parsed_addr = parsed_addr._replace(scheme="https")
    else:
        parsed_addr = parsed_addr._replace(scheme="http")
        
    clean_addr = f"{parsed_addr.scheme}://{parsed_addr.netloc}:{port}{parsed_addr.path}"

    if len(clean_addr) > 33:
        return None, "Too long didn't read lol -_-"
        
    return clean_addr, None
    


@app.get("/", response_class=HTMLResponse)
async def index(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})

@app.post("/login")
async def login(request: Request, username: str = Form(...), password: str = Form(...)):
    if (validate(username, LOGIN_SECRET_KEY) - validate(password, LOGIN_SECRET_KEY)) == 7 and NEED_USERNAME in username and NEED_PASSWORD in password:
        request.session["auth"] = True
        return RedirectResponse(url="/terminal", status_code=303)
    else:
        return JSONResponse({"message":"Try harder, Alien!"})
    
@app.get("/terminal")
async def terminal(request: Request):
    if not request.session.get("auth"):
        raise HTTPException(status_code=403, detail="nasOS terminal is secured. Show your password first, Alien!")
    
    return templates.TemplateResponse("terminal.html", {"request":request})

@app.post("/terminal")
async def process_address(request: Request, address: str = Form(...)):
    if not request.session.get("auth"):
        raise HTTPException(status_code=403, detail="nasOS terminal is secured. Show your password first, Alien!")
    
    valid_addr, err =  check_address(address)
    if not err:
        try:
            async with httpx.AsyncClient(follow_redirects=False) as client:
                response = await client.get(valid_addr)
                
                if response.status_code == 200:
                    content = response.text
                else:
                    content = "Omg! NASA site is not working O_O"
        except httpx.RequestError:
            content = "That's bad :("
    else:
        content = err
        
    return templates.TemplateResponse("terminal.html", {"request":request, "content": content})