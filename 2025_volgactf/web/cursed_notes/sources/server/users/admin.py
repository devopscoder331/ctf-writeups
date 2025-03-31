from django.contrib import admin
from .models import UnixUser
from django.contrib.auth.forms import UserChangeForm, UserCreationForm

@admin.register(UnixUser)
class UnixUserAdmin(admin.ModelAdmin):

    add_form = UserCreationForm
    form = UserChangeForm
    list_display = ('username', 'is_active', 'is_staff', 'is_superuser', 'token')
    search_fields = ('username',)
    ordering = ('username',)
    list_filter = ('is_active', 'is_staff', 'is_superuser')
    fieldsets = (
        (None, {'fields': ('username', 'password')}),
        ('Permissions', {'fields': ('is_active', 'is_staff', 'is_superuser', 'token')}),
    )
