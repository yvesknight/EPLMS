/* ===== STATE ===== */
const API = '';
let state = { token: null, user: null, allLeaves: [] };

/* ===== INIT ===== */
document.addEventListener('DOMContentLoaded', () => {
    const saved = sessionStorage.getItem('eplms_session');
    if (saved) {
        state = JSON.parse(saved);
        bootApp();
    } else {
        showScreen('loginScreen');
    }

    // Set default year in self-review
    document.getElementById('srYear').value = new Date().getFullYear();

    // Date change → preview days
    ['leaveStart', 'leaveEnd'].forEach(id =>
        document.getElementById(id).addEventListener('change', previewLeaveDays));

    // Role change → show manager select
    document.getElementById('regRole').addEventListener('change', async (e) => {
        const show = e.target.value === 'EMPLOYEE';
        document.getElementById('managerSelectGroup').style.display = show ? 'block' : 'none';
        if (show) await loadManagerOptions();
    });

    // Forms
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    document.getElementById('leaveForm').addEventListener('submit', handleLeaveSubmit);
    document.getElementById('goalForm').addEventListener('submit', handleGoalSubmit);
    document.getElementById('selfReviewForm').addEventListener('submit', handleSelfReview);
    document.getElementById('managerReviewForm').addEventListener('submit', handleManagerReview);
});

/* ===== AUTH ===== */
async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    try {
        const data = await api('POST', '/api/auth/login', { email, password });
        state.token = data.token;
        state.user = { id: data.userId, email: data.email, role: data.role };
        sessionStorage.setItem('eplms_session', JSON.stringify(state));
        bootApp();
    } catch (err) {
        toast(err.message || 'Login failed', 'error');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const role = document.getElementById('regRole').value;
    const payload = {
        name: document.getElementById('regName').value,
        email: document.getElementById('regEmail').value,
        password: document.getElementById('regPassword').value,
        role
    };
    const managerId = document.getElementById('regManagerId').value;
    if (role === 'EMPLOYEE' && managerId) payload.managerId = parseInt(managerId);
    try {
        await api('POST', '/api/auth/register', payload);
        toast('Account created! Please sign in.', 'success');
        showScreen('loginScreen');
    } catch (err) {
        toast(err.message || 'Registration failed', 'error');
    }
}

function logout() {
    sessionStorage.removeItem('eplms_session');
    state = { token: null, user: null, allLeaves: [] };
    showScreen('loginScreen');
}

/* ===== BOOT ===== */
function bootApp() {
    showScreen('appScreen');
    setupRoleUI();
    loadDashboard();
    showTab('dashboard');
}

function setupRoleUI() {
    const { role, email } = state.user;
    document.getElementById('sidebarName').textContent = email.split('@')[0];
    document.getElementById('sidebarRole').textContent = role.replace('_', ' ');
    document.getElementById('sidebarAvatar').textContent = email[0].toUpperCase();
    document.getElementById('dashGreeting').textContent = `Welcome back, ${email.split('@')[0]}`;

    // Show/hide nav items by role
    const isManagerOrHR = role === 'MANAGER' || role === 'HR_MANAGER';
    document.getElementById('navApprovals').style.display = isManagerOrHR ? 'flex' : 'none';
    document.getElementById('navTeam').style.display = isManagerOrHR ? 'flex' : 'none';
    document.getElementById('navLeave').style.display = role === 'EMPLOYEE' ? 'flex' : 'none';
    document.getElementById('navPerf').style.display = 'flex';

    // Manager review section
    if (isManagerOrHR) {
        document.getElementById('managerReviewSection').style.display = 'block';
        document.getElementById('btnSelfReview').style.display = 'none';
    }
}

