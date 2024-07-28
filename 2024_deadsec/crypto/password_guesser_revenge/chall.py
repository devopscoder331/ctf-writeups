from collections import Counter
from Crypto.Util.number import *
from Crypto.Cipher import AES
import hashlib
from Crypto.Util.Padding import pad
import math

flag = b'<redacted>'
P = 13**37
password = b'<redacted>' # password charset is string.printable
pl = list(password)
pl = sorted(pl)
assert math.prod(pl) % P == sum(pl) % P
password2 = bytes(pl)
#print(password2)
print(f"counts = {[cnt for _, cnt in Counter(password).items()]}")
cipher = AES.new(hashlib.sha256(password2).digest(), AES.MODE_CBC)
print(f"c = {cipher.encrypt(pad(flag, 16))}")
print(f"iv = {cipher.iv}")


'''
counts = [2, 4, 14, 7, 3, 2, 5, 3, 1, 3, 1, 4, 3, 3, 2, 10, 2, 6, 4, 1, 3, 4, 3, 2, 4, 3, 6, 1, 1, 4, 2, 21, 8, 8, 2, 4, 1, 9, 3, 4, 8, 3, 1, 2, 2, 5, 8, 2, 7, 2, 9, 2, 2, 6, 6, 3, 3, 9, 1, 3, 6, 6, 2, 4, 5, 3, 3, 8, 5, 1, 1, 9, 2, 8, 4, 1, 4, 9, 4, 1, 3, 4, 3, 4, 6, 4]
c = b'\xfb\x9e\xda\x81\xa6\xdf.\xc9zw\xb6t\x9e\x05\xb7\xdb\x84\xe5\x01\x97\xfb\xd2\x04 i\xa5\x13d\xfd\x89c\x0b'
iv = b'\xd4\xa6\xbc\xae\t/\xd3\x85YY\xb5\xda\xcf\xcaX\xb3'
'''