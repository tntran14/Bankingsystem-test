/**
 * Dashboard Page - Statistics overview with charts
 */
async function initDashboardPage() {
    const content = document.getElementById('page-content');
    content.innerHTML = `
        <div class="stats-grid" id="stats-cards"></div>
        <div class="dashboard-grid">
            <div class="card">
                <div class="card-header"><h3 class="card-title">Giao Dịch Theo Loại</h3></div>
                <div class="chart-container"><div class="bar-chart" id="type-chart"></div></div>
            </div>
            <div class="card">
                <div class="card-header"><h3 class="card-title">Báo Cáo Tuần</h3></div>
                <div id="weekly-report"></div>
            </div>
            <div class="card">
                <div class="card-header"><h3 class="card-title">Cảnh Báo Gần Đây</h3></div>
                <div id="recent-alerts"></div>
            </div>
            <div class="card">
                <div class="card-header"><h3 class="card-title">KH Theo Địa Điểm</h3></div>
                <div id="location-stats"></div>
            </div>
        </div>`;

    try {
        const [stats, weekly, alerts, locations] = await Promise.all([
            api.getAccountStatistics().catch(() => null),
            api.getWeeklyReport().catch(() => null),
            api.getAlerts().catch(() => []),
            api.getCustomersByLocation().catch(() => [])
        ]);

        renderStatCards(stats);
        renderTypeChart(stats);
        renderWeeklyReport(weekly);
        renderRecentAlerts(Array.isArray(alerts) ? alerts : []);
        renderLocationStats(Array.isArray(locations) ? locations : []);
    } catch (e) {
        console.error('Dashboard error:', e);
    }
}

function renderStatCards(stats) {
    const el = document.getElementById('stats-cards');
    if (!stats) { el.innerHTML = '<p class="empty-state">Không có dữ liệu thống kê</p>'; return; }
    const cards = [
        { label: 'Tổng Tài Khoản', value: stats.totalAccounts || 0, color: '#6366f1', icon: 'credit-card' },
        { label: 'Tổng Giao Dịch', value: stats.totalTransactions || 0, color: '#22d3ee', icon: 'activity' },
        { label: 'Số Dư Cao', value: stats.highBalanceAccounts || 0, color: '#22c55e', icon: 'trending-up' },
        { label: 'Số Dư Thấp', value: stats.lowBalanceAccounts || 0, color: '#f59e0b', icon: 'trending-down' }
    ];
    el.innerHTML = cards.map(c => `
        <div class="stat-card" style="--stat-color: ${c.color}">
            <div class="stat-card-header">
                <span class="stat-label">${c.label}</span>
                <div class="stat-icon" style="background: ${c.color}22; color: ${c.color}">
                    ${getIcon(c.icon)}
                </div>
            </div>
            <div class="stat-value" style="color: ${c.color}">${animateNumber(c.value)}</div>
        </div>`).join('');

    // Animate numbers
    el.querySelectorAll('.stat-value').forEach((el, i) => {
        const target = cards[i].value;
        animateCounter(el, target);
    });
}

function animateCounter(el, target) {
    let current = 0;
    const step = Math.max(1, Math.ceil(target / 40));
    const timer = setInterval(() => {
        current += step;
        if (current >= target) { current = target; clearInterval(timer); }
        el.textContent = current.toLocaleString('vi-VN');
    }, 30);
}

function animateNumber(n) { return '0'; }

function renderTypeChart(stats) {
    const el = document.getElementById('type-chart');
    if (!stats || !stats.transactionsByType) { el.innerHTML = '<p style="color:var(--text-muted);text-align:center">Chưa có dữ liệu</p>'; return; }
    const types = stats.transactionsByType;
    const max = Math.max(...Object.values(types), 1);
    const colors = { DEPOSIT: '#22c55e', WITHDRAWAL: '#ef4444', TRANSFER: '#6366f1' };
    const labels = { DEPOSIT: 'Nạp tiền', WITHDRAWAL: 'Rút tiền', TRANSFER: 'Chuyển khoản' };
    el.innerHTML = Object.entries(types).map(([key, val]) => {
        const h = Math.max(4, (val / max) * 180);
        return `<div class="bar-group">
            <div class="bar-value">${val}</div>
            <div class="bar" style="height:${h}px;background:${colors[key] || '#6366f1'}"></div>
            <div class="bar-label">${labels[key] || key}</div>
        </div>`;
    }).join('');
}

