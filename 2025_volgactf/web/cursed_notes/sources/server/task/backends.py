from django.contrib.auth.backends import BaseBackend
from users.models import UnixUser
import pexpect
import shlex
import logging

logger = logging.getLogger(__name__)

class Backend(BaseBackend):

    def authenticate(self, request, username=None, password=None, **kwargs):
        try:
            logger.error(f"Authenticating user: {username}")
            command = shlex.join(["/bin/su", username, "-c", "echo userid-$(id -u)"])
            child = pexpect.spawn("/bin/su", ["user", "-c", command])
            
            child.expect('Password')
            child.sendline(password)

            i = child.expect(["userid-(\d)+", f'su: user {username} does not exist or the user entry does not contain all the required fields', 'su: Authentication failure', pexpect.EOF])

            logger.error(f"child.after: {child.after.decode('utf-8')}, child.before: {child.before.decode('utf-8')}")
            user_id = None
            if i == 0:
                user_id = child.after.decode('utf-8').strip().split("userid-")[1]
                user_id = int(user_id)

            logger.error(f"user_id: {user_id}")
            child.close()

            if user_id:
                try:
                    user = UnixUser.objects.get(id=user_id)
                    return user
                except UnixUser.DoesNotExist:
                    return None
            
            return None    
        except pexpect.TIMEOUT:
            return None
        except pexpect.EOF:
            return None
    
    def get_user(self, user_id):
        try:
            return UnixUser.objects.get(id=user_id)
        except UnixUser.DoesNotExist:
            return None
