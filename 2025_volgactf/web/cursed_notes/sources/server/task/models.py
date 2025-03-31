from django.db import models
from django.conf import settings
import markdown2
from django.utils.html import escape, mark_safe


class NoteManager(models.Manager):
    use_in_migrations = True

class Note(models.Model):
    title = models.CharField(max_length=200)
    description = models.TextField()
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='notes')

    def __str__(self):
        return self.title

    def save(self, *args, **kwargs):
        self.description = markdown2.markdown(self.description, safe_mode="escape")
        super().save(*args, **kwargs)

    def __html__(self):
        return mark_safe(f"""
    <div class="card">
        <div class="card-body">
            <div class="note-content mb-4">
                { self.description }
            </div>
            
            <p class="text-muted">
                <small>By { self.owner.username }</small>
            </p>
            
            <div class="mt-4">
                <a href="/notes/{self.id}/edit/" class="btn btn-primary">
                    <i class="bi bi-pencil"></i> Edit
                </a>
                <a href="/notes/{self.id}/delete/" class="btn btn-danger ms-2">
                    <i class="bi bi-trash"></i> Delete
                </a>
            </div>
        </div>
        """)