function renderWeeklyReport(r) {
    const el = document.getElementById('weekly-report');
    if (!r) { el.innerHTML = '<p style="color:var(--text-muted)">Chưa có dữ liệu</p>'; return; }
    el.innerHTML = `
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-top:8px">
            <div style="padding:12px;background:var(--bg-input);border-radius:var(--radius-sm)">
                <div style="color:var(--text-muted);font-size:0.8rem">Tổng GD</div>
                <div style="font-size:1.3rem;font-weight:700;color:var(--accent-cyan)">${r.totalTransactions || 0}</div>
            </div>
            <div style="padding:12px;background:var(--bg-input);border-radius:var(--radius-sm)">
                <div style="color:var(--text-muted);font-size:0.8rem">Tổng tiền</div>
                <div style="font-size:1.3rem;font-weight:700;color:var(--accent-green)">${formatCurrency(r.totalAmount)}</div>
            </div>
            <div style="padding:12px;background:var(--bg-input);border-radius:var(--radius-sm)">
                <div style="color:var(--text-muted);font-size:0.8rem">Trung bình</div>
                <div style="font-size:1.1rem;font-weight:600">${formatCurrency(r.averageAmount)}</div>
            </div>
            <div style="padding:12px;background:var(--bg-input);border-radius:var(--radius-sm)">
                <div style="color:var(--text-muted);font-size:0.8rem">Phí thu</div>
                <div style="font-size:1.1rem;font-weight:600;color:var(--accent-amber)">${formatCurrency(r.totalFees)}</div>
            </div>
        </div>`;
}

function renderRecentAlerts(alerts) {
    const el = document.getElementById('recent-alerts');
    const recent = alerts.slice(0, 5);
    if (!recent.length) { el.innerHTML = '<p style="color:var(--text-muted);padding:20px 0;text-align:center">Không có cảnh báo</p>'; return; }

    // Update badge
    const pending = alerts.filter(a => a.status === 'PENDING').length;
    const badge = document.getElementById('alert-badge');
    if (pending > 0) { badge.textContent = pending; badge.classList.remove('hidden'); }
    else { badge.classList.add('hidden'); }

    el.innerHTML = recent.map(a => `
        <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 0;border-bottom:1px solid var(--border-color)">
            <div>
                <div style="font-weight:600;font-size:0.85rem">${a.alertType || 'N/A'}</div>
                <div style="color:var(--text-muted);font-size:0.8rem">${a.description || ''}</div>
            </div>
            <span class="badge badge-${(a.status || '').toLowerCase()}">${a.status}</span>
        </div>`).join('');
}

function renderLocationStats(locations) {
    const el = document.getElementById('location-stats');
    if (!locations.length) { el.innerHTML = '<p style="color:var(--text-muted);padding:20px 0;text-align:center">Chưa có dữ liệu</p>'; return; }
    const max = Math.max(...locations.map(l => l.customerCount), 1);
    el.innerHTML = locations.slice(0, 6).map(l => `
        <div style="margin-bottom:12px">
            <div style="display:flex;justify-content:space-between;margin-bottom:4px">
                <span style="font-size:0.85rem">${l.address}</span>
                <span style="font-weight:600;color:var(--accent-purple)">${l.customerCount}</span>
            </div>
            <div style="height:6px;background:var(--bg-input);border-radius:3px;overflow:hidden">
                <div style="height:100%;width:${(l.customerCount/max)*100}%;background:linear-gradient(90deg,var(--accent-blue),var(--accent-purple));border-radius:3px;transition:width 0.6s ease"></div>
            </div>
        </div>`).join('');
}

function getIcon(name) {
    const icons = {
        'credit-card': '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg>',
        'activity': '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>',
        'trending-up': '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>',
        'trending-down': '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="17 18 23 18 23 12"/></svg>'
    };
    return icons[name] || '';
}
