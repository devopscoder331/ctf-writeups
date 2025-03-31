from Crypto.Random.random import getrandbits, randrange
from ecdsa import SigningKey, VerifyingKey, curves


class PolyRNG:
    def __init__(self, m: int, n: int = None):
        self.m = m
        if n is None:
            self.n = 9
        else:
            self.n = n

        self.s = randrange(self.m)
        self.p = [randrange(self.m) for _ in range(self.n)]

    def eval(self, x: int):
        e = 0
        for i, c in enumerate(self.p):
            e *= x
            e += c
            e %= self.m
        return e

    def __call__(self) -> int:
        self.s = self.eval(self.s)
        r = self.s
        self.s = self.eval(self.s)
        return r


class Signer:
    def __init__(self):
        self.sk = SigningKey.generate(curve=curves.NIST256p)
        self.vk = self.sk.verifying_key
        self.m = curves.NIST256p.order
        self.rng = PolyRNG(int(self.m))

    def sign(self, msg: bytes) -> bytes:
        return self.sk.sign(msg, k=self.rng())

    def verify(self, msg: bytes, sig: bytes):
        assert self.vk.verify(sig, msg)


if __name__ == "__main__":
    S = Signer()
    s = S.sign(b"aboba")
    S.verify(b"aboba", s)
