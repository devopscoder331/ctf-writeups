from os import urandom

from Crypto.Cipher import ChaCha20
from Crypto.Hash import BLAKE2b
from Crypto.Random.random import getrandbits

from complex_integer import ComplexInteger as CI
from ec import Point, get_point, mul
from secret import flag

p = CI(3269507727967, 27132981712422)
a = CI(645998724097, 6312492567089)
b = CI(461842182557, 7565454455722)
G = Point(CI(-3046838041084, -5693046299168), CI(-14206613175608, -6130930752474))

s = getrandbits(80)
Q = mul(G, s, a, p)

h = BLAKE2b.new(digest_bytes=32)
h.update(str(s).encode())

key = h.digest()
nonce = urandom(12)
cip = ChaCha20.new(key=key, nonce=nonce)
ct = cip.encrypt(flag)

print(f"{p, a, b, G, Q, nonce, ct = }")
