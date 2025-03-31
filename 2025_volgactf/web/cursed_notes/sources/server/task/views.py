from django.http import JsonResponse
from django.views import View
from django.contrib.auth import authenticate, login
from django.views.generic.base import TemplateView
from django_weasyprint import WeasyTemplateResponse
from django.views.generic import TemplateView, View
from django.http import HttpResponse
from django.contrib.auth import authenticate, login, logout
from django.shortcuts import render, redirect
from django.contrib.auth.mixins import LoginRequiredMixin
from django.contrib import messages
from django.urls import reverse
from users.models import UnixUser
from django import forms

class MyView(TemplateView):
    def dispatch(self, request, *args, **kwargs):
        print(request.user)
        if request.method == 'GET' and request.GET.get('pdf') != None:
            return self.pdf(request, *args, **kwargs)
        return super(TemplateView, self).dispatch(request, *args, **kwargs)

    def pdf(self, request, *args, **kwargs):
        context = {}
        if hasattr(self, 'get_context_data'):
            context = self.get_context_data(**kwargs)
        return WeasyTemplateResponse(request, self.template_name, context)

class LoginForm(forms.Form):
    username = forms.CharField(
        max_length=150, 
        widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder': 'Username'})
    )
    password = forms.CharField(
        widget=forms.PasswordInput(attrs={'class': 'form-control', 'placeholder': 'Password'})
    )

class RegisterForm(forms.Form):
    username = forms.CharField(
        max_length=150, 
        widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder': 'Username'})
    )
    password = forms.CharField(
        widget=forms.PasswordInput(attrs={'class': 'form-control', 'placeholder': 'Password'})
    )
    password_confirm = forms.CharField(
        widget=forms.PasswordInput(attrs={'class': 'form-control', 'placeholder': 'Confirm Password'}),
        label="Confirm Password"
    )

    def clean(self):
        cleaned_data = super().clean()
        password = cleaned_data.get('password')
        password_confirm = cleaned_data.get('password_confirm')

        if password and password_confirm and password != password_confirm:
            self.add_error('password_confirm', "Passwords don't match")
        
        return cleaned_data

class LoginView(MyView):
    template_name = 'auth/login.html'
    
    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        context['form'] = LoginForm()
        return context
    
    def get(self, request, *args, **kwargs):
        if request.user.is_authenticated:
            return redirect('notes_list')
        return super().get(request, *args, **kwargs)
    
    def post(self, request):
        form = LoginForm(request.POST)
        if form.is_valid():
            username = form.cleaned_data['username']
            password = form.cleaned_data['password']

            if not UnixUser.objects.filter(username=username).exists():
                messages.error(request, "Invalid username")
                return render(request, self.template_name, {'form': form})
            
            user = authenticate(request, username=username, password=password)
            
            if user is not None:
                login(request, user)
                next_url = request.GET.get('next', reverse('notes_list'))
                return redirect(next_url)
            else:
                messages.error(request, "Invalid password.")
        
        return render(request, self.template_name, {'form': form})

class RegisterView(MyView):
    template_name = 'auth/register.html'
    
    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        context['form'] = RegisterForm()
        return context
    
    def get(self, request, *args, **kwargs):
        if request.user.is_authenticated:
            return redirect('notes_list')
        return super().get(request, *args, **kwargs)
    
    def post(self, request):
        form = RegisterForm(request.POST)
        if form.is_valid():
            username = form.cleaned_data['username']
            password = form.cleaned_data['password']
            
            try:
                UnixUser.objects.create_user(username=username, password=password)
                
                messages.success(request, f"Account created for {username}! Please login.")
                return redirect('login')
            except ValueError as e:
                messages.error(request, str(e))
        
        return render(request, self.template_name, {'form': form})

class LogoutView(View):
    def get(self, request):
        logout(request)
        messages.info(request, "You have been logged out.")
        return redirect('login')

class ProfileView(LoginRequiredMixin, MyView):
    template_name = 'auth/profile.html'
    
    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        context['user'] = self.request.user
        return context 

class DeleteAccountView(MyView):
    template_name = 'auth/delete_account.html'
    
    def get(self, request, *args, **kwargs):
        return render(request, self.template_name)
    
    def post(self, request):
        request.user.delete()
        logout(request)
        messages.info(request, "You have been logged out.")
        return redirect('login')

