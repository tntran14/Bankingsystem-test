// ===== UTILITIES =====
function $(sel) { return document.querySelector(sel); }
function $$(sel) { return document.querySelectorAll(sel); }
function fmt(n) { return Number(n||0).toLocaleString('vi-VN'); }
function fmtDate(d) { return d ? new Date(d).toLocaleDateString('vi-VN') : '-'; }
function fmtDateTime(d) { return d ? new Date(d).toLocaleString('vi-VN') : '-'; }

function toast(msg, type) {
    var c = $('#toast-container'), el = document.createElement('div');
    el.className = 'toast ' + (type||'info');
    el.innerHTML = '<span class="material-icons-round">' + ({success:'check_circle',error:'error',info:'info'}[type]||'info') + '</span>' + msg;
    c.appendChild(el);
    setTimeout(function(){ el.style.opacity='0'; setTimeout(function(){el.remove()},300); }, 3000);
}

function showModal(title, html) {
    $('#modal-title').textContent = title;
    $('#modal-body').innerHTML = html;
    $('#modal-overlay').style.display = 'flex';
}
function closeModal() { $('#modal-overlay').style.display = 'none'; }

// ===== STATE =====
var state = { page: 'dashboard', customerPage: 0, accountPage: 0, txPage: 0 };

// ===== INIT =====
document.addEventListener('DOMContentLoaded', function() {
    $('#modal-close').onclick = closeModal;
    $('#modal-overlay').onclick = function(e) { if (e.target === this) closeModal(); };
    $('#btn-toggle-sidebar').onclick = function() { $('#sidebar').classList.toggle('collapsed'); };
    $('#btn-logout').onclick = function() { API.clearToken(); location.reload(); };

    $$('.nav-item').forEach(function(item) {
        item.onclick = function(e) {
            e.preventDefault();
            $$('.nav-item').forEach(function(x){x.classList.remove('active')});
            item.classList.add('active');
            var page = item.getAttribute('data-page');
            state.page = page;
            $('#page-title').textContent = item.querySelector('.nav-label').textContent;
            loadPage(page);
        };
    });

    // Check auth
    if (API.token) { showApp(); loadPage(API.isAdmin() ? 'dashboard' : 'customer-dashboard'); }
    else { showLogin(); }

    // Toggle Login / Register
    $('#go-to-register').onclick = function(e) {
        e.preventDefault();
        $('#login-card').style.display = 'none';
        $('#register-card').style.display = 'block';
        $('#register-error').style.display = 'none';
        $('#register-success').style.display = 'none';
    };
    $('#go-to-login').onclick = function(e) {
        e.preventDefault();
        $('#register-card').style.display = 'none';
        $('#login-card').style.display = 'block';
        $('#login-error').style.display = 'none';
    };

    // Login form
    $('#login-form').onsubmit = function(e) {
        e.preventDefault();
        var btn = $('#login-btn');
        btn.querySelector('.btn-text').textContent = 'Đang đăng nhập...';
        btn.disabled = true;
        API.login({ username: $('#login-username').value, password: $('#login-password').value })
            .then(function(r) {
                API.setToken(r.token, r.roles);
                $('#current-user').textContent = r.username;
                showApp(); loadPage(API.isAdmin() ? 'dashboard' : 'customer-dashboard');
            })
            .catch(function(err) {
                var el = $('#login-error');
                el.style.display = 'block';
                el.textContent = 'Sai tên đăng nhập hoặc mật khẩu';
            })
            .finally(function() {
                btn.querySelector('.btn-text').textContent = 'Đăng nhập';
                btn.disabled = false;
            });
    };

    // Register form
    $('#register-form').onsubmit = function(e) {
        e.preventDefault();
        var btn = $('#register-btn');
        btn.querySelector('.btn-text').textContent = 'Đang đăng ký...';
        btn.disabled = true;
        
        var regData = {
            username: $('#reg-username').value,
            password: $('#reg-password').value,
            fullName: $('#reg-fullname').value,
            email: $('#reg-email').value,
            phone: $('#reg-phone').value,
            address: $('#reg-address').value
        };

        API.register(regData)
            .then(function(r) {
                var successEl = $('#register-success');
                successEl.style.display = 'block';
                successEl.textContent = 'Đăng ký thành công! Đang chuyển hướng...';
                toast('Đăng ký tài khoản thành công!','success');
                setTimeout(function() {
                    $('#go-to-login').click();
                    $('#login-username').value = regData.username;
                    $('#login-password').value = regData.password;
                }, 1500);
            })
            .catch(function(err) {
                var el = $('#register-error');
                el.style.display = 'block';
                el.textContent = err.message || 'Đăng ký thất bại';
            })
            .finally(function() {
                btn.querySelector('.btn-text').textContent = 'Đăng ký';
                btn.disabled = false;
            });
    };
});

