import re
import whisper 

def load_model():
    print("Loading Whisper model at worker startup...")
    model = whisper.load_model("small")
    return model


def special_character_postprocessing(raw_transcription):    
    symbol_map = {
        r'\s*less\s+than\s*': '<',
        r'\s*greater\s+than\s*': '>',
        r'\s*equals\s*': '=',
        r'\s*double\s+quote\s*': '"',
        r'\s*single\s+quote\s*': "'",
        r'\s*colon\s*': ':',
        r'\s*semicolon\s*': ';',
        r'\s*forward\s+slash\s*': '/',
        r'\s*back\s+slash\s*': '\\',
        r'\s*dot\s*': '.',
        r'\s*comma\s*': ',',
        
        r'\s*question\s+mark\s*': '?',
        r'\s*exclamation\s+mark\s*': '!',
        
        r'\s*hyphen\s*': '-',
        r'\s*dash\s*': '-',
        r'\s*underscore\s*': '_',
        r'\s*open\s+bracket\s*': '[',
        r'\s*close\s+bracket\s*': ']',
        r'\s*open\s+brace\s*': '{',
        r'\s*close\s+brace\s*': '}',
        r'\s*open\s+parenthesis\s*': '(',
        r'\s*close\s+parenthesis\s*': ')',
        r'\s*angle\s+bracket\s*': '<',
        
        r'\s*plus\s*': '+',
        r'\s*minus\s*': '-',
        r'\s*multiply\s*': '*',
        r'\s*divide\s*': '/',
        r'\s*percent\s*': '%',
        r'\s*dollar\s*': '$',
        r'\s*euro\s*': '€',
        r'\s*pound\s*': '£',
        r'\s*yen\s*': '¥',
        r'\s*bitcoin\s*': '₿',
        
        r'\s*ampersand\s*': '&',
        r'\s*pipe\s*': '|',
        r'\s*caret\s*': '^',
        r'\s*tilde\s*': '~',
        r'\s*backtick\s*': '`',
        r'\s*at\s+sign\s*': '@',
        r'\s*hash\s*': '#',
        r'\s*number\s+sign\s*': '#',
        r'\s*section\s*': '§',
        r'\s*paragraph\s*': '¶',

        r'\s*degree\s*': '°',
        r'\s*copyright\s*': '©',
        r'\s*registered\s*': '®',
        r'\s*trademark\s*': '™',
        r'\s*bullet\s*': '•',
        r'\s*ellipsis\s*': '…',
        
        r'\s*left\s+arrow\s*': '←',
        r'\s*right\s+arrow\s*': '→',
        r'\s*up\s+arrow\s*': '↑',
        r'\s*down\s+arrow\s*': '↓',
        
        r'\s*back\s+quote\s*': '`',
        r'\s*acute\s+accent\s*': '´',
        r'\s*grave\s+accent\s*': '`',
        
        r'\s*amp\s*': '&',
        r'\s*lt\s*': '<',
        r'\s*gt\s*': '>',
        
        r'\s*quote\s*': '"',
        r'\s*apostrophe\s*': "'",
        r'\s*slash\s*': '/',
        r'\s*equal\s*': '=',
        r'\s*solidus\s*': '/',
    }
    processed_transcription = raw_transcription
    for pattern, replacement in symbol_map.items():
        processed_transcription = re.sub(
            pattern,
            lambda m: replacement,
            processed_transcription,
            flags=re.IGNORECASE
        )
        
    return processed_transcription
    