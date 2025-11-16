#!/usr/bin/env python3
import sys
from pathlib import Path
from playwright.sync_api import sync_playwright, TimeoutError as PWTimeout

BASE_URL = "http://localhost:5000"
ADMIN_USERNAME = "adminnnnnnn"
ADMIN_PASSWORD = "adminnnnnnn"
LOCKFILE = Path("bot4.lock")

def route_force_identity(route, request):
    try:
        headers = dict(request.headers)
        headers.pop("accept-encoding", None)
        headers.pop("Accept-Encoding", None)
        headers["Accept-Encoding"] = "identity"
        route.continue_(headers=headers)
    except Exception:
        # fallback: continue normally
        route.continue_()

def visit(target_url: str):
    if LOCKFILE.exists():
        print("[xssbot] another visit is ongoing. exiting.")
        return 1

    try:
        LOCKFILE.touch(exist_ok=False)
    except Exception:
        # if touch failed for any reason, continue but warn
        print("[xssbot] warning: could not create lockfile, continuing")

    p = None
    browser = None
    context = None
    try:
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            context = browser.new_context()

            # use a page and install routing BEFORE navigations
            page = context.new_page()
            page.route("**/*", route_force_identity)

            # short helper to avoid exceptions if elements are missing
            def safe_fill(selector, value):
                try:
                    if page.locator(selector).count() > 0:
                        page.fill(selector, value)
                        return True
                except Exception:
                    pass
                return False

            print("[xssbot] visiting admin login...")
            try:
                page.goto(f"{BASE_URL}/admin_login", wait_until="domcontentloaded", timeout=15_000)
            except PWTimeout:
                print("[xssbot] warning: admin_login domcontentloaded timed out (continuing)")

            # try to fill & submit
            if safe_fill('input[name="username"]', ADMIN_USERNAME):
                print("[xssbot] filled username")
            else:
                print("[xssbot] username input not found")

            if safe_fill('input[name="password"]', ADMIN_PASSWORD):
                print("[xssbot] filled password")
            else:
                print("[xssbot] password input not found")

            try:
                if page.locator('button[type="submit"]').count() > 0:
                    page.click('button[type="submit"]')
                    print("[xssbot] clicked login submit")
            except Exception as e:
                print(f"[xssbot] could not click submit: {e}")

            # small wait to allow login redirect/session set
            page.wait_for_timeout(2000)
            print(f"[xssbot] after login url: {page.url}")

            # visit the target URL but wait only for DOMContentLoaded
            print(f"[xssbot] visiting target: {target_url}")
            try:
                page.goto(target_url, wait_until="domcontentloaded", timeout=15_000)
            except PWTimeout:
                print("[xssbot] warning: target domcontentloaded timed out (continuing)")

            # give JS a short window to run (XSS exfil, etc.)
            page.wait_for_timeout(5000)

            # optional: print small snippet to confirm content (first 400 chars)
            try:
                snippet = page.content()[:4000]
                print("[xssbot] page snippet:", snippet.replace("\n", " ")[:400])
            except Exception:
                pass

            print("[xssbot] done - closing browser")
            # cleanup
            try:
                page.unroute("**/*", route_force_identity)
            except Exception:
                pass

            context.close()
            browser.close()

    except Exception as e:
        print("[xssbot] fatal error:", e)
        # try to close resources gracefully
        try:
            if context:
                context.close()
        except Exception:
            pass
        try:
            if browser:
                browser.close()
        except Exception:
            pass
        return 2
    finally:
        try:
            if LOCKFILE.exists():
                LOCKFILE.unlink()
        except Exception:
            pass

    return 0

if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(1)
    sys.exit(visit(sys.argv[1]))

