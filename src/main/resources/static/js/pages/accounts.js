/**
 * Accounts Page - CRUD + Status Management
 */
async function initAccountsPage() {
    const content = document.getElementById('page-content');
    content.innerHTML = `
        <div class="toolbar">
            <div style="display:flex;gap:12px;align-items:center">
                <span style="color:var(--text-muted);font-size:0.9rem">Quản lý tài khoản ngân hàng</span>
            </div>
            <button class="btn btn-primary" id="btn-add-account">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Tạo Tài Khoản
            </button>
        </div>
        <div class="card">
            <div class="table-wrapper">
                <table>
                    <thead><tr>
                        <th>ID</th><th>Số TK</th><th>Khách Hàng</th><th>Số Dư</th><th>Hạn Mức</th><th>Ngày Mở</th><th>Trạng Thái</th><th>Hành Động</th>
                    </tr></thead>
                    <tbody id="accounts-tbody"></tbody>
                </table>
            </div>
            <div id="accounts-pagination" class="pagination"></div>
        </div>`;

    let currentPage = 0;

    async function loadAccounts(page = 0) {
        currentPage = page;
        try {
            const data = await api.getAccounts(page, 10);
            renderAccountsTable(data);
        } catch (e) { showToast(e.message, 'error'); }
    }

    function renderAccountsTable(data) {
        const tbody = document.getElementById('accounts-tbody');
        const items = data.content || [];
        if (!items.length) {
            tbody.innerHTML = '<tr><td colspan="8" class="empty-state">Chưa có tài khoản nào</td></tr>';
            return;
        }
        tbody.innerHTML = items.map(a => `<tr>
            <td>${a.id}</td>
            <td style="font-weight:600;font-family:monospace">${a.accountNumber}</td>
            <td>${a.customer ? a.customer.fullName : '-'}</td>
            <td style="font-weight:700;color:var(--accent-green)">${formatCurrency(a.balance)}</td>
            <td>${formatCurrency(a.transactionLimit)}</td>
            <td>${a.accountOpenDate || '-'}</td>
            <td>
                <select class="badge badge-${(a.status||'').toLowerCase()}" style="border:none;cursor:pointer;background-color:transparent;font-weight:600;padding:4px 8px" onchange="changeStatus(${a.id}, this.value)">
                    <option value="ACTIVE" ${a.status==='ACTIVE'?'selected':''} style="color:#000">ACTIVE</option>
                    <option value="LOCKED" ${a.status==='LOCKED'?'selected':''} style="color:#000">LOCKED</option>
                    <option value="CLOSED" ${a.status==='CLOSED'?'selected':''} style="color:#000">CLOSED</option>
                </select>
            </td>
            <td class="table-actions">
                <button class="btn-icon" onclick="viewStatusHistory(${a.id})" title="Lịch sử">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                </button>
                <button class="btn-icon" onclick="removeAccount(${a.id},'${a.accountNumber}')" title="Xóa" style="color:var(--accent-red)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                </button>
            </td>
        </tr>`).join('');
        renderPagination('accounts-pagination', data, loadAccounts);
    }

    window.changeStatus = async (id, status) => {
        try {
            await api.changeAccountStatus(id, status);
            showToast(`Trạng thái đã đổi thành ${status}`, 'success');
            loadAccounts(currentPage);
        } catch (e) { showToast(e.message, 'error'); loadAccounts(currentPage); }
    };

    window.viewStatusHistory = async (id) => {
        try {
            const history = await api.getAccountStatusHistory(id);
            const items = Array.isArray(history) ? history : [];
            openModal('Lịch Sử Trạng Thái', items.length ?
                `<table style="width:100%"><thead><tr><th>Trạng thái</th><th>Người thay đổi</th><th>Thời gian</th></tr></thead><tbody>
                ${items.map(h => `<tr><td><span class="badge badge-${(h.status||'').toLowerCase()}">${h.status}</span></td><td>${h.changedBy||'-'}</td><td>${formatDate(h.changedAt)}</td></tr>`).join('')}
                </tbody></table>` : '<p class="empty-state">Chưa có lịch sử</p>');
        } catch (e) { showToast(e.message, 'error'); }
    };

    window.removeAccount = (id, num) => {
        showConfirm(`Xóa tài khoản "${num}"?`, async () => {
            try {
                await api.deleteAccount(id);
                showToast('Đã xóa tài khoản', 'success');
                loadAccounts(currentPage);
            } catch (e) { showToast(e.message, 'error'); }
        });
    };

    document.getElementById('btn-add-account').onclick = () => {
        openModal('Tạo Tài Khoản Mới', `
            <form id="account-form">
                <div class="form-group">
                    <label class="form-label">Mã Khách Hàng *</label>
                    <input class="form-control" name="customerId" type="number" required placeholder="Nhập ID khách hàng">
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label">Hạn Mức GD *</label>
                        <input class="form-control" name="transactionLimit" type="number" required placeholder="VD: 100000000">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Số Dư Ban Đầu</label>
                        <input class="form-control" name="initialBalance" type="number" placeholder="0">
                    </div>
                </div>
            </form>`, [
            { text: 'Hủy', class: 'btn btn-secondary', action: closeModal },
            { text: 'Tạo', class: 'btn btn-primary', action: async () => {
                const form = document.getElementById('account-form');
                if (!form.checkValidity()) { form.reportValidity(); return; }
                const fd = new FormData(form);
                try {
                    await api.createAccount({
                        customerId: parseInt(fd.get('customerId')),
                        transactionLimit: parseFloat(fd.get('transactionLimit')),
                        initialBalance: fd.get('initialBalance') ? parseFloat(fd.get('initialBalance')) : 0
                    });
                    showToast('Đã tạo tài khoản', 'success');
                    closeModal();
                    loadAccounts(0);
                } catch (e) { showToast(e.message, 'error'); }
            }}
        ]);
    };

    loadAccounts(0);
}
