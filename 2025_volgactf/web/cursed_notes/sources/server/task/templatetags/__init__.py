from django import template
from django.utils.html import escape

register = template.Library()

@register.filter(name='safe_html')
def safe_html(value):
    return "huiu"
