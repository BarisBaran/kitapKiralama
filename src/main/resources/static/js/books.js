// Token kontrolü
const token = localStorage.getItem('token');
if (!token) {
    window.location.href = '/login';
}

// Çıkış butonu
document.getElementById('logout-btn').addEventListener('click', function() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
});

// Kirala butonları
document.querySelectorAll('.rent-btn').forEach(button => {
    button.addEventListener('click', async function() {
        const bookId = this.getAttribute('data-book-id');
        
        if (this.disabled) {
            return;
        }
        
        const errorDiv = document.getElementById('error-message');
        const successDiv = document.getElementById('success-message');
        errorDiv.style.display = 'none';
        successDiv.style.display = 'none';
        
        try {
            const response = await fetch('/api/rentals/kirala', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify({
                    kitapId: parseInt(bookId)
                })
            });
            
            const data = await response.json();
            
            if (response.ok && data.success) {
                successDiv.textContent = 'Kitap başarıyla kiralandı!';
                successDiv.style.display = 'block';
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            } else {
                errorDiv.textContent = data.message || 'Kiralama başarısız!';
                errorDiv.style.display = 'block';
            }
        } catch (error) {
            errorDiv.textContent = 'Bir hata oluştu: ' + error.message;
            errorDiv.style.display = 'block';
        }
    });
});
