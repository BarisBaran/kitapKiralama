document.getElementById('register-form').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const adSoyad = document.getElementById('adSoyad').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    const errorDiv = document.getElementById('error-message');
    const successDiv = document.getElementById('success-message');
    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';
    
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                adSoyad: adSoyad,
                email: email,
                sifre: password
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            successDiv.textContent = 'Kayıt başarılı! Giriş sayfasına yönlendiriliyorsunuz...';
            successDiv.style.display = 'block';
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        } else {
            errorDiv.textContent = data.message || 'Kayıt başarısız!';
            errorDiv.style.display = 'block';
        }
    } catch (error) {
        errorDiv.textContent = 'Bir hata oluştu: ' + error.message;
        errorDiv.style.display = 'block';
    }
});
