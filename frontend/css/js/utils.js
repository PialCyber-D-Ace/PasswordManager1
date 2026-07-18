/* ============ API BASE ============ */
const API = ''; // same origin

/* ============ TOAST ============ */
function toast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const icons = { success: 'fa-circle-check', error: 'fa-circle-xmark', info: 'fa-circle-info' };
    const el = document.createElement('div');
    el.className = `toast ${type}`;
    el.innerHTML = `<i class="fa-solid ${icons[type]}"></i><span class="msg">${message}</span>`;
    container.appendChild(el);
    setTimeout(() => {
        el.style.opacity = '0';
        el.style.transform = 'translateX(100%)';
        setTimeout(() => el.remove(), 300);
    }, 3000);
}

/* ============ CONFIRM MODAL ============ */
function confirmDialog(title, message) {
    return new Promise(resolve => {
        const backdrop = document.createElement('div');
        backdrop.className = 'modal-backdrop active';
        backdrop.innerHTML = `
            <div class="modal">
                <h3>${title}</h3>
                <p>${message}</p>
                <div class="modal-actions">
                    <button class="btn btn-ghost" data-action="cancel">Cancel</button>
                    <button class="btn btn-danger" data-action="ok">Confirm</button>
                </div>
            </div>`;
        document.body.appendChild(backdrop);
        backdrop.addEventListener('click', (e) => {
            const action = e.target.dataset.action;
            if (action === 'ok') { resolve(true); backdrop.remove(); }
            else if (action === 'cancel' || e.target === backdrop) { resolve(false); backdrop.remove(); }
        });
    });
}

/* ============ SESSION ============ */
const Session = {
    set(user) { localStorage.setItem('pm_user', JSON.stringify(user)); },
    get() {
        const data = localStorage.getItem('pm_user');
        return data ? JSON.parse(data) : null;
    },
    clear() { localStorage.removeItem('pm_user'); },
    requireAuth(redirect = 'login.html') {
        if (!this.get()) { window.location.href = redirect; }
    }
};

/* ============ PASSWORD STRENGTH ============ */
function checkStrength(password) {
    let score = 0;
    if (!password) return { score: 0, label: '', color: '' };
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    const levels = [
        { min: 0, label: '', color: '' },
        { min: 1, label: 'Very Weak', color: '#ef4444' },
        { min: 2, label: 'Weak', color: '#f59e0b' },
        { min: 3, label: 'Fair', color: '#f59e0b' },
        { min: 4, label: 'Strong', color: '#10b981' },
        { min: 5, label: 'Very Strong', color: '#22d3ee' }
    ];
    const level = levels.filter(l => score >= l.min).pop();
    return { score, ...level, percent: (score / 5) * 100 };
}

/* ============ PASSWORD GENERATOR ============ */
function generatePassword(length = 16) {
    const lower = 'abcdefghijklmnopqrstuvwxyz';
    const upper = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const nums = '0123456789';
    const syms = '!@#$%^&*()_+-=[]{}|;:,.<>?';
    const all = lower + upper + nums + syms;
    let pass = '';
    // Guarantee at least one of each type
    pass += lower[Math.floor(Math.random() * lower.length)];
    pass += upper[Math.floor(Math.random() * upper.length)];
    pass += nums[Math.floor(Math.random() * nums.length)];
    pass += syms[Math.floor(Math.random() * syms.length)];
    for (let i = pass.length; i < length; i++) {
        pass += all[Math.floor(Math.random() * all.length)];
    }
    return pass.split('').sort(() => Math.random() - 0.5).join('');
}

/* ============ API CALL ============ */
async function api(path, options = {}) {
    const { method = 'GET', body, query } = options;
    let url = API + path;
    if (query) {
        const qs = new URLSearchParams(query).toString();
        url += (url.includes('?') ? '&' : '?') + qs;
    }
    const fetchOpts = { method, headers: { 'Content-Type': 'application/x-www-form-urlencoded' } };
    if (body) fetchOpts.body = new URLSearchParams(body).toString();
    const res = await fetch(url, fetchOpts);
    const text = await res.text();
    try { return { ok: res.ok, status: res.status, data: JSON.parse(text) }; }
    catch { return { ok: res.ok, status: res.status, data: text }; }
}

/* ============ COPY TO CLIPBOARD ============ */
async function copyText(text) {
    try {
        await navigator.clipboard.writeText(text);
        return true;
    } catch {
        // Fallback
        const ta = document.createElement('textarea');
        ta.value = text; document.body.appendChild(ta);
        ta.select();
        try { document.execCommand('copy'); } catch (e) { ta.remove(); return false; }
        ta.remove();
        return true;
    }
}

/* ============ FAVICON / LOGO ============ */
function siteLogo(name) {
    if (!name) return '?';
    return name.trim().charAt(0).toUpperCase();
}