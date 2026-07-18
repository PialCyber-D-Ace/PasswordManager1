/* ============ REGISTER ============ */
const regForm = document.getElementById('registerForm');
if (regForm) {
    const strengthBar = document.getElementById('strengthBar');
    const strengthText = document.getElementById('strengthText');
    const passInput = document.getElementById('password');

    passInput.addEventListener('input', () => {
        const s = checkStrength(passInput.value);
        strengthBar.style.width = s.percent + '%';
        strengthBar.style.background = s.color;
        strengthText.textContent = s.label;
        strengthText.style.color = s.color;
    });

    regForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const fullName = document.getElementById('fullName').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const confirm = document.getElementById('confirm').value;

        if (!fullName || !email || !password) {
            toast('Please fill in all fields', 'error'); return;
        }
        if (password.length < 6) {
            toast('Password must be at least 6 characters', 'error'); return;
        }
        if (password !== confirm) {
            toast('Passwords do not match', 'error'); return;
        }

        const btn = regForm.querySelector('button[type="submit"]');
        btn.disabled = true;
        btn.innerHTML = '<span class="loader"></span> Creating account...';

        const res = await api('/api/register', {
            method: 'POST',
            body: { fullName, email, password }
        });

        btn.disabled = false;
        btn.innerHTML = 'Create Account';

        if (res.ok && res.data.success) {
            toast('Account created! Please login.', 'success');
            setTimeout(() => window.location.href = 'login.html', 800);
        } else {
            toast(res.data.message || 'Registration failed', 'error');
        }
    });
}

/* ============ LOGIN ============ */
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        if (!email || !password) {
            toast('Please fill in all fields', 'error'); return;
        }

        const btn = loginForm.querySelector('button[type="submit"]');
        btn.disabled = true;
        btn.innerHTML = '<span class="loader"></span> Signing in...';

        const res = await api('/api/login', {
            method: 'POST',
            body: { email, password }
        });

        btn.disabled = false;
        btn.innerHTML = 'Sign In';

        if (res.ok && res.data.success) {
            Session.set({ fullName: res.data.fullName, email: res.data.email });
            toast('Welcome back, ' + res.data.fullName + '!', 'success');
            setTimeout(() => window.location.href = 'dashboard.html', 600);
        } else {
            toast(res.data.message || 'Login failed', 'error');
        }
    });
}

/* ============ TOGGLE PASSWORD VISIBILITY ============ */
document.querySelectorAll('.toggle-pass').forEach(btn => {
    btn.addEventListener('click', () => {
        const input = btn.parentElement.querySelector('input');
        const icon = btn.querySelector('i');
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.replace('fa-eye', 'fa-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.replace('fa-eye-slash', 'fa-eye');
        }
    });
});