function showLogin() { $('#login-page').style.display='flex'; $('#app').style.display='none'; }
function showApp() {
    $('#login-page').style.display='none';
    $('#app').style.display='flex';
    renderSidebar();
}

function renderSidebar() {
    var isAdmin = API.isAdmin();
    var nav = $('.sidebar-nav');
    
    if (isAdmin) {
        nav.innerHTML = `
            <a href="#" class="nav-item active" data-page="dashboard">
                <span class="material-icons-round">dashboard</span>
                <span class="nav-label">Dashboard</span>
            </a>
            <a href="#" class="nav-item" data-page="customers">
                <span class="material-icons-round">people</span>
                <span class="nav-label">Khách hàng</span>
            </a>
            <a href="#" class="nav-item" data-page="accounts">
                <span class="material-icons-round">account_balance_wallet</span>
                <span class="nav-label">Tài khoản</span>
            </a>
            <a href="#" class="nav-item" data-page="transactions">
                <span class="material-icons-round">swap_horiz</span>
                <span class="nav-label">Giao dịch</span>
            </a>
            <a href="#" class="nav-item" data-page="alerts">
                <span class="material-icons-round">warning</span>
                <span class="nav-label">Cảnh báo</span>
            </a>
            <a href="#" class="nav-item" data-page="reports">
                <span class="material-icons-round">assessment</span>
                <span class="nav-label">Báo cáo</span>
            </a>
        `;
    } else {
        nav.innerHTML = `
            <a href="#" class="nav-item active" data-page="customer-dashboard">
                <span class="material-icons-round">account_circle</span>
                <span class="nav-label">Thông tin cá nhân</span>
            </a>
            <a href="#" class="nav-item" data-page="customer-transactions">
                <span class="material-icons-round">history</span>
                <span class="nav-label">Lịch sử giao dịch</span>
            </a>
        `;
    }

    // Rebind onclick events for nav items
    $$('.nav-item').forEach(function(item) {
        item.onclick = function(e) {
            e.preventDefault();
            $$('.nav-item').forEach(function(x){x.classList.remove('active')});
            item.classList.add('active');
            var page = item.getAttribute('data-page');
            state.page = page;
            $('#page-title').textContent = item.querySelector('.nav-label').textContent;
            loadPage(page);
        };
    });
}

// ===== PAGE ROUTER =====
function loadPage(page) {
    var area = $('#content-area');
    switch(page) {
        case 'dashboard': loadDashboard(area); break;
        case 'customers': loadCustomers(area); break;
        case 'accounts': loadAccounts(area); break;
        case 'transactions': loadTransactions(area); break;
        case 'alerts': loadAlerts(area); break;
        case 'reports': loadReports(area); break;
        case 'customer-dashboard': loadCustomerDashboard(area); break;
        case 'customer-transactions': loadCustomerTransactions(area); break;
    }
}

// ===== DASHBOARD =====
function loadDashboard(area) {
    area.innerHTML = '<div class="stats-grid" id="stats-grid"></div><div class="report-grid" id="report-grid"></div>';
    API.getAccountStats().then(function(d) {
        $('#stats-grid').innerHTML =
            statCard('purple','account_balance_wallet','Tổng tài khoản',d.totalAccounts) +
            statCard('green','trending_up','Số dư cao',d.highBalanceAccounts) +
            statCard('blue','swap_horiz','Tổng giao dịch',d.totalTransactions) +
            statCard('orange','show_chart','Số dư thấp',d.lowBalanceAccounts);
    }).catch(function(){ $('#stats-grid').innerHTML = '<p style="color:var(--text-muted)">Không thể tải thống kê</p>'; });

    Promise.all([API.getWeeklyReport(), API.getQuarterlyReport(), API.getYearlyReport()])
        .then(function(r) {
            $('#report-grid').innerHTML = reportCard('Tuần này', r[0]) + reportCard('Quý này', r[1]) + reportCard('Năm nay', r[2]);
        }).catch(function(){});
}

