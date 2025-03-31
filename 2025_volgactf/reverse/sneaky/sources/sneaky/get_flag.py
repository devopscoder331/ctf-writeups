from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad
import sys

enc = "9a5b3385d60ffcc965149c47d0a2bf46b26cdaefe5023e2dd40abb88724298b189a0fe4faee992747e57091df823e38098646b834193c39e612b5650a3e491a1d6828d3c869032068165ae1a6893f250"


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <key>")
        sys.exit()
    try:
        key = bytes.fromhex(sys.argv[1])
        aes = AES.new(key=key, mode=AES.MODE_ECB)
        print(unpad(aes.decrypt(bytes.fromhex(enc)), 16).decode())
    except:
        print("Key is not correct")