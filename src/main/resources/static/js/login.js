document.getElementById('login-form').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    const errorDiv = document.getElementById('error-message');
    const successDiv = document.getElementById('success-message');
    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email,
                sifre: password
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            localStorage.setItem('token', data.data.token);
            localStorage.setItem('user', JSON.stringify(data.data.user));
            successDiv.textContent = 'Giriş başarılı! Yönlendiriliyorsunuz...';
            successDiv.style.display = 'block';
            setTimeout(() => {
                window.location.href = '/books';
            }, 1000);
        } else {
            errorDiv.textContent = data.message || 'Giriş başarısız!';
            errorDiv.style.display = 'block';
        }
    } catch (error) {
        errorDiv.textContent = 'Bir hata oluştu: ' + error.message;
        errorDiv.style.display = 'block';
    }
});
