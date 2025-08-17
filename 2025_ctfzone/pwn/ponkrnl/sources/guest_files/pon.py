import socket
import threading
import base64
import tempfile
import os
import subprocess
import uuid
import logging

logging.basicConfig(level=logging.INFO, format='%(levelname)s: %(message)s')
logger = logging.getLogger(__name__)

HOST = '0.0.0.0'
PORT = 31337
BUFFER_SIZE = 4096
OUTPUT_FILE = "C:\\Task\\ponkrnl.txt"

def handle_client(client_socket: socket.socket, addr: str):
    try:
        logger.info(f"Connection from {addr}")
        client_socket.sendall(b"Ready\n")
        
        base64_data = b""
        while True:
            chunk: bytes = client_socket.recv(BUFFER_SIZE)
            if not chunk:
                break
            if b"\n" in chunk:
                chunk = chunk[:chunk.index(b"\n")]
                base64_data += chunk
                break

            base64_data += chunk
        
        if not base64_data:
            logger.warning(f"No data received from {addr}")
            return

        logger.info(f"Received {len(base64_data)} bytes from {addr}")

        temp_dir = tempfile.gettempdir()
        temp_file_name = f"{uuid.uuid4().hex}.exe"
        temp_file_path = os.path.join(temp_dir, temp_file_name)

        try:
            decoded_data = base64.b64decode(base64_data)
            logger.info(f"Decoded {len(decoded_data)} bytes for {addr}")

            with open(temp_file_path, 'wb') as f:
                f.write(decoded_data)
            logger.info(f"Saved executable to {temp_file_path} for {addr}")

            os.chmod(temp_file_path, 0o700)

            try:
                process = subprocess.Popen(temp_file_path, shell=False)
                logger.info(f"Started process {process.pid} from {temp_file_path} for {addr}")
                process.wait()
                logger.info(f"Process {process.pid} completed for {addr}")
            except Exception as e:
                logger.error(f"Failed to execute {temp_file_path} for {addr}: {e}")
        
        except base64.binascii.Error:
            logger.error(f"Invalid base64 data from {addr}")
        except Exception as e:
            logger.error(f"Error processing file for {addr}: {e}")
        finally:
            try:
                if os.path.exists(temp_file_path):
                    os.remove(temp_file_path)
                    logger.info(f"Deleted temporary file {temp_file_path} for {addr}")
            except Exception as e:
                logger.error(f"Failed to delete {temp_file_path} for {addr}: {e}")

        try:
            if os.path.exists(OUTPUT_FILE):
                with open(OUTPUT_FILE, "rb") as fd:
                    client_socket.sendall(fd.read())
        except Exception as e:
            logger.error(f"Failed to read {OUTPUT_FILE} for {addr}: {e}")
    except Exception as e:
        logger.error(f"Error handling client {addr}: {e}")
    finally:
        client_socket.close()
        logger.info(f"Closed connection with {addr}")

def main():
    try:
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind((HOST, PORT))
        server_socket.listen(5)
        logger.info(f"Server listening on {HOST}:{PORT}")

        while True:
            try:
                client_socket, addr = server_socket.accept()
                client_thread = threading.Thread(target=handle_client, args=(client_socket, addr))
                client_thread.daemon = True
                client_thread.start()
            except Exception as e:
                logger.error(f"Error accepting connection: {e}")
                continue

    except KeyboardInterrupt:
        logger.info("Server shutting down")
    except Exception as e:
        logger.error(f"Server error: {e}")
    finally:
        server_socket.close()
        logger.info("Server socket closed")

if __name__ == "__main__":
    main()