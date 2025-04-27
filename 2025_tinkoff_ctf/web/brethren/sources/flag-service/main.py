from fastapi import FastAPI

app = FastAPI()

@app.get("/")
def flag_service():
    return {"flag": "tctf{XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX}"}
