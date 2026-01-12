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

// Kiralamaları yükle
async function loadRentals() {
    const rentalsList = document.getElementById('rentals-list');
    const errorDiv = document.getElementById('error-message');
    const successDiv = document.getElementById('success-message');
    
    try {
        const response = await fetch('/api/rentals/my-rentals', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            const rentals = data.data;
            
            if (rentals.length === 0) {
                rentalsList.innerHTML = '<p>Henüz kiralama yapmadınız.</p>';
                return;
            }
            
            rentalsList.innerHTML = rentals.map(rental => `
                <div class="rental-item">
                    <div>
                        <h3>${rental.kitapBaslik}</h3>
                        <p>Kiralama Tarihi: ${rental.kiralamaTarihi}</p>
                        <p>İade Tarihi: ${rental.iadeTarihi}</p>
                        <p>Durum: ${rental.durum === 'KIRADA' ? 'Kiralandı' : 'İade Edildi'}</p>
                    </div>
                    ${rental.durum === 'KIRADA' ? 
                        `<button class="btn btn-primary return-btn" data-rental-id="${rental.id}">İade Et</button>` : 
                        '<span style="color: green;">İade Edildi</span>'
                    }
                </div>
            `).join('');
            
            // İade butonları
            document.querySelectorAll('.return-btn').forEach(button => {
                button.addEventListener('click', async function() {
                    const rentalId = this.getAttribute('data-rental-id');
                    
                    try {
                        const response = await fetch(`/api/rentals/${rentalId}/iade`, {
                            method: 'POST',
                            headers: {
                                'Authorization': 'Bearer ' + token
                            }
                        });
                        
                        const data = await response.json();
                        
                        if (response.ok && data.success) {
                            successDiv.textContent = 'Kitap başarıyla iade edildi!';
                            successDiv.style.display = 'block';
                            setTimeout(() => {
                                loadRentals();
                            }, 1000);
                        } else {
                            errorDiv.textContent = data.message || 'İade başarısız!';
                            errorDiv.style.display = 'block';
                        }
                    } catch (error) {
                        errorDiv.textContent = 'Bir hata oluştu: ' + error.message;
                        errorDiv.style.display = 'block';
                    }
                });
            });
        } else {
            rentalsList.innerHTML = '<p>Kiralamalar yüklenirken bir hata oluştu.</p>';
        }
    } catch (error) {
        rentalsList.innerHTML = '<p>Bir hata oluştu: ' + error.message + '</p>';
    }
}

loadRentals();
