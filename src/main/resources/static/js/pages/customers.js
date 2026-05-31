/**
 * Customers Page - CRUD + Search
 */
async function initCustomersPage() {
    const content = document.getElementById('page-content');
    content.innerHTML = `
        <div class="toolbar">
            <div class="search-box">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input type="text" id="customer-search" placeholder="Tìm kiếm khách hàng...">
            </div>
            <button class="btn btn-primary" id="btn-add-customer">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Thêm Khách Hàng
            </button>
        </div>
        <div class="card">
            <div class="table-wrapper">
                <table>
                    <thead><tr>
                        <th>ID</th><th>Họ Tên</th><th>Email</th><th>SĐT</th><th>Địa Chỉ</th><th>Loại KH</th><th>Hành Động</th>
                    </tr></thead>
                    <tbody id="customers-tbody"></tbody>
                </table>
            </div>
            <div id="customers-pagination" class="pagination"></div>
        </div>`;

    let currentPage = 0;
    const pageSize = 10;

    async function loadCustomers(page = 0) {
        currentPage = page;
        try {
            const data = await api.getCustomers(page, pageSize);
            renderCustomersTable(data);
        } catch (e) { showToast(e.message, 'error'); }
    }

    function renderCustomersTable(data) {
        const tbody = document.getElementById('customers-tbody');
        const items = data.content || [];
        if (!items.length) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Chưa có khách hàng nào</td></tr>';
            document.getElementById('customers-pagination').innerHTML = '';
            return;
        }
        tbody.innerHTML = items.map(c => `<tr>
            <td>${c.id}</td>
            <td style="font-weight:600">${c.fullName}</td>
            <td>${c.email}</td>
            <td>${c.phone}</td>
            <td>${c.address}</td>
            <td>${c.customerType ? c.customerType.category : '-'}</td>
            <td class="table-actions">
                <button class="btn-icon" onclick="editCustomer(${c.id})" title="Sửa">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                </button>
                <button class="btn-icon" onclick="removeCustomer(${c.id},'${c.fullName}')" title="Xóa" style="color:var(--accent-red)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                </button>
            </td>
        </tr>`).join('');
        renderPagination('customers-pagination', data, loadCustomers);
    }

    // Search
    let searchTimeout;
    document.getElementById('customer-search').addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(async () => {
            const q = e.target.value.trim();
            if (q.length > 0) {
                try {
                    const results = await api.searchCustomers(q);
                    const tbody = document.getElementById('customers-tbody');
                    const items = Array.isArray(results) ? results : [];
                    if (!items.length) { tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Không tìm thấy</td></tr>'; return; }
                    tbody.innerHTML = items.map(c => `<tr>
                        <td>${c.id}</td><td style="font-weight:600">${c.fullName}</td><td>${c.email}</td>
                        <td>${c.phone}</td><td>${c.address}</td><td>${c.customerType ? c.customerType.category : '-'}</td>
                        <td class="table-actions">
                            <button class="btn-icon" onclick="editCustomer(${c.id})" title="Sửa"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                            <button class="btn-icon" onclick="removeCustomer(${c.id},'${c.fullName}')" title="Xóa" style="color:var(--accent-red)"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></button>
                        </td></tr>`).join('');
                    document.getElementById('customers-pagination').innerHTML = '';
                } catch (e) { showToast(e.message, 'error'); }
            } else { loadCustomers(0); }
        }, 400);
    });

    // Add customer
    document.getElementById('btn-add-customer').onclick = () => showCustomerModal();

    // Global handlers
    window.editCustomer = async (id) => {
        try {
            const c = await api.getCustomer(id);
            showCustomerModal(c);
        } catch (e) { showToast(e.message, 'error'); }
    };

    window.removeCustomer = (id, name) => {
        showConfirm(`Xóa khách hàng "${name}"?`, async () => {
            try {
                await api.deleteCustomer(id);
                showToast('Đã xóa khách hàng', 'success');
                loadCustomers(currentPage);
            } catch (e) { showToast(e.message, 'error'); }
        });
    };

    function showCustomerModal(customer = null) {
        const isEdit = !!customer;
        openModal(isEdit ? 'Sửa Khách Hàng' : 'Thêm Khách Hàng', `
            <form id="customer-form">
                <div class="form-group">
                    <label class="form-label">Họ Tên *</label>
                    <input class="form-control" name="fullName" value="${customer?.fullName || ''}" required>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label">Email *</label>
                        <input class="form-control" name="email" type="email" value="${customer?.email || ''}" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">SĐT *</label>
                        <input class="form-control" name="phone" value="${customer?.phone || ''}" required>
                    </div>
                </div>
                <div class="form-group">
                    <label class="form-label">Địa Chỉ *</label>
                    <input class="form-control" name="address" value="${customer?.address || ''}" required>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label">Ngày Sinh</label>
                        <input class="form-control" name="dateOfBirth" type="date" value="${customer?.dateOfBirth || ''}">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Loại KH</label>
                        <select class="form-control" name="customerTypeId">
                            <option value="">-- Chọn --</option>
                            <option value="1" ${customer?.customerType?.id === 1 ? 'selected' : ''}>Cá nhân</option>
                            <option value="2" ${customer?.customerType?.id === 2 ? 'selected' : ''}>Doanh nghiệp</option>
                        </select>
                    </div>
                </div>
            </form>`, [
            { text: 'Hủy', class: 'btn btn-secondary', action: closeModal },
            { text: isEdit ? 'Cập Nhật' : 'Thêm Mới', class: 'btn btn-primary', action: async () => {
                const form = document.getElementById('customer-form');
                if (!form.checkValidity()) { form.reportValidity(); return; }
                const fd = new FormData(form);
                const data = {
                    fullName: fd.get('fullName'), email: fd.get('email'),
                    phone: fd.get('phone'), address: fd.get('address'),
                    dateOfBirth: fd.get('dateOfBirth') || null,
                    customerTypeId: fd.get('customerTypeId') ? parseInt(fd.get('customerTypeId')) : null
                };
                try {
                    if (isEdit) await api.updateCustomer(customer.id, data);
                    else await api.createCustomer(data);
                    showToast(isEdit ? 'Đã cập nhật' : 'Đã thêm khách hàng', 'success');
                    closeModal();
                    loadCustomers(currentPage);
                } catch (e) { showToast(e.message, 'error'); }
            }}
        ]);
    }

    loadCustomers(0);
}
