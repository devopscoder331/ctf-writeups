from os import urandom
from random import Random
from Crypto.Cipher import ChaCha20
from secret import flag

random = Random()
secret = int.from_bytes(urandom(16)) % 2**78
random.seed(secret)

def fight(x, y):
    x_, y_ = x, y
    while x_ * y_:
        if random.getrandbits(1):
            x_ //= 2
        else:
            y_ //= 2
    return x if not x_ else y

# looks familiar, huh?
fighting_pit = []

print("Eh. Where's your rooster?")
picked = 0

your_fighters = []
my_fighters = []
print("Ok, you can take some from here")
while picked < 16:
    cmd = input("> ")
    if cmd == "pick":
        your_fighters.append(random.getrandbits(32))
        picked += 1
        print("It's Your choice")
    elif cmd == "skip":
        print("I respect that")
        my_fighters.append(random.getrandbits(32))
    else:
        print("Oh no. Anyway")

my_fighters = my_fighters[:16]
print(f"{your_fighters=}")

fighting_pit = your_fighters + my_fighters
while len(fighting_pit) != 1:
    x, y = random.choices(fighting_pit, k=2)
    l = fight(x, y)
    fighting_pit.remove(l)

key = random.getrandbits(256).to_bytes(32)
nonce = random.getrandbits(96).to_bytes(12)
cip = ChaCha20.new(key=key, nonce=nonce)
ct = cip.encrypt(flag)

w = fighting_pit[0]
if w in your_fighters:
    print(f"W fr fr.")
    print(f"nonce: {nonce.hex()}")
    print(f"ct: {ct.hex()}")
else:
    print(f"L + Ratio")