function statCard(color,icon,label,value) {
    return '<div class="stat-card '+color+'"><div class="stat-icon"><span class="material-icons-round">'+icon+'</span></div><div class="stat-value">'+fmt(value)+'</div><div class="stat-label">'+label+'</div></div>';
}
function reportCard(title, d) {
    return '<div class="report-card"><h4>'+title+'</h4>'+
        ri('Tổng giao dịch',fmt(d.totalTransactions))+ri('Tổng tiền',fmt(d.totalAmount)+' đ')+
        ri('Trung bình',fmt(d.averageAmount)+' đ')+ri('Lớn nhất',fmt(d.maxAmount)+' đ')+
        ri('Nhỏ nhất',fmt(d.minAmount)+' đ')+ri('Tổng phí',fmt(d.totalFees)+' đ')+'</div>';
}
function ri(l,v) { return '<div class="report-item"><span class="label">'+l+'</span><span class="value">'+v+'</span></div>'; }

// ===== CUSTOMERS =====
function loadCustomers(area) {
    area.innerHTML = '<div class="card"><div class="card-header"><h3>Danh sách khách hàng</h3><div style="display:flex;gap:8px"><input id="cust-search" placeholder="Tìm theo tên..." style="max-width:200px"><button class="btn btn-primary btn-sm" onclick="showAddCustomer()"><span class="material-icons-round" style="font-size:16px">add</span> Thêm</button></div></div><div class="card-body no-pad"><div class="table-wrapper"><table><thead><tr><th>ID</th><th>Họ tên</th><th>Email</th><th>SĐT</th><th>Địa chỉ</th><th>Loại KH</th><th>Thao tác</th></tr></thead><tbody id="cust-tbody"></tbody></table></div><div class="pagination" id="cust-pag"></div></div></div>';
    fetchCustomers(0);
    $('#cust-search').oninput = debounce(function() {
        var v = this.value.trim();
        if (v.length >= 2) { API.searchCustomers(v).then(function(r) { renderCustomerRows(r); }); }
        else fetchCustomers(0);
    }, 400);
}

function fetchCustomers(page) {
    state.customerPage = page;
    API.getCustomers(page, 8).then(function(d) {
        renderCustomerRows(d.content);
        renderPagination('cust-pag', d, fetchCustomers);
    }).catch(function(e) { toast(e.message, 'error'); });
}

function renderCustomerRows(list) {
    var tb = $('#cust-tbody');
    if (!list || !list.length) { tb.innerHTML = '<tr><td colspan="7" class="empty-state">Chưa có khách hàng</td></tr>'; return; }
    tb.innerHTML = list.map(function(c) {
        var typeName = c.customerType ? c.customerType.category : '-';
        return '<tr><td>'+c.id+'</td><td>'+c.fullName+'</td><td>'+c.email+'</td><td>'+c.phone+'</td><td>'+c.address+'</td><td>'+typeName+'</td><td class="table-actions"><button class="btn btn-outline btn-sm" onclick="showEditCustomer('+c.id+')"><span class="material-icons-round" style="font-size:14px">edit</span></button><button class="btn btn-danger btn-sm" onclick="deleteCustomer('+c.id+')"><span class="material-icons-round" style="font-size:14px">delete</span></button></td></tr>';
    }).join('');
}

function showAddCustomer() {
    showModal('Thêm khách hàng', customerForm());
    $('#modal-body form').onsubmit = function(e) {
        e.preventDefault();
        API.createCustomer(getCustomerFormData()).then(function() { toast('Tạo khách hàng thành công','success'); closeModal(); fetchCustomers(state.customerPage); }).catch(function(e){toast(e.message,'error')});
    };
}
function showEditCustomer(id) {
    API.getCustomer(id).then(function(c) {
        showModal('Sửa khách hàng', customerForm(c));
        $('#modal-body form').onsubmit = function(e) {
            e.preventDefault();
            API.updateCustomer(id, getCustomerFormData()).then(function() { toast('Cập nhật thành công','success'); closeModal(); fetchCustomers(state.customerPage); }).catch(function(e){toast(e.message,'error')});
        };
    });
}
function deleteCustomer(id) {
    if (!confirm('Xóa khách hàng #'+id+'?')) return;
    API.deleteCustomer(id).then(function() { toast('Đã xóa','success'); fetchCustomers(state.customerPage); }).catch(function(e){toast(e.message,'error')});
}
function customerForm(c) {
    c = c || {};
    return '<form><div class="form-row"><div class="form-group"><label>Họ tên</label><input id="cf-name" value="'+(c.fullName||'')+'" required></div><div class="form-group"><label>Email</label><input id="cf-email" type="email" value="'+(c.email||'')+'" required></div></div><div class="form-row"><div class="form-group"><label>SĐT</label><input id="cf-phone" value="'+(c.phone||'')+'" required></div><div class="form-group"><label>Địa chỉ</label><input id="cf-address" value="'+(c.address||'')+'" required></div></div><div class="form-group"><label>Loại KH (1=Cá nhân, 2=Doanh nghiệp)</label><input id="cf-type" type="number" value="'+(c.customerType?c.customerType.id:'')+'"></div><div class="modal-actions"><button type="button" class="btn btn-outline" onclick="closeModal()">Hủy</button><button type="submit" class="btn btn-primary">Lưu</button></div></form>';
}
function getCustomerFormData() {
    var d = { fullName:$('#cf-name').value, email:$('#cf-email').value, phone:$('#cf-phone').value, address:$('#cf-address').value };
    var t = $('#cf-type').value; if (t) d.customerTypeId = parseInt(t);
    return d;
}

