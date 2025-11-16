#!/usr/bin/env python3
import os
import re
import base64
import shutil
import ctypes
import subprocess
from shutil import which

# --- Constants ---
USERNAME_RE   = re.compile(r'^[A-Za-z0-9._-\{\}]{3,16}\Z')
_FILENAME_RE  = re.compile(r'^[A-Za-z0-9_.]{3,30}\Z')

GUEST_COMMANDS = {"login", "register", "exit"}
USER_COMMANDS  = {"profile", "global_docs", "create_docs", "read_docs", "remove_docs", "logout", "exit"}
ADMIN_COMMANDS = {"manage_users"} | USER_COMMANDS
VALID_ROLES    = {"ADMIN", "USER"}

CURRENT_PATH = os.getcwd()

Docs = {
    "str": str, 
    "chr": chr, 
    "len": len, 
    "eval": eval, 
    "exec": exec,
    "pow": pow, 
    "sum": sum, 
    "input": input,
}

Users = {
    "admin": {
        "role": "ADMIN", 
        "path": os.path.join("users", "admin"), 
        "password": os.urandom(16).hex()
    }
}

# --- Helpers ---
def check_username(users: dict, username: str, action: str = "Login"):
    if not USERNAME_RE.fullmatch(username):
        return False, False, "Invalid username (A-Za-z0-9._-, len 3..15)"
    exists = username in users
    return (True, exists, f"[!] Unknown username: {username}") if action == "Login" \
           else (True, not exists, f"[!] {username} already exists")

def is_safe_filename(name: str) -> bool:
    return isinstance(name, str) and bool(_FILENAME_RE.fullmatch(name))

def show_menu(logged_in: bool, is_admin: bool = False):
    if logged_in:
        print("\nAvailable options:")
        print("  - Profile\n  - Global_docs\n  - Create_docs\n  - Read_docs\n  - Remove_docs")
        if is_admin: print("  - Manage_users")
        print("  - Logout\n  - Exit\n")
    else:
        print("\nAvailable options:\n  - Login\n  - Register\n  - Exit\n")

def list_user_docs(user_path: str) -> list[str]:
    try:
        return sorted(f for f in os.listdir(user_path) if os.path.isfile(os.path.join(user_path, f)))
    except FileNotFoundError:
        return []

def process_home(dir_path: str, ctl_cmd: str | None = None):
    cli = ctl_cmd or CURRENT_PATH + "/ctl"
    if os.path.isfile(cli) and os.access(cli, os.X_OK):
        try:
            out = subprocess.check_output([cli, "path", dir_path], text=True, stderr=subprocess.STDOUT)
        except Exception as e:
            pass