/* ===== DASHBOARD ===== */
async function loadDashboard() {
    const { role, id } = state.user;
    const statsGrid = document.getElementById('statsGrid');

    if (role === 'EMPLOYEE') {
        const [balance, leaves] = await Promise.all([
            api('GET', `/api/leave/balance/${id}`),
            api('GET', `/api/leave/employee/${id}`)
        ]);
        state.allLeaves = leaves;

        const pending = leaves.filter(l => l.status === 'PENDING').length;
        const approved = leaves.filter(l => ['APPROVED','AUTO_APPROVED'].includes(l.status)).length;

        statsGrid.innerHTML = `
            ${statCard(balance.annualLeave, 'Annual Leave Days', '')}
            ${statCard(balance.sickLeave, 'Sick Leave Days', 'green')}
            ${statCard(pending, 'Pending Requests', 'orange')}
            ${statCard(approved, 'Approved Leaves', 'green')}
        `;
        renderBalanceSummary(balance);
        renderRecentLeaves(leaves.slice(-5).reverse());
    } else {
        const [employees, pending] = await Promise.all([
            api('GET', '/api/users/employees'),
            api('GET', '/api/leave/pending')
        ]);
        statsGrid.innerHTML = `
            ${statCard(employees.length, 'Total Employees', '')}
            ${statCard(pending.length, 'Pending Approvals', 'orange')}
        `;
        document.getElementById('balanceSummary').innerHTML = '<p style="padding:20px;color:var(--text-muted)">Select an employee to view balance.</p>';
        renderRecentLeaves(pending.slice(0, 5));
    }
}

function statCard(value, label, color) {
    return `<div class="stat-card ${color}">
        <div class="stat-value">${value}</div>
        <div class="stat-label">${label}</div>
    </div>`;
}

function renderBalanceSummary(b) {
    document.getElementById('balanceSummary').innerHTML = `
        <div class="balance-item"><div class="balance-days">${b.annualLeave}</div><div class="balance-type">Annual</div></div>
        <div class="balance-item"><div class="balance-days">${b.sickLeave}</div><div class="balance-type">Sick</div></div>
        <div class="balance-item"><div class="balance-days">${b.maternityLeave}</div><div class="balance-type">Maternity</div></div>
        <div class="balance-item"><div class="balance-days">${b.paternityLeave}</div><div class="balance-type">Paternity</div></div>
    `;
}

function renderRecentLeaves(leaves) {
    const el = document.getElementById('recentLeaves');
    if (!leaves.length) { el.innerHTML = '<p class="table-empty">No leave requests yet.</p>'; return; }
    el.innerHTML = `<table class="table">
        <thead><tr><th>Type</th><th>Dates</th><th>Days</th><th>Status</th></tr></thead>
        <tbody>${leaves.map(l => `
            <tr>
                <td>${l.leaveType}</td>
                <td>${l.startDate} → ${l.endDate}</td>
                <td>${l.totalDays}</td>
                <td>${badge(l.status)}</td>
            </tr>`).join('')}
        </tbody>
    </table>`;
}

/* ===== LEAVE TAB ===== */
async function loadLeaveTab() {
    const leaves = await api('GET', `/api/leave/employee/${state.user.id}`);
    state.allLeaves = leaves;
    renderLeaveTable(leaves);
}

function renderLeaveTable(leaves) {
    const tbody = document.getElementById('leaveTableBody');
    if (!leaves.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="table-empty">No leave requests found.</td></tr>';
        return;
    }
    tbody.innerHTML = leaves.map(l => `
        <tr>
            <td>${l.leaveType}</td>
            <td>${l.startDate}</td>
            <td>${l.endDate}</td>
            <td><strong>${l.totalDays}</strong></td>
            <td>${l.reason || '—'}</td>
            <td>${badge(l.status)}</td>
            <td style="font-size:12px;color:var(--text-muted)">${l.approverComment || '—'}</td>
        </tr>`).join('');
}

function filterLeaves() {
    const filter = document.getElementById('leaveFilter').value;
    const filtered = filter ? state.allLeaves.filter(l => l.status === filter) : state.allLeaves;
    renderLeaveTable(filtered);
}

async function handleLeaveSubmit(e) {
    e.preventDefault();
    const payload = {
        employeeId: state.user.id,
        leaveType: document.getElementById('leaveType').value,
        startDate: document.getElementById('leaveStart').value,
        endDate: document.getElementById('leaveEnd').value,
        reason: document.getElementById('leaveReason').value
    };
    try {
        const result = await api('POST', '/api/leave/request', payload);
        toast(`Leave submitted! Status: ${result.status}`, 'success');
        closeModal('leaveModal');
        document.getElementById('leaveForm').reset();
        document.getElementById('leaveDaysPreview').style.display = 'none';
        loadLeaveTab();
        loadDashboard();
    } catch (err) {
        toast(err.message || 'Failed to submit leave', 'error');
    }
}

function previewLeaveDays() {
    const start = document.getElementById('leaveStart').value;
    const end = document.getElementById('leaveEnd').value;
    const preview = document.getElementById('leaveDaysPreview');
    if (start && end) {
        const days = Math.floor((new Date(end) - new Date(start)) / 86400000) + 1;
        if (days > 0) {
            preview.textContent = `📅 ${days} day(s) requested`;
            preview.style.display = 'block';
        }
    }
}

