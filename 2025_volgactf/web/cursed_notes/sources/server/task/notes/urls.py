from django.urls import path
from .views import (
    NotesView, 
    NoteDetailView,
    NoteCreateView,
    NoteUpdateView,
    NoteDeleteView,
    NoteSearchView
)

urlpatterns = [
    # Web UI endpoints
    path('', NotesView.as_view(), name='notes_list'),
    path('create/', NoteCreateView.as_view(), name='create_note'),
    path('<int:note_id>/', NoteDetailView.as_view(), name='note_detail'),
    path('<int:note_id>/edit/', NoteUpdateView.as_view(), name='update_note'),
    path('<int:note_id>/delete/', NoteDeleteView.as_view(), name='delete_note'),
    path('search/', NoteSearchView.as_view(), name='search_notes'),
]
