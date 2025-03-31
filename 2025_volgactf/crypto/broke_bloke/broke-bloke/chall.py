from secret import flag
from sig import Signer

print("Please, crack me open")
M = b"Maybe crypto was the friends that we signed along the way"
S = Signer()
while True:
    cmd = input("> ")
    try:
        if cmd == "sign":
            msg = bytes.fromhex(input("m: "))
            sig = S.sign(msg)
            print(f"rs: {sig.hex()}")
        elif cmd == "verify":
            sig = bytes.fromhex(input("rs: "))
            S.verify(M, sig)
            print(f"Hooray, here's your flag: {flag}")
            break
        else:
            print("Didn't get it, bub")
    except Exception as e:
        print(e)
