from Crypto.Random import random
from Crypto.Util.number import getPrime, isPrime


class ComplexInteger:
    def __init__(self, a, b=0):
        self.re = a
        self.im = b

    def __neg__(self):
        return ComplexInteger(-self.re, -self.im)

    def __add__(self, other):
        return ComplexInteger(self.re + other.re, self.im + other.im)

    def __sub__(self, other):
        return ComplexInteger(self.re - other.re, self.im - other.im)

    def __mul__(self, other):
        return ComplexInteger(
            self.re * other.re - self.im * other.im,
            self.re * other.im + self.im * other.re,
        )

    __rmul__ = __mul__

    def conj(self):
        return ComplexInteger(self.re, -self.im)

    def norm(self):
        return (self * self.conj()).re

    def __mod__(self, other):
        a = self
        b = other
        while True:
            tmp = a * b.conj()
            sc = b.norm()

            c1, c2 = round(tmp.re / sc), round(tmp.im / sc)
            a -= b * ComplexInteger(c1, c2)
            if a.norm() < b.norm():
                return a

    def pow_mod(self, n: int, mod):
        res = self
        for i in bin(n)[3:]:
            res = res * res % mod
            if i == "1":
                res = res * self % mod
        return res

    def is_prime(p):
        if p.im == 0:
            return isPrime(abs(p.re))
        return isPrime(p.norm())

    def __eq__(self, other):
        if isinstance(other, int):
            return self.im == 0 and self.re == other
        return self.im == other.im and self.re == other.re

    def __repr__(self):
        return f"{self.re} + {self.im} * I"

    def tuple(self):
        return (self.re, self.im)

    @staticmethod
    def random_mod(p):
        return (
            ComplexInteger(random.randint(0, abs(p.re)), random.randint(0, abs(p.im)))
            % p
        )

    def __hash__(self):
        return hash((self.re, self.im))


def tonelli_shanks(a, p):
    if a.pow_mod((p.norm() - 1) // 2, p) != 1:
        return None

    s = 0
    q = p.norm() - 1
    while q % 2 == 0:
        q //= 2
        s += 1

    z = ComplexInteger(2)
    while z.pow_mod((p.norm() - 1) // 2, p) == 1:
        z = ComplexInteger.random_mod(p)

    c = z.pow_mod(q, p)
    r = a.pow_mod((q + 1) // 2, p)
    t = a.pow_mod(q, p)
    m = s

    while t != 1:
        i = 0
        temp = t
        while temp != 1:
            temp = temp.pow_mod(2, p)
            i += 1
        b = c.pow_mod(2 ** (m - i - 1), p)
        r = (r * b) % p
        t = (t * b * b) % p
        c = (b * b) % p
        m = i

    return r