/* ===== APPROVALS TAB ===== */
let currentApprovalId = null;

async function loadApprovalsTab() {
    const pending = await api('GET', '/api/leave/pending');
    const tbody = document.getElementById('approvalsTableBody');
    if (!pending.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="table-empty">No pending approvals.</td></tr>';
        return;
    }
    tbody.innerHTML = pending.map(l => `
        <tr>
            <td>#${l.id}</td>
            <td>${l.employeeName}</td>
            <td>${l.leaveType}</td>
            <td>${l.startDate}</td>
            <td>${l.endDate}</td>
            <td><strong>${l.totalDays}</strong></td>
            <td>${l.reason || '—'}</td>
            <td>
                <div class="btn-group">
                    <button class="btn btn-success btn-sm" onclick="openApprovalModal(${l.id}, 'APPROVE')">✓</button>
                    <button class="btn btn-danger btn-sm" onclick="openApprovalModal(${l.id}, 'REJECT')">✕</button>
                </div>
            </td>
        </tr>`).join('');
}

function openApprovalModal(id, action) {
    currentApprovalId = id;
    document.getElementById('approvalModalTitle').textContent =
        action === 'APPROVE' ? `Approve Leave #${id}` : `Reject Leave #${id}`;
    document.getElementById('approvalComment').value = '';
    openModal('approvalModal');
}

async function submitApproval(action) {
    const comment = document.getElementById('approvalComment').value;
    try {
        await api('PUT', `/api/leave/approve/${currentApprovalId}`, { action, comment });
        toast(`Leave request ${action === 'APPROVE' ? 'approved' : 'rejected'}`, 'success');
        closeModal('approvalModal');
        loadApprovalsTab();
        loadDashboard();
    } catch (err) {
        toast(err.message || 'Action failed', 'error');
    }
}

/* ===== PERFORMANCE TAB ===== */
async function loadPerformanceTab() {
    const { role, id } = state.user;
    const targetId = role === 'EMPLOYEE' ? id : null;

    if (targetId) {
        const [goals, reviews] = await Promise.all([
            api('GET', `/api/performance/goals/employee/${targetId}`),
            api('GET', `/api/performance/review/employee/${targetId}`)
        ]);
        renderGoals(goals);
        renderReviews(reviews);
    } else {
        document.getElementById('goalsList').innerHTML = '<p style="padding:20px;color:var(--text-muted)">Use employee ID to view goals.</p>';
        document.getElementById('reviewsList').innerHTML = '<p style="padding:20px;color:var(--text-muted)">Use the form below to submit manager reviews.</p>';
    }
}

function renderGoals(goals) {
    const el = document.getElementById('goalsList');
    if (!goals.length) { el.innerHTML = '<p class="table-empty">No goals set yet.</p>'; return; }
    el.innerHTML = goals.map(g => `
        <div class="goal-card">
            <div>
                <div class="goal-title">${g.title}</div>
                <div class="goal-meta">Target: ${g.targetDate} · ${g.description || ''}</div>
            </div>
            <div class="goal-actions">
                ${badge(g.status)}
                ${g.status === 'IN_PROGRESS' ? `<button class="btn btn-sm btn-secondary" onclick="markGoalDone(${g.id})">✓ Done</button>` : ''}
            </div>
        </div>`).join('');
}

function renderReviews(reviews) {
    const el = document.getElementById('reviewsList');
    if (!reviews.length) { el.innerHTML = '<p class="table-empty">No reviews yet.</p>'; return; }
    el.innerHTML = reviews.map(r => `
        <div class="review-card">
            <div class="review-header">
                <span class="review-year">${r.reviewYear}</span>
                <span class="review-rating">${r.finalRating ? '★ ' + r.finalRating : badge(r.status)}</span>
            </div>
            <div class="review-text">Self: ${r.selfReview || '—'}</div>
            ${r.managerReview ? `<div class="review-text" style="margin-top:4px">Manager: ${r.managerReview}</div>` : ''}
            ${r.reviewerName ? `<div class="review-text" style="margin-top:4px;color:var(--primary)">Reviewed by: ${r.reviewerName}</div>` : ''}
        </div>`).join('');
}

