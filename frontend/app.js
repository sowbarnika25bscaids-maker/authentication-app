document.getElementById('registerForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent normal browser form submission

    const name = document.getElementById('name').value;
    const phone = document.getElementById('phone').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const messageDiv = document.getElementById('message');

    const payload = {
        name: name,
        phone: phone,
        email: email,
        password: password
    };

    fetch('/api/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (response.status === 201) {
            messageDiv.style.color = 'green';
            messageDiv.innerText = 'Registration successful!';
            document.getElementById('registerForm').reset();
        } else if (response.status === 409) {
            messageDiv.style.color = 'red';
            messageDiv.innerText = 'Error: Email already exists.';
        } else {
            messageDiv.style.color = 'red';
            messageDiv.innerText = 'Error: Registration failed. Please check inputs.';
        }
    })
    .catch(error => {
        messageDiv.style.color = 'red';
        messageDiv.innerText = 'Network error. Could not reach the server.';
    });
});
