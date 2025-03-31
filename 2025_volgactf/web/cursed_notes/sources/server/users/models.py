from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin
from django.db import models
import pexpect
from django.utils.html import mark_safe
import hashlib



class UnixUserManager(BaseUserManager):
    def _create_user(self, username, id):
        child = pexpect.spawn("/sbin/useradd", ["-m", "-u", str(id), "--", username])
        i = child.expect(["useradd: password is empty", f"useradd: user '{username}' already exists", pexpect.EOF])

        if i == 2:
            return
        elif i == 1:
            raise ValueError('User already exists')
        else:
            raise ValueError('User creation failed')

    def _set_password(self, username, password):
        child = pexpect.spawn("/bin/passwd", [username])
        i = child.expect(["New password:", f"passwd: user '{username}' does not exist"])
        if i != 0:
            raise ValueError('User does not exist')

        child.sendline(password)
        child.expect("Retype new password:")
        child.sendline(password)

        i = child.expect(["passwd: password updated successfully", f"passwd: user '{username}' does not exist", pexpect.EOF])
        if i != 0:
            raise ValueError('User does not exist or password change failed')

    def create_user(self, username, password=None, **extra_fields):
        if not username:
            raise ValueError('The Username field must be set')
    

        user_obj = self.model.objects.order_by('-id').first()
        next_id = (user_obj.id + 1) if user_obj else 9000

        self._create_user(username, next_id)
        self._set_password(username, password)

        user = self.model(username=username, id=next_id, **extra_fields)
        user.save(using=self._db)
        return user

    def create_superuser(self, username, password=None, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)

        return self.create_user(username, password, **extra_fields)


class UnixUser(AbstractBaseUser, PermissionsMixin):
    id = models.AutoField(primary_key=True, unique=True, default=9000)
    username = models.CharField(max_length=150, unique=True)
    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)
    is_superuser = models.BooleanField(default=False)
    token = models.CharField(max_length=255, blank=True, null=True) 

    objects = UnixUserManager()

    USERNAME_FIELD = 'username'

    def __str__(self):
        return self.username

    def delete(self, *args, **kwargs):
        child = pexpect.spawn("/sbin/userdel", ["-r", self.username])
        child.expect([pexpect.EOF, f"userdel: user '{self.username}' does not exist"])
        super().delete(*args, **kwargs)

    def __html__(self):
        return mark_safe(f"""
        <div class="card" style="width: 18rem; height: fit-content;">
            <img src="https://www.gravatar.com/avatar/{hashlib.md5(self.username.encode('utf-8')).hexdigest()}?d=identicon&size=512" class="card-img-top" alt="{self.username} avatar">
            <div class="card-body">
                <h5 class="card-title">{self.username}</h5>
                <p class="card-text">
                    <span class="badge {'bg-success' if self.is_active else 'bg-danger'}">
                        {'Active' if self.is_active else 'Inactive'}
                    </span>
                    {' <span class="badge bg-primary">Staff</span>' if self.is_staff else ''}
                    {' <span class="badge bg-warning">Superuser</span>' if self.is_superuser else ''}
                </p>
            </div>
        </div>
        """)
