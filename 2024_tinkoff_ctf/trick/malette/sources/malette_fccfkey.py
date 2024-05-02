import asyncio
from random import SystemRandom
import sys

# I overheard random.randint is not secure
rnd = SystemRandom()

ROULETTE = [0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26]
BETS = {
    b"red": [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36],
    b"black": [2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35],
    b"even": [i for i in range(2, 37, 2)],
    b"odd": [i for i in range(1, 36, 2)],
    b"high": [i for i in range(19, 37)],
    b"low": [i for i in range(1, 19)]
}

class RouletteHandler:
    def __init__(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter):
        self.state = 0
        self.remaining = 0
        self.reader = reader
        self.writer = writer
    
    def random(self):
        if self.remaining == 0:
            self.state = rnd.getrandbits(32)
            self.remaining = 6
        self.remaining -= 1
        res = self.state % 37
        self.state //= 37
        return res

    async def finalize(self, error: bytes):
        self.writer.write(error)
        self.writer.write_eof()
        await self.writer.drain()
        self.writer.close()
        await self.writer.wait_closed()

    async def handle(self):
        position = 0
        balance = 100

        try:
            addr = self.writer.get_extra_info("peername")
            print(f"New connection from {addr}", file=sys.stderr)

            self.writer.write(b"""Welcome to SadCasino!

    Have problems accessing our services? Add digit 1 to get SadCasino1 and go ahead
    to new winnings on the best European slot machines.
                                
    Your croupier: Virus1337 (balance: 7231 chips).
    You: anonymous player (demo balance: 100 chips).
    """)
            await self.writer.drain()

            while True:
                self.writer.write(b"""Please place your bet:
- Red
- Black
- Even
- Odd
- High
- Low
- VIP bet: any 18 sectors (type 18 integers split by space)
>>> """)
                await self.writer.drain()
                bet_type = (await asyncio.wait_for(
                    self.reader.readuntil(b"\n"),
                    timeout=30
                )).strip().lower()

                if len(bet_type.split()) == 18 and all(x.isdigit() for x in bet_type.split()):
                    betted = list(map(int, bet_type.split()))
                elif bet_type in BETS:
                    betted = BETS[bet_type]
                else:
                    return await self.finalize(b"Invalid bet type. You were banned from casino.")

                self.writer.write(b"Please type bet amount (minimum 1 chip)\n>>> ")
                await self.writer.drain()

                bet = (await asyncio.wait_for(
                    self.reader.readuntil(b"\n"),
                    timeout=30
                )).strip()

                if not bet.isdigit() or int(bet) > balance:
                    return await self.finalize(b"Invalid bet. You were banned from casino.")
                if int(bet) > 100:
                    return await self.finalize(b"Please respect the table limits.")

                balance -= int(bet)

                position_index = ROULETTE.index(position)
                position_index = (position_index + 1 + self.random()) % len(ROULETTE)
                position = ROULETTE[position_index]

                win = position in betted
                print(f"{addr}: Bet was {bet.decode()} on {bet_type.decode()}, got {position}: {'won' if win else 'lost'}, balance {balance}", file=sys.stderr)

                if win:
                    balance += int(bet) * (36 // len(betted))
                
                self.writer.write(f"""Roulette sector: {position}
{"You won!" if win else "You lost"}

New balance: {balance} chips\n\n""".encode())
                await self.writer.drain()

                if balance >= 7331:
                    import flag
                    print(f"Bankruptcy case registered by {addr}", file=sys.stderr)
                    return await self.finalize(f"""Croupier is bankrupt :( All his assets will be transferred to you. His card PIN code: {flag.get()}""".encode())
                
                if balance == 0:
                    return await self.finalize(b"""You have no chips left. Come back when you'll get a few more""")
        except asyncio.TimeoutError:
            print(f"Timeout from {addr}", file=sys.stderr)
        except asyncio.IncompleteReadError:
            print(f"Connection closed by {addr}", file=sys.stderr)
        
        print(f"Connection finished by {addr}", file=sys.stderr)


async def handle_connection(reader: asyncio.StreamReader, writer: asyncio.StreamWriter):
    return await RouletteHandler(reader, writer).handle()


async def main():
    server = await asyncio.start_server(handle_connection, "0.0.0.0", 20035)
    print("Listening...", file=sys.stderr, flush=True)
    async with server:
        await server.serve_forever()


if __name__ == "__main__":
    asyncio.run(main())