// ===== ACCOUNTS =====
function loadAccounts(area) {
    area.innerHTML = '<div class="card"><div class="card-header"><h3>Danh sách tài khoản</h3><button class="btn btn-primary btn-sm" onclick="showAddAccount()"><span class="material-icons-round" style="font-size:16px">add</span> Thêm</button></div><div class="card-body no-pad"><div class="table-wrapper"><table><thead><tr><th>ID</th><th>Số TK</th><th>Khách hàng</th><th>Số dư</th><th>Hạn mức</th><th>Trạng thái</th><th>Ngày mở</th><th>Thao tác</th></tr></thead><tbody id="acc-tbody"></tbody></table></div><div class="pagination" id="acc-pag"></div></div></div>';
    fetchAccounts(0);
}
function fetchAccounts(page) {
    state.accountPage = page;
    API.getAccounts(page, 8).then(function(d) {
        var tb = $('#acc-tbody');
        if (!d.content.length) { tb.innerHTML = '<tr><td colspan="8" class="empty-state">Chưa có tài khoản</td></tr>'; return; }
        tb.innerHTML = d.content.map(function(a) {
            var custName = a.customer ? a.customer.fullName : '-';
            return '<tr><td>'+a.id+'</td><td style="font-family:monospace">'+a.accountNumber+'</td><td>'+custName+'</td><td style="font-weight:600">'+fmt(a.balance)+' đ</td><td>'+fmt(a.transactionLimit)+' đ</td><td><span class="status-badge '+a.status.toLowerCase()+'">'+a.status+'</span></td><td>'+fmtDate(a.accountOpenDate)+'</td><td class="table-actions"><button class="btn btn-outline btn-sm" onclick="showChangeStatus('+a.id+',\''+a.status+'\')"><span class="material-icons-round" style="font-size:14px">sync</span></button><button class="btn btn-danger btn-sm" onclick="deleteAccount('+a.id+')"><span class="material-icons-round" style="font-size:14px">delete</span></button></td></tr>';
        }).join('');
        renderPagination('acc-pag', d, fetchAccounts);
    }).catch(function(e){ toast(e.message,'error'); });
}
function showAddAccount() {
    showModal('Tạo tài khoản', '<form><div class="form-group"><label>Customer ID</label><input id="af-cid" type="number" required></div><div class="form-row"><div class="form-group"><label>Số dư ban đầu</label><input id="af-bal" type="number" value="0"></div><div class="form-group"><label>Hạn mức GD</label><input id="af-limit" type="number" required></div></div><div class="modal-actions"><button type="button" class="btn btn-outline" onclick="closeModal()">Hủy</button><button type="submit" class="btn btn-primary">Tạo</button></div></form>');
    $('#modal-body form').onsubmit = function(e) {
        e.preventDefault();
        API.createAccount({ customerId: +$('#af-cid').value, initialBalance: +$('#af-bal').value, transactionLimit: +$('#af-limit').value })
            .then(function(r) { toast('Tạo tài khoản thành công: '+(r.data?r.data.accountNumber:''),'success'); closeModal(); fetchAccounts(state.accountPage); }).catch(function(e){toast(e.message,'error')});
    };
}
function showChangeStatus(id, current) {
    var opts = ['ACTIVE','LOCKED','CLOSED'].filter(function(s){return s!==current}).map(function(s){return '<option value="'+s+'">'+s+'</option>'}).join('');
    showModal('Đổi trạng thái TK #'+id, '<form><div class="form-group"><label>Trạng thái hiện tại: <strong>'+current+'</strong></label><select id="sf-status">'+opts+'</select></div><div class="modal-actions"><button type="button" class="btn btn-outline" onclick="closeModal()">Hủy</button><button type="submit" class="btn btn-primary">Cập nhật</button></div></form>');
    $('#modal-body form').onsubmit = function(e) {
        e.preventDefault();
        API.changeAccountStatus(id, $('#sf-status').value).then(function() { toast('Đã cập nhật trạng thái','success'); closeModal(); fetchAccounts(state.accountPage); }).catch(function(e){toast(e.message,'error')});
    };
}
function deleteAccount(id) {
    if (!confirm('Xóa tài khoản #'+id+'?')) return;
    API.deleteAccount(id).then(function() { toast('Đã xóa','success'); fetchAccounts(state.accountPage); }).catch(function(e){toast(e.message,'error')});
}

