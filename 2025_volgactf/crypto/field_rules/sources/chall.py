from functools import reduce
from operator import mul
from os import urandom

from Crypto.Util.number import getPrime

from secret import flag

ROUNDS, m, k = 512, 20, 78
key = int.from_bytes(flag)
target_bl = len(flag) * 8
prime_base = [getPrime(m) for _ in range(-(-target_bl // m) + 1)]
n = reduce(mul, prime_base, 1)
e_len = -(-n.bit_length() // 8)

assert key < n


def encrypt(m: bytes, key: int) -> bytes:
    e = int.from_bytes(m)
    for _ in range(ROUNDS - 1):
        e += key
        e = pow(e, k, n)
    e = (e + key) % n
    return e.to_bytes(e_len, "big")


msgs, encs = [], []
for i in range(10):
    m = urandom(len(flag))
    msgs.append(m)
    encs.append(encrypt(m, key))

with open("output.py", "wt") as f:
    f.write(f"{msgs, encs, n =}")
