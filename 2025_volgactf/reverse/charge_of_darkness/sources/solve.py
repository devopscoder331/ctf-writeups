from flag_enc import flag_enc

def revert(value):
    # TODO: Implement f^-1
    return value

if __name__ == "__main__":
    flag = [revert(e) for e in flag_enc]
    open("flag", 'wb').write(struct.pack("I"*len(flag), *flag))