// ===== TRANSACTIONS =====
function loadTransactions(area) {
    area.innerHTML = '<div class="filters" id="tx-filters"><select id="txf-type"><option value="">Tất cả loại</option><option value="DEPOSIT">Nạp tiền</option><option value="WITHDRAWAL">Rút tiền</option><option value="TRANSFER">Chuyển khoản</option></select><input id="txf-min" type="number" placeholder="Từ số tiền"><input id="txf-max" type="number" placeholder="Đến số tiền"><div style="display:inline-flex;align-items:center;gap:6px"><label style="font-size:0.85rem;color:var(--text-muted);white-space:nowrap">Từ ngày:</label><input id="txf-from-date" type="date" style="max-width:140px"></div><div style="display:inline-flex;align-items:center;gap:6px"><label style="font-size:0.85rem;color:var(--text-muted);white-space:nowrap">Đến ngày:</label><input id="txf-to-date" type="date" style="max-width:140px"></div><button class="btn btn-primary btn-sm" onclick="fetchTransactions(0)"><span class="material-icons-round" style="font-size:16px">search</span> Lọc</button><button class="btn btn-success btn-sm" onclick="showNewTransaction()"><span class="material-icons-round" style="font-size:16px">add</span> Giao dịch mới</button></div><div class="card"><div class="card-header"><h3>Danh sách giao dịch</h3></div><div class="card-body no-pad"><div class="table-wrapper"><table><thead><tr><th>ID</th><th>Loại</th><th>Số tiền</th><th>Phí</th><th>Từ TK</th><th>Đến TK</th><th>Địa điểm</th><th>Ngày</th></tr></thead><tbody id="tx-tbody"></tbody></table></div><div class="pagination" id="tx-pag"></div></div></div>';
    fetchTransactions(0);
}
function fetchTransactions(page) {
    state.txPage = page;
    var fromVal = $('#txf-from-date').value;
    var toVal = $('#txf-to-date').value;
    var params = { 
        type: $('#txf-type').value, 
        minAmount: $('#txf-min').value, 
        maxAmount: $('#txf-max').value,
        fromDate: fromVal ? fromVal + 'T00:00:00' : '',
        toDate: toVal ? toVal + 'T23:59:59' : ''
    };
    API.searchTransactions(params, page, 10).then(function(d) {
        var tb = $('#tx-tbody');
        if (!d.content.length) { tb.innerHTML = '<tr><td colspan="8" class="empty-state">Chưa có giao dịch</td></tr>'; return; }
        tb.innerHTML = d.content.map(function(t) {
            return '<tr><td>'+t.id+'</td><td><span class="status-badge '+t.type.toLowerCase()+'">'+t.type+'</span></td><td style="font-weight:600">'+fmt(t.amount)+' đ</td><td>'+fmt(t.transactionFee)+' đ</td><td>'+(t.fromAccount?t.fromAccount.accountNumber:'-')+'</td><td>'+(t.toAccount?t.toAccount.accountNumber:'-')+'</td><td>'+(t.location||'-')+'</td><td>'+fmtDateTime(t.transactionDate)+'</td></tr>';
        }).join('');
        renderPagination('tx-pag', d, fetchTransactions);
    }).catch(function(e){ toast(e.message,'error'); });
}
function showNewTransaction() {
    showModal('Tạo giao dịch', '<form><div class="form-group"><label>Loại giao dịch</label><select id="tf-type" required><option value="DEPOSIT">Nạp tiền</option><option value="WITHDRAWAL">Rút tiền</option><option value="TRANSFER">Chuyển khoản</option></select></div><div class="form-row"><div class="form-group"><label>Từ tài khoản (ID)</label><input id="tf-from" type="number"></div><div class="form-group"><label>Đến tài khoản (ID)</label><input id="tf-to" type="number"></div></div><div class="form-row"><div class="form-group"><label>Số tiền</label><input id="tf-amount" type="number" required></div><div class="form-group"><label>Địa điểm</label><input id="tf-loc" placeholder="VD: Hà Nội"></div></div><div class="form-group"><label>Mô tả</label><input id="tf-desc"></div><div class="modal-actions"><button type="button" class="btn btn-outline" onclick="closeModal()">Hủy</button><button type="submit" class="btn btn-primary">Thực hiện</button></div></form>');
    $('#modal-body form').onsubmit = function(e) {
        e.preventDefault();
        var d = { type: $('#tf-type').value, amount: +$('#tf-amount').value, location: $('#tf-loc').value, description: $('#tf-desc').value };
        if ($('#tf-from').value) d.fromAccountId = +$('#tf-from').value;
        if ($('#tf-to').value) d.toAccountId = +$('#tf-to').value;
        API.createTransaction(d).then(function() { toast('Giao dịch thành công','success'); closeModal(); fetchTransactions(state.txPage); }).catch(function(e){toast(e.message,'error')});
    };
}

