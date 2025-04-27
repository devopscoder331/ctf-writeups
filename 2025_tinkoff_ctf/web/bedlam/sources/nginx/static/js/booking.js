document.addEventListener('DOMContentLoaded', function() {
    const bookingForm = document.getElementById('bookingForm');
    const bookingFile = document.getElementById('booking-file');
    const bookingError = document.getElementById('booking-error');
    const bookingSuccess = document.getElementById('booking-success');
    
    bookingForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        bookingError.textContent = '';
        bookingSuccess.textContent = '';
        
        const button = bookingForm.querySelector('button');
        button.classList.add('loading');
        
        const file = bookingFile.files[0];
        if (!file) {
            bookingError.textContent = 'Please select a file';
            button.classList.remove('loading');
            return;
        }
        
        if (!file.name.endsWith('.docx')) {
            bookingError.textContent = 'Please upload a .docx file';
            button.classList.remove('loading');
            return;
        }
        
        const formData = new FormData();
        formData.append('file', file);
        
        fetch('/api/process-booking', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.error || 'Failed to process letter');
                });
            }
            return response.json();
        })
        .then(data => {
            bookingSuccess.textContent = data.message;
            bookingFile.value = '';
        })
        .catch(error => {
            bookingError.textContent = error.message;
        })
        .finally(() => {
            button.classList.remove('loading');
        });
    });
}); 