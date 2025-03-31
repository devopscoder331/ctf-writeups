from django.urls import path, include
from django.contrib import admin
from .views import (
    LoginView, 
    RegisterView, 
    LogoutView, 
    ProfileView,
    DeleteAccountView
)


urlpatterns = [
    path("notes/", include("task.notes.urls")),
    path("login/", LoginView.as_view(), name="login"),
    path("register/", RegisterView.as_view(), name="register"),
    path("logout/", LogoutView.as_view(), name="logout"),
    path("profile/", ProfileView.as_view(), name="profile"),
    path("profile/delete/", DeleteAccountView.as_view(), name="delete_account"),
    path("", LoginView.as_view(), name="home"),
    path("admin/", admin.site.urls),
]
