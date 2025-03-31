from django import template
from django.utils.html import conditional_escape

register = template.Library()

@register.filter(name='safe_html')
def safe_html(value):
    print(value)
    return conditional_escape(value)