async function handleGoalSubmit(e) {
    e.preventDefault();
    const payload = {
        employeeId: state.user.id,
        title: document.getElementById('goalTitle').value,
        description: document.getElementById('goalDesc').value,
        targetDate: document.getElementById('goalDate').value
    };
    try {
        await api('POST', '/api/performance/goals', payload);
        toast('Goal added!', 'success');
        closeModal('goalModal');
        document.getElementById('goalForm').reset();
        loadPerformanceTab();
    } catch (err) {
        toast(err.message || 'Failed to add goal', 'error');
    }
}

async function markGoalDone(goalId) {
    try {
        await api('PUT', `/api/performance/goals/${goalId}/status?status=COMPLETED`);
        toast('Goal marked as completed!', 'success');
        loadPerformanceTab();
    } catch (err) {
        toast(err.message || 'Failed to update goal', 'error');
    }
}

async function handleSelfReview(e) {
    e.preventDefault();
    const payload = {
        employeeId: state.user.id,
        reviewYear: parseInt(document.getElementById('srYear').value),
        selfReview: document.getElementById('srText').value,
        selfRating: parseFloat(document.getElementById('srRating').value)
    };
    try {
        await api('POST', '/api/performance/review/self', payload);
        toast('Self-review submitted!', 'success');
        closeModal('selfReviewModal');
        document.getElementById('selfReviewForm').reset();
        document.getElementById('srYear').value = new Date().getFullYear();
        loadPerformanceTab();
    } catch (err) {
        toast(err.message || 'Failed to submit review', 'error');
    }
}

async function handleManagerReview(e) {
    e.preventDefault();
    const payload = {
        reviewId: parseInt(document.getElementById('mrReviewId').value),
        reviewerId: state.user.id,
        managerReview: document.getElementById('mrFeedback').value,
        managerRating: parseFloat(document.getElementById('mrRating').value)
    };
    try {
        const result = await api('POST', '/api/performance/review/manager', payload);
        toast(`Review completed! Final rating: ${result.finalRating}`, 'success');
        document.getElementById('managerReviewForm').reset();
    } catch (err) {
        toast(err.message || 'Failed to submit manager review', 'error');
    }
}

/* ===== TEAM TAB ===== */
async function loadTeamTab() {
    const employees = await api('GET', '/api/users/employees');
    const tbody = document.getElementById('teamTableBody');
    if (!employees.length) {
        tbody.innerHTML = '<tr><td colspan="4" class="table-empty">No employees found.</td></tr>';
        return;
    }
    tbody.innerHTML = employees.map(e => `
        <tr>
            <td><strong>${e.name}</strong></td>
            <td>${e.email}</td>
            <td>${badge(e.role)}</td>
            <td>${e.managerName || '—'}</td>
        </tr>`).join('');
}

/* ===== HELPERS ===== */
async function loadManagerOptions() {
    try {
        const managers = await api('GET', '/api/users/managers');
        const sel = document.getElementById('regManagerId');
        sel.innerHTML = '<option value="">-- Select Manager --</option>' +
            managers.map(m => `<option value="${m.id}">${m.name}</option>`).join('');
    } catch (_) {}
}

function badge(status) {
    const s = (status || '').toLowerCase().replace(/ /g, '_');
    return `<span class="badge badge-${s}">${(status || '').replace(/_/g, ' ')}</span>`;
}

function showScreen(id) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById(id).classList.add('active');
}

function showTab(name) {
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.getElementById(`tab-${name}`).classList.add('active');
    document.querySelectorAll('.nav-item').forEach(n => {
        if (n.getAttribute('onclick')?.includes(name)) n.classList.add('active');
    });

    // Lazy load tab data
    if (name === 'leave') loadLeaveTab();
    else if (name === 'performance') loadPerformanceTab();
    else if (name === 'approvals') loadApprovalsTab();
    else if (name === 'team') loadTeamTab();
    else if (name === 'dashboard') loadDashboard();
}

function openModal(id) { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

function toast(message, type = 'info') {
    const el = document.getElementById('toast');
    el.textContent = message;
    el.className = `toast ${type} show`;
    setTimeout(() => el.classList.remove('show'), 3500);
}

async function api(method, path, body) {
    const opts = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (state.token) opts.headers['Authorization'] = `Bearer ${state.token}`;
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(API + path, opts);
    const text = await res.text();
    const data = text ? JSON.parse(text) : {};

    if (!res.ok) {
        const msg = data.message || data.error || `Error ${res.status}`;
        throw new Error(msg);
    }
    return data;
}