// ===== ALERTS =====
function loadAlerts(area) {
    area.innerHTML = '<div class="card"><div class="card-header"><h3>Cảnh báo giao dịch bất thường</h3><div style="display:flex;gap:8px"><button class="btn btn-outline btn-sm" onclick="fetchAlerts(\'all\')">Tất cả</button><button class="btn btn-outline btn-sm" onclick="fetchAlerts(\'PENDING\')">Pending</button><button class="btn btn-outline btn-sm" onclick="fetchAlerts(\'REVIEWED\')">Reviewed</button></div></div><div class="card-body no-pad"><div class="table-wrapper"><table><thead><tr><th>ID</th><th>Loại</th><th>Mô tả</th><th>GD #</th><th>Trạng thái</th><th>Ngày</th><th>Thao tác</th></tr></thead><tbody id="alert-tbody"></tbody></table></div></div></div>';
    fetchAlerts('all');
}
function fetchAlerts(filter) {
    var p = filter === 'all' ? API.getAlerts() : API.getAlertsByStatus(filter);
    p.then(function(list) {
        var tb = $('#alert-tbody');
        if (!list.length) { tb.innerHTML = '<tr><td colspan="7" class="empty-state">Không có cảnh báo</td></tr>'; return; }
        tb.innerHTML = list.map(function(a) {
            var actions = a.status === 'PENDING' ? '<button class="btn btn-outline btn-sm" onclick="resolveAlert('+a.id+',\'REVIEWED\')">Review</button>' : a.status === 'REVIEWED' ? '<button class="btn btn-success btn-sm" onclick="resolveAlert('+a.id+',\'RESOLVED\')">Resolve</button>' : '<span style="color:var(--success)">✓</span>';
            return '<tr><td>'+a.id+'</td><td><span class="status-badge '+(a.alertType==='HIGH_AMOUNT'?'withdrawal':'transfer')+'">'+a.alertType+'</span></td><td style="max-width:300px;overflow:hidden;text-overflow:ellipsis">'+a.description+'</td><td>'+(a.transaction?a.transaction.id:'-')+'</td><td><span class="status-badge '+a.status.toLowerCase()+'">'+a.status+'</span></td><td>'+fmtDateTime(a.createdAt)+'</td><td>'+actions+'</td></tr>';
        }).join('');
    }).catch(function(e){ toast(e.message,'error'); });
}
function resolveAlert(id, status) {
    API.updateAlertStatus(id, status).then(function() { toast('Cập nhật trạng thái','success'); fetchAlerts('all'); }).catch(function(e){toast(e.message,'error')});
}

