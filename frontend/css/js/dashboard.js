/* ============ INIT ============ */
Session.requireAuth();
const user = Session.get();

// Populate welcome
const welcomeEl = document.getElementById('welcomeName');
if (welcomeEl) welcomeEl.textContent = user.fullName;

// Logout
document.getElementById('logoutBtn')?.addEventListener('click', async () => {
    const ok = await confirmDialog('Logout?', 'Are you sure you want to sign out?');
    if (ok) { Session.clear(); window.location.href = 'login.html'; }
});

/* ============ LOAD PASSWORDS ============ */
let allPasswords = [];

async function loadPasswords(query = '') {
    const res = await api('/api/passwords', {
        query: { email: user.email, q: query }
    });
    if (!res.ok) { toast('Failed to load passwords', 'error'); return; }
    allPasswords = res.data;
    renderStats();
    renderTable();
}

function renderStats() {
    const totalEl = document.getElementById('statTotal');
    const recentEl = document.getElementById('statRecent');
    const categoriesEl = document.getElementById('statCategories');
    const strongEl = document.getElementById('statStrong');

    if (totalEl) totalEl.textContent = allPasswords.length;
    if (recentEl) {
        const weekAgo = Date.now() - 7 * 24 * 60 * 60 * 1000;
        const recent = allPasswords.filter(p => new Date(p.createdAt).getTime() > weekAgo).length;
        recentEl.textContent = recent;
    }
    if (categoriesEl) {
        const cats = new Set(allPasswords.map(p => p.category).filter(Boolean));
        categoriesEl.textContent = cats.size;
    }
    if (strongEl) {
        const strong = allPasswords.filter(p => checkStrength(p.password).score >= 4).length;
        strongEl.textContent = strong;
    }
}

function renderTable() {
    const tbody = document.getElementById('passwordsBody');
    const empty = document.getElementById('emptyState');
    if (!tbody) return;

    if (allPasswords.length === 0) {
        tbody.innerHTML = '';
        if (empty) empty.style.display = 'block';
        return;
    }
    if (empty) empty.style.display = 'none';

    tbody.innerHTML = allPasswords.map(p => `
        <tr data-id="${p.id}">
            <td>
                <div class="site-cell">
                    <div class="site-logo">${siteLogo(p.websiteName)}</div>
                    <div class="site-info">
                        <div class="name">${escapeHtml(p.websiteName)}</div>
                        <div class="url">${escapeHtml(p.websiteUrl || '—')}</div>
                    </div>
                </div>
            </td>
            <td>${escapeHtml(p.username)}</td>
            <td>
                <div class="pass-cell">
                    <span class="pass-value" data-hidden="true" data-pass="${escapeAttr(p.password)}">••••••••</span>
                    <button class="btn-icon btn-toggle" title="Show/Hide"><i class="fa-solid fa-eye"></i></button>
                    <button class="btn-icon btn-copy" title="Copy"><i class="fa-solid fa-copy"></i></button>
                </div>
            </td>
            <td><span style="font-size:12px;padding:4px 10px;border-radius:6px;background:rgba(79,124,255,.15);color:var(--primary);">${escapeHtml(p.category || 'General')}</span></td>
            <td>
                <div class="actions">
                    <button class="btn-icon btn-edit" title="Edit"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn-icon btn-delete" title="Delete" style="color:var(--danger);"><i class="fa-solid fa-trash"></i></button>
                </div>
            </td>
        </tr>
    `).join('');

    // Bind events
    tbody.querySelectorAll('.btn-toggle').forEach(b => b.addEventListener('click', togglePass));
    tbody.querySelectorAll('.btn-copy').forEach(b => b.addEventListener('click', copyPass));
    tbody.querySelectorAll('.btn-edit').forEach(b => b.addEventListener('click', editPass));
    tbody.querySelectorAll('.btn-delete').forEach(b => b.addEventListener('click', deletePass));
}

function togglePass(e) {
    const btn = e.currentTarget;
    const span = btn.parentElement.querySelector('.pass-value');
    const icon = btn.querySelector('i');
    if (span.dataset.hidden === 'true') {
        span.textContent = span.dataset.pass;
        span.dataset.hidden = 'false';
        icon.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        span.textContent = '••••••••';
        span.dataset.hidden = 'true';
        icon.classList.replace('fa-eye-slash', 'fa-eye');
    }
}

async function copyPass(e) {
    const btn = e.currentTarget;
    const span = btn.parentElement.querySelector('.pass-value');
    const ok = await copyText(span.dataset.pass);
    toast(ok ? 'Password copied!' : 'Copy failed', ok ? 'success' : 'error');
}

function editPass(e) {
    const id = e.currentTarget.closest('tr').dataset.id;
    window.location.href = `edit.html?id=${id}`;
}

async function deletePass(e) {
    const tr = e.currentTarget.closest('tr');
    const id = tr.dataset.id;
    const name = tr.querySelector('.site-info .name').textContent;
    const ok = await confirmDialog('Delete password?', `Are you sure you want to delete "${name}"? This cannot be undone.`);
    if (!ok) return;

    const res = await api('/api/passwords', {
        method: 'DELETE',
        query: { email: user.email, id }
    });
    if (res.ok && res.data.success) {
        toast('Password deleted', 'success');
        loadPasswords();
    } else {
        toast('Delete failed', 'error');
    }
}

/* ============ SEARCH ============ */
const searchInput = document.getElementById('searchInput');
if (searchInput) {
    let timer;
    searchInput.addEventListener('input', () => {
        clearTimeout(timer);
        timer = setTimeout(() => loadPasswords(searchInput.value), 250);
    });
}

/* ============ UTIL ============ */
function escapeHtml(s) {
    if (!s) return '';
    return String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}
function escapeAttr(s) { return escapeHtml(s); }

/* ============ GENERATE PASSWORD BUTTON ============ */
document.getElementById('generateBtn')?.addEventListener('click', () => {
    const input = document.getElementById('password');
    if (input) {
        input.value = generatePassword(16);
        input.dispatchEvent(new Event('input'));
    }
});

/* ============ INITIAL LOAD ============ */
if (document.getElementById('passwordsBody')) loadPasswords();