import ctypes

# Load the C standard library
libc = ctypes.CDLL(None)  # Automatically finds the C standard library

# Define strtok function prototype
libc.strtok.restype = ctypes.c_char_p
libc.strtok.argtypes = [ctypes.c_char_p, ctypes.c_char_p]

def strtok(input_string, delimiter):
    # Create a ctypes string buffer for the input string
    input_buffer = ctypes.create_string_buffer(input_string.encode('utf-8'))
    
    # Tokenize the first part of the string
    token = libc.strtok(input_buffer, delimiter.encode('utf-8'))
    
    tokens = []
    
    # Iterate through the tokens
    while token is not None:
        # Add the token to the list of tokens
        tokens.append(token.decode('utf-8'))
        
        # Get the next token
        token = libc.strtok(None, delimiter.encode('utf-8'))

    # Solve edge case
    if input_string[-1] == delimiter:
        token = ctypes.string_at(token)
        tokens.append(token)
    
    return tokens