// ===== REPORTS =====
function loadReports(area) {
    area.innerHTML = '<div class="stats-grid"><div class="stat-card purple" style="cursor:pointer" onclick="downloadExcel()"><div class="stat-icon"><span class="material-icons-round">table_chart</span></div><div class="stat-value" style="font-size:1.3rem">Excel</div><div class="stat-label">Xuất báo cáo .xlsx</div></div><div class="stat-card green" style="cursor:pointer" onclick="downloadPdf()"><div class="stat-icon"><span class="material-icons-round">picture_as_pdf</span></div><div class="stat-value" style="font-size:1.3rem">PDF</div><div class="stat-label">Xuất báo cáo .pdf</div></div></div><div id="report-detail" class="report-grid" style="margin-top:20px"></div>';
    Promise.all([API.getWeeklyReport(), API.getQuarterlyReport(), API.getYearlyReport(), API.getCustomerLocations()])
        .then(function(r) {
            var locHtml = r[3].map(function(l){return ri(l.address, l.customerCount+' KH')}).join('');
            $('#report-detail').innerHTML = reportCard('Báo cáo tuần',r[0]) + reportCard('Báo cáo quý',r[1]) + reportCard('Báo cáo năm',r[2]) + '<div class="report-card"><h4>KH theo địa điểm</h4>'+(locHtml||'<p class="empty-state">Chưa có dữ liệu</p>')+'</div>';
        }).catch(function(){});
}
function downloadExcel() {
    toast('Đang tạo file Excel...','info');
    API.exportExcel().then(function(blob) { downloadBlob(blob, 'transactions_report.xlsx'); toast('Tải xuống Excel thành công','success'); }).catch(function(e){toast(e.message,'error')});
}
function downloadPdf() {
    toast('Đang tạo file PDF...','info');
    API.exportPdf().then(function(blob) { downloadBlob(blob, 'transactions_report.pdf'); toast('Tải xuống PDF thành công','success'); }).catch(function(e){toast(e.message,'error')});
}
function downloadBlob(blob, name) {
    var url = URL.createObjectURL(blob), a = document.createElement('a');
    a.href = url; a.download = name; a.click(); URL.revokeObjectURL(url);
}

// ===== HELPERS =====
function renderPagination(elId, pageData, fetchFn) {
    var el = document.getElementById(elId);
    if (!el || pageData.totalPages <= 1) { if(el) el.innerHTML=''; return; }
    var html = '<button '+(pageData.first?'disabled':'')+' onclick="'+fetchFn.name+'('+(pageData.number-1)+')">‹</button>';
    for (var i = 0; i < pageData.totalPages && i < 7; i++) {
        html += '<button class="'+(i===pageData.number?'active':'')+'" onclick="'+fetchFn.name+'('+i+')">'+(i+1)+'</button>';
    }
    html += '<button '+(pageData.last?'disabled':'')+' onclick="'+fetchFn.name+'('+(pageData.number+1)+')">›</button>';
    el.innerHTML = html;
}
function debounce(fn, ms) {
    var timer;
    return function() {
        var ctx = this, args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function() { fn.apply(ctx, args); }, ms);
    };
}