def set_perm(path: str, ctl_cmd: str = "./ctl"):
    try:
        if os.path.isfile(ctl_cmd) and os.access(ctl_cmd, os.X_OK):
            res = subprocess.run([ctl_cmd, "permission", path],
                                 check=True, text=True,
                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            return
    except subprocess.CalledProcessError as e:
        raise RuntimeError(f"ctl failed: {e.stderr or e.stdout}") from e
    except Exception as e:
        pass

def count_admins(users: dict) -> int:
    return sum(1 for u in users.values() if u.get("role") == "ADMIN")

def print_users(users: dict):
    print("\n[i] Users:")
    for i, (u, meta) in enumerate(sorted(users.items()), 1):
        print(f"  {i}. {u}  [{meta.get('role','?')}]  {meta.get('path','-')}")
    print()

def print_role(role):
    s = str(role).strip().upper()
    COLORS = {
        "ADMIN": "\033[31m",  # red
        "USER":  "\033[32m",  # green
        "GUEST": "\033[90m",  # gray
    }
    c = COLORS.get(s, "")
    reset = "\033[0m" if c else ""
    return f"{c}{s}{reset}"

def manage_users(users: dict, current_user: str):
    while True:
        print_users(users)
        print("Manage actions: [C]hange role  [D]elete user  [H]ome user  [B]ack")
        action = input("Action: ").strip().lower()

        if action in {"b", "back"}:
            return

        elif action in {"c", "change", "change role"}:
            target = input("Username to change role: ").strip()
            if target not in users: 
                print("[!] User not found.")
                continue
            if target == "admin": 
                print("[!] Cannot change built-in 'admin' role here.")
                continue
            new_role = input("New role (ADMIN/USER): ").strip().upper()
            if new_role not in VALID_ROLES: 
                print("[!] Invalid role."); 
                continue
            if users[target]["role"] == "ADMIN" and new_role != "ADMIN" and count_admins(users) == 1:
                print("[!] Refusing: last ADMIN would be demoted."); continue
            users[target]["role"] = new_role
            print(f"[+] '{target}' role changed to {new_role}.")

        elif action in {"d", "del", "delete", "remove"}:
            target = input("Username to delete: ").strip()
            if target not in users: 
                print("[!] User not found.");
                continue
            if target == current_user:
                print("[!] You cannot delete your own account while logged in.");
                continue
            if target == "admin":
                print("[!] You cannot delete the built-in 'admin' user.");
                continue
            if users[target]["role"] == "ADMIN" and count_admins(users) == 1:
                print("[!] Refusing: would remove the last ADMIN account.")
                continue
            if input(f"Type 'yes' to permanently delete '{target}': ").strip().lower() != "yes":
                print("[i] Cancelled.")
                continue
            user_path = users[target].get("path")
            try:
                if user_path and os.path.isdir(user_path):
                    shutil.rmtree(user_path, ignore_errors=True)
            except Exception as e:
                print(f"[!] Warning: failed to remove directory '{user_path}': {e}")
            del users[target]
            print(f"[+] User '{target}' deleted.")
        elif action in {"h", "home", "changehome"}:
            target = input("Username to change home: ").strip()
            if target not in users: 
                print("[!] User not found.")
                continue
            if target == "admin": 
                print("[!] Cannot change built-in 'admin'")
                continue
            user_home = os.path.join(CURRENT_PATH, "users", target)
            libc = ctypes.CDLL("libc.so.6")
            libc.setenv.argtypes = [ctypes.c_char_p, ctypes.c_char_p, ctypes.c_int]
            libc.getenv.restype = ctypes.c_char_p
            current_path = libc.getenv(b"PATH")
            if current_path:
                current_path = current_path.decode()
            home = f"{user_home}:{current_path}".encode()
            libc.setenv(b"PATH", home, 1)
            current_path = libc.getenv(b"PATH")
            print(current_path)
        else:
            print("[!] Unknown manage action.")

# --- Main ---
def main():
    current_user = "GUEST"
    is_logged_in = False
    role = ""

    os.makedirs(Users["admin"]["path"], exist_ok=True)

    while True:
        show_menu(is_logged_in, is_admin=(role == "ADMIN"))
        choice = input("Enter your option: ").lower().strip()

        if not is_logged_in and choice not in GUEST_COMMANDS:
            print("[!] You must be logged in to use this command.")
            continue
        
        if choice in list(GUEST_COMMANDS)[1:] and current_user in ["ADMIN", "USER"]:
            print("[!] You are already logged in.")
            continue

        if choice == "login":
            username = input("Enter your username: ").strip()
            password = input("Enter your password: ").strip()
            valid, exists, msg = check_username(Users, username, "Login")
            if not (valid and exists):
                print(msg)
                continue
            if Users[username]["password"] != password: print(f"[!] Wrong password for {username}"); continue
            current_user, is_logged_in, role = username, True, Users[username]["role"]
            print(f"[+] Welcome back, {current_user}! \n[+] role: {print_role(role)}!!".format(current_user, print_role))

        elif choice == "register":
            username = input("Enter your username: ").strip()
            password = input("Enter your password: ").strip()
            valid, available, msg = check_username(Users, username, "Register")
            if not (valid and available):
                print(msg)
                continue
            user_path = os.path.join("users", username)
            Users[username] = {"role": "USER", "path": user_path, "password": password}
            os.makedirs(user_path, exist_ok=True)
            process_home(user_path)
            print(f"[+] Successfully registered user '{username}'!")

        elif choice == "profile":
            print(f"[i] Logged in as: {current_user} ({role})")
            user_path = Users[current_user]["path"]
            print(f"[i] User directory: {user_path}")
            docs = list_user_docs(user_path)
            if docs:
                print("[i] Your documents:")
                for i, n in enumerate(docs, 1): print(f"  {i}. {n}")
            else:
                print("[i] You have no documents yet.")

        elif choice == "global_docs":
            doc = input("Enter the global docs: ").strip()
            if doc in Docs: help(Docs[doc])
            else: print("[i] Nope.")

        elif choice == "create_docs":
            _ = input("Enter the Documentation name (optional): ").strip()
            filename = input("Enter the filename: ").strip()
            if not is_safe_filename(filename):
                print("[!] Invalid filename. Allowed: letters, digits, underscore, dot; length 3..60.")
                continue
            try:
                b64 = input("Enter the file content as base64: ").strip()
                data = base64.b64decode(b64, validate=True)
            except Exception:
                print("[!] Invalid base64. Please try again."); continue

            if len(data) > 250:
                print("[!] File too large — maximum 250 bytes allowed.")
                continue
            
            user_path = Users[current_user]["path"]
            os.makedirs(user_path, exist_ok=True)
            full_path = os.path.join(user_path, filename)
            try:
                with open(full_path, "wb") as f: f.write(data)
                set_perm(full_path)
            except Exception as e:
                pass

        elif choice == "read_docs":
            user_path = Users[current_user]["path"]
            docs = list_user_docs(user_path)
            if not docs:
                print("[i] You have no documents.");
                continue
            
            print("[i] Your documents:")
            for idx, name in enumerate(docs, 1):
                print(f"  {idx}. {name}")
                
            sel = input("Enter number or filename: ").strip()
            filename = docs[int(sel)-1] if sel.isdigit() and 1 <= int(sel) <= len(docs) else sel
            if not is_safe_filename(filename):
                print("[!] Invalid filename.")
                continue
            
            full_path = os.path.join(user_path, filename)
            if not os.path.isfile(full_path):
                print("[!] File not found.")
                continue
            
            try:
                with open(full_path, "rb") as fp: 
                    data = fp.read()
                try:
                    print("\n----- FILE START -----")
                    print(data.decode("utf-8"))
                    print("----- FILE END -----\n")
                except UnicodeDecodeError:
                    print("[i] (Binary) size:", len(data), "bytes")
                    print("[i] First 256 bytes (hex):", data[:256].hex())
            except Exception as e:
                print(f"[!] Failed to read file: {e}")

        elif choice == "remove_docs":
            user_path = Users[current_user]["path"]
            docs = list_user_docs(user_path)
            if not docs: print("[i] You have no documents to remove."); continue
            print("[i] Your documents:")
            for idx, name in enumerate(docs, 1): print(f"  {idx}. {name}")
            sel = input("Pick to remove (number or filename): ").strip()
            filename = docs[int(sel)-1] if sel.isdigit() and 1 <= int(sel) <= len(docs) else sel
            
            if not is_safe_filename(filename):
                print("[!] Invalid filename.")
                continue
            
            full_path = os.path.join(user_path, filename)
            if not os.path.isfile(full_path): 
                print("[!] File not found.")
                continue
            
            if input(f"Type 'yes' to delete '{filename}': ").strip().lower() != "yes":
                print("[i] Cancelled.")
                continue
            
            try:
                os.remove(full_path)
                print(f"[+] Removed '{filename}'.")
            except Exception as e:
                print(f"[!] Failed to remove: {e}")

        elif choice == "manage_users":
            if role != "ADMIN":
                print("[!] Access denied.")
                continue
            manage_users(Users, current_user)

        elif choice == "logout":
            print("[+] You have been logged out.")
            current_user, is_logged_in, role = "GUEST", False, ""

        elif choice == "exit":
            print("Goodbye!")
            break

        else:
            print(f"[!] Unknown command: {choice}")

if __name__ == "__main__":
    main()
