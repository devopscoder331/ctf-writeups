from functools import namedtuple

from complex_integer import ComplexInteger as CI
from complex_integer import tonelli_shanks

Point = namedtuple("Point", ["x", "y"])
Inf = Point(0, 0)


def is_on_curve(P, a, b, p):
    return (P == Inf) or (P.y * P.y - (P.x * P.x * P.x + a * P.x + b)) % p == 0


def neg(P, p):
    return Point(P.x, (-P.y) % p)


def add(P, Q, a, p):
    if P == Inf:
        return Q
    if Q == Inf:
        return self
    if P.x == Q.x and P.y == (-Q.y) % p:
        return Inf

    if P == Q:
        num = CI(3) * P.x * P.x + a
        den = CI(2) * P.y
    else:
        num = Q.y - P.y
        den = Q.x - P.x
    lam = num * den.pow_mod(p.norm() - 2, p) % p

    x = lam * lam - P.x - Q.x
    y = lam * (P.x - x) - P.y
    return Point(x % p, y % p)


def mul(P, n: int, a, p):
    if n == 0:
        return Inf
    if n < 0:
        P = neg(P, p)
        n = -n

    R = P
    for i in bin(n)[3:]:
        R = add(R, R, a, p)
        if i == "1":
            R = add(R, P, a, p)
    return R


def get_point(a, b, p):
    while True:
        x = CI.random_mod(p)
        tmp = (x * x * x + a * x + b) % p
        y = tonelli_shanks(tmp, p)
        if y is not None:
            break
    return Point(x, y)
