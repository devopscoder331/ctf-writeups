document.addEventListener('DOMContentLoaded', function() {

    const boardingPassForm = document.getElementById('boardingPassForm');
    const bpError = document.getElementById('bp-error');

    boardingPassForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        bpError.textContent = '';

        const data = {
            passenger: document.getElementById('bp-passenger').value,
            flight_number: document.getElementById('bp-flight').value,
        };

        try {
            const response = await fetch('/api/boardingpass', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'  
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error('Failed to generate boarding pass');
            }

            const result = await response.blob();  

            const url = window.URL.createObjectURL(result);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'boarding-pass.pdf';
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

            boardingPassForm.reset();
        } catch (error) {
            bpError.textContent = error.message;
        }
    });

    const loungePassForm = document.getElementById('loungePassForm');
    const loungeError = document.getElementById('lounge-error');

    loungePassForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const orderId = document.getElementById('lounge-success');

        const data = {
            passenger: document.getElementById('lounge-passenger').value,
            guests: parseInt(document.getElementById('lounge-guests').value)
        };

        try {
            const response = await fetch('/api/business-lounge', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error('Failed to purchase lounge pass');
            }

            const result = await response.json();
            orderId.textContent = `Lounge pass purchased successfully! Order ID: ${result.orderId}`;
            orderId.style.display = 'block';
            loungePassForm.reset();
        } catch (error) {
            console.log(error.message);
        }
    });

    function showLoading(button) {
        button.classList.add('loading');
    }

    function hideLoading(button) {
        button.classList.remove('loading');
    }

    document.getElementById('boardingPassForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const button = e.target.querySelector('button');
        showLoading(button);
        await new Promise(resolve => setTimeout(resolve, 1500));
        hideLoading(button);
    });

    document.getElementById('loungePassForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const button = e.target.querySelector('button');
        showLoading(button);
        await new Promise(resolve => setTimeout(resolve, 1500));
        hideLoading(button);
    });
});