// Master Controller for UI Interactions
document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Smooth page fade-in
    document.body.style.opacity = '0';
    setTimeout(() => { 
        document.body.style.opacity = '1'; 
        document.body.style.transition = 'opacity 0.4s ease'; 
    }, 50);

    // 2. Global Click Listener (Works for Forms AND Dashboard Table)
    document.addEventListener('click', function(e) {
        
        // Find the button that was clicked (eye icon or copy icon)
        const target = e.target.closest('.toggle-pass, .btn-toggle, .btn-copy');
        if (!target) return; // If they didn't click a button, do nothing

        // --- SHOW / HIDE PASSWORD LOGIC ---
        if (target.classList.contains('toggle-pass') || target.classList.contains('btn-toggle')) {
            
            // Find the parent container (input-wrap for forms, pass-cell for dashboard)
            const parent = target.closest('.input-wrap') || target.closest('.pass-cell');
            if (!parent) return;

            // Find the input field or the text span
            const element = parent.querySelector('input') || parent.querySelector('.pass-value');
            const icon = target.querySelector('i');

            if (element && icon) {
                // If it's a Form Input (Login, Register, Add, Edit)
                if (element.tagName === 'INPUT') {
                    if (element.type === 'password') {
                        element.type = 'text';
                        icon.classList.remove('fa-eye');
                        icon.classList.add('fa-eye-slash');
                    } else {
                        element.type = 'password';
                        icon.classList.remove('fa-eye-slash');
                        icon.classList.add('fa-eye');
                    }
                } 
                // If it's the Dashboard Table
                else if (element.classList.contains('pass-value')) {
                    if (element.dataset.hidden === 'true') {
                        element.textContent = element.dataset.pass;
                        element.dataset.hidden = 'false';
                        icon.classList.remove('fa-eye');
                        icon.classList.add('fa-eye-slash');
                    } else {
                        element.textContent = '••••••••';
                        element.dataset.hidden = 'true';
                        icon.classList.remove('fa-eye-slash');
                        icon.classList.add('fa-eye');
                    }
                }
            }
        }

        // --- COPY PASSWORD LOGIC ---
        if (target.classList.contains('btn-copy')) {
            const passCell = target.closest('.pass-cell');
            if (passCell) {
                const span = passCell.querySelector('.pass-value');
                if (span && span.dataset.pass) {
                    navigator.clipboard.writeText(span.dataset.pass).then(() => {
                        const icon = target.querySelector('i');
                        if(icon) {
                            icon.className = 'fa-solid fa-check';
                            icon.style.color = '#10b981'; // Green color
                            setTimeout(() => {
                                icon.className = 'fa-solid fa-copy';
                                icon.style.color = '';
                            }, 1500);
                        }
                    });
                }
            }
        }
    });
});