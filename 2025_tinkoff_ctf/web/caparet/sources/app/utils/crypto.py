import json
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad
from app.config import AES_KEY

def encrypt_ticket(ticket_data: dict) -> str:
    json_data = json.dumps(ticket_data).encode('utf-8')
    
    cipher = AES.new(AES_KEY.encode('utf-8'), AES.MODE_ECB)
    
    padded_data = pad(json_data, AES.block_size)
    
    encrypted_data = cipher.encrypt(padded_data)
    
    token = encrypted_data.hex()
    
    return token


def decrypt_ticket(token: str) -> dict:
    try:
        encrypted_data = bytes.fromhex(token)
        
        cipher = AES.new(AES_KEY.encode('utf-8'), AES.MODE_ECB)
        
        decrypted_padded_data = cipher.decrypt(encrypted_data)
        
        decrypted_data = unpad(decrypted_padded_data, AES.block_size)
        
        ticket_data = json.loads(decrypted_data.decode('utf-8'))
        
        return ticket_data
    except Exception as e:
        return {"error": str(e)} 
