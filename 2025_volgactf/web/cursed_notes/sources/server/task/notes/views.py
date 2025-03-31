from django.views.generic import TemplateView, View, CreateView, UpdateView, DeleteView, DetailView
from django.views.generic.base import TemplateResponseMixin, View
from django.http import HttpResponse
from django.contrib.auth.mixins import LoginRequiredMixin
from task.models import Note
from django.shortcuts import get_object_or_404, render, redirect
from users.models import UnixUser
from django_weasyprint import WeasyTemplateResponse, WeasyTemplateResponseMixin
from django.db.models import Q
from django.urls import reverse, reverse_lazy
from django import forms
from task.views import MyView

class NoteForm(forms.ModelForm):
    class Meta:
        model = Note
        fields = ['title', 'description']
        widgets = {
            'title': forms.TextInput(attrs={'class': 'form-control'}),
            'description': forms.Textarea(attrs={'class': 'form-control', 'rows': 5}),
        }

class NotesView(LoginRequiredMixin, MyView):
    template_name = 'notes/list.html'

    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        context['notes'] = Note.objects.filter(owner=self.request.user)
        user = UnixUser.objects.get(username=self.request.user)
        context['user'] = user
        return context


    def post(self, request):
        title = request.POST.get('title')
        description = request.POST.get('description')
        
        note = Note.objects.create(owner=request.user, title=title, description=description)

        context = self.get_context_data()
        context['current_note_id'] = note.id
        return render(request, self.template_name, context)


class NoteDetailView(LoginRequiredMixin, DetailView):
    template_name = 'notes/detail.html'
    context_object_name = 'note'
    
    def get_object(self):
        note_id = self.kwargs.get('note_id')
        return get_object_or_404(Note, id=note_id, owner=self.request.user)
    
    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        context['note'] = self.object
        return context

class NoteCreateView(LoginRequiredMixin, CreateView):
    model = Note
    form_class = NoteForm
    template_name = 'notes/create.html'
    success_url = reverse_lazy('notes_list')
    
    def form_valid(self, form):
        form.instance.owner = self.request.user
        return super().form_valid(form)

class NoteUpdateView(LoginRequiredMixin, UpdateView):
    model = Note
    form_class = NoteForm
    template_name = 'notes/edit.html'
    context_object_name = 'note'
    
    def get_object(self):
        note_id = self.kwargs.get('note_id')
        return get_object_or_404(Note, id=note_id, owner=self.request.user)
    
    def get_success_url(self):
        return reverse('note_detail', kwargs={'note_id': self.object.id})

class NoteDeleteView(LoginRequiredMixin, DeleteView):
    model = Note
    template_name = 'notes/delete.html'
    context_object_name = 'note'
    success_url = reverse_lazy('notes_list')
    
    def get_object(self):
        note_id = self.kwargs.get('note_id')
        return get_object_or_404(Note, id=note_id, owner=self.request.user)

class NoteSearchView(LoginRequiredMixin, MyView):
    template_name = 'notes/search.html'
    
    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        query = self.request.GET.get('q', '')
        context['query'] = query
        
        if query:
            notes = Note.objects.filter(
                Q(owner=self.request.user) & 
                (Q(title__icontains=query) | Q(description__icontains=query))
            )
        else:
            notes = Note.objects.none()
            
        context['notes'] = notes
        return context