// ===== CUSTOMER PAGES =====
function loadCustomerDashboard(area) {
    area.innerHTML = '<div class="empty-state"><span class="material-icons-round">sync</span><p>Đang tải thông tin...</p></div>';
    API.getMe().then(function(user) {
        var cust = user.customer || {};
        var accounts = cust.accounts || [];
        
        var accountsHtml = accounts.map(function(acc) {
            return `
                <div class="stat-card purple" style="margin-bottom: 16px;">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <div>
                            <div class="stat-label" style="font-family:monospace; font-size:1.1rem; font-weight:700; color:var(--accent-light);">TK: ${acc.accountNumber}</div>
                            <div class="stat-value" style="font-size:1.8rem; margin-top:8px;">${fmt(acc.balance)} đ</div>
                            <div class="stat-label" style="margin-top:4px;">Hạn mức giao dịch: ${fmt(acc.transactionLimit)} đ</div>
                        </div>
                        <div>
                            <span class="status-badge ${acc.status.toLowerCase()}">${acc.status}</span>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        if (accounts.length === 0) {
            accountsHtml = '<p class="empty-state">Bạn chưa có tài khoản ngân hàng nào. Vui lòng liên hệ Admin để mở tài khoản.</p>';
        }

        area.innerHTML = `
            <div class="report-grid">
                <div class="report-card" style="grid-column: span 1;">
                    <h4>Thông tin khách hàng</h4>
                    <div class="report-item">
                        <span class="label">Họ và tên</span>
                        <span class="value">${cust.fullName || '-'}</span>
                    </div>
                    <div class="report-item">
                        <span class="label">Tên đăng nhập</span>
                        <span class="value">${user.username}</span>
                    </div>
                    <div class="report-item">
                        <span class="label">Email</span>
                        <span class="value">${cust.email || '-'}</span>
                    </div>
                    <div class="report-item">
                        <span class="label">Số điện thoại</span>
                        <span class="value">${cust.phone || '-'}</span>
                    </div>
                    <div class="report-item">
                        <span class="label">Địa chỉ</span>
                        <span class="value">${cust.address || '-'}</span>
                    </div>
                    <div class="report-item">
                        <span class="label">Nhóm khách hàng</span>
                        <span class="value">${cust.customerType ? cust.customerType.category : '-'}</span>
                    </div>
                </div>
                <div class="report-card" style="grid-column: span 1;">
                    <h4>Tài khoản của tôi</h4>
                    ${accountsHtml}
                </div>
            </div>
        `;
    }).catch(function(e) {
        area.innerHTML = '<p class="empty-state" style="color:var(--danger)">Không thể tải thông tin cá nhân. Lỗi: ' + e.message + '</p>';
    });
}

function loadCustomerTransactions(area) {
    area.innerHTML = '<div class="empty-state"><span class="material-icons-round">sync</span><p>Đang tải thông tin tài khoản...</p></div>';
    API.getMe().then(function(user) {
        var cust = user.customer || {};
        var accounts = cust.accounts || [];
        
        if (accounts.length === 0) {
            area.innerHTML = '<p class="empty-state">Bạn chưa có tài khoản ngân hàng nào để xem giao dịch.</p>';
            return;
        }

        var selectOpts = accounts.map(function(acc) {
            return `<option value="${acc.id}">Tài khoản: ${acc.accountNumber} (${fmt(acc.balance)} đ)</option>`;
        }).join('');

        area.innerHTML = `
            <div class="filters">
                <label style="margin-right:10px; align-self:center; font-weight:600;">Chọn tài khoản:</label>
                <select id="cust-tx-acc-select" style="max-width:320px;">
                    ${selectOpts}
                </select>
            </div>
            <div class="card">
                <div class="card-header">
                    <h3>Lịch sử giao dịch tài khoản</h3>
                </div>
                <div class="card-body no-pad">
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Loại</th>
                                    <th>Số tiền</th>
                                    <th>Phí</th>
                                    <th>Từ TK</th>
                                    <th>Đến TK</th>
                                    <th>Địa điểm</th>
                                    <th>Ngày</th>
                                </tr>
                            </thead>
                            <tbody id="cust-tx-tbody"></tbody>
                        </table>
                    </div>
                    <div class="pagination" id="cust-tx-pag"></div>
                </div>
            </div>
        `;

        $('#cust-tx-acc-select').onchange = function() {
            fetchCustomerTransactions(0);
        };

        // Load initially
        fetchCustomerTransactions(0);
    }).catch(function(e) {
        area.innerHTML = '<p class="empty-state" style="color:var(--danger)">Không thể tải thông tin. Lỗi: ' + e.message + '</p>';
    });
}

function fetchCustomerTransactions(page) {
    var select = $('#cust-tx-acc-select');
    if (!select) return;
    var accId = select.value;
    if (!accId) return;

    state.txPage = page;
    API.getTransactionsByAccount(accId, page, 10).then(function(d) {
        var tb = $('#cust-tx-tbody');
        if (!d.content.length) { tb.innerHTML = '<tr><td colspan="8" class="empty-state">Không có giao dịch nào cho tài khoản này</td></tr>'; return; }
        tb.innerHTML = d.content.map(function(t) {
            return '<tr><td>'+t.id+'</td><td><span class="status-badge '+t.type.toLowerCase()+'">'+t.type+'</span></td><td style="font-weight:600">'+fmt(t.amount)+' đ</td><td>'+fmt(t.transactionFee)+' đ</td><td>'+(t.fromAccount?t.fromAccount.accountNumber:'-')+'</td><td>'+(t.toAccount?t.toAccount.accountNumber:'-')+'</td><td>'+(t.location||'-')+'</td><td>'+fmtDateTime(t.transactionDate)+'</td></tr>';
        }).join('');
        renderPagination('cust-tx-pag', d, fetchCustomerTransactions);
    }).catch(function(e) {
        toast(e.message, 'error');
    });
}
