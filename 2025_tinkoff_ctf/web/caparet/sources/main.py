import uvicorn
from app.config import HOST, PORT, DEBUG

def main():
    """Run the FastAPI application with uvicorn"""
    uvicorn.run(
        "app.main:app",
        host=HOST,
        port=PORT,
        reload=DEBUG
    )

if __name__ == "__main__":
    main()
