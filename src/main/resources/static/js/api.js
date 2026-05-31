// ===== API CLIENT =====
const API = {
    baseUrl: '/api',
    token: localStorage.getItem('jwt_token'),

    setToken(token, roles) {
        this.token = token;
        localStorage.setItem('jwt_token', token);
        localStorage.setItem('user_roles', JSON.stringify(roles || []));
    },
    clearToken() {
        this.token = null;
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_roles');
    },
    isAdmin() {
        try {
            const roles = JSON.parse(localStorage.getItem('user_roles') || '[]');
            return roles.includes('ROLE_ADMIN');
        } catch (e) {
            return false;
        }
    },

    async request(method, path, body, isBlob) {
        const headers = { 'Content-Type': 'application/json' };
        if (this.token) headers['Authorization'] = 'Bearer ' + this.token;
        const opts = { method, headers };
        if (body) opts.body = JSON.stringify(body);
        const res = await fetch(this.baseUrl + path, opts);
        if (res.status === 401 || res.status === 403) {
            this.clearToken();
            location.reload();
            throw new Error('Unauthorized');
        }
        if (!res.ok) {
            let errMsg = 'Request failed';
            try {
                const data = await res.json();
                errMsg = data.message || errMsg;
            } catch (e) {}
            throw new Error(errMsg);
        }
        if (isBlob) return res.blob();
        if (res.status === 204) return null;
        return await res.json();
    },

    get(path) { return this.request('GET', path); },
    post(path, body) { return this.request('POST', path, body); },
    put(path, body) { return this.request('PUT', path, body); },
    patch(path, body) { return this.request('PATCH', path, body); },
    del(path) { return this.request('DELETE', path); },
    getBlob(path) { return this.request('GET', path, null, true); },

    // Auth
    login(data) { return this.post('/auth/login', data); },
    register(data) { return this.post('/auth/register', data); },
    getMe() { return this.get('/auth/me'); },

    // Customers
    getCustomers(page, size) { return this.get('/customers?page=' + page + '&size=' + size); },
    getCustomer(id) { return this.get('/customers/' + id); },
    createCustomer(d) { return this.post('/customers', d); },
    updateCustomer(id, d) { return this.put('/customers/' + id, d); },
    deleteCustomer(id) { return this.del('/customers/' + id); },
    searchCustomers(name) { return this.get('/customers/search?name=' + encodeURIComponent(name)); },

    // Accounts
    getAccounts(page, size) { return this.get('/accounts?page=' + page + '&size=' + size); },
    getAccount(id) { return this.get('/accounts/' + id); },
    createAccount(d) { return this.post('/accounts', d); },
    updateAccount(id, d) { return this.put('/accounts/' + id, d); },
    deleteAccount(id) { return this.del('/accounts/' + id); },
    changeAccountStatus(id, status) { return this.patch('/accounts/' + id + '/status?status=' + status); },
    getAccountHistory(id) { return this.get('/accounts/' + id + '/status-history'); },
    getAccountStatusHistory(id) { return this.get('/accounts/' + id + '/status-history'); },
    getAccountsByCustomer(cid) { return this.get('/accounts/customer/' + cid); },

    // Transactions
    createTransaction(d) { return this.post('/transactions', d); },
    getTransaction(id) { return this.get('/transactions/' + id); },
    searchTransactions(params, page, size) {
        let q = '?page=' + page + '&size=' + size + '&sort=transactionDate,desc';
        if (params.type) q += '&type=' + params.type;
        if (params.minAmount) q += '&minAmount=' + params.minAmount;
        if (params.maxAmount) q += '&maxAmount=' + params.maxAmount;
        if (params.fromDate) q += '&fromDate=' + params.fromDate;
        if (params.toDate) q += '&toDate=' + params.toDate;
        return this.get('/transactions/search' + q);
    },
    getTransactionsByAccount(id, page, size) { return this.get('/transactions/account/' + id + '?page=' + page + '&size=' + size); },
    deleteTransaction(id) { return this.del('/transactions/' + id); },

    // Scheduled
    getScheduled() { return this.get('/transactions/scheduled'); },
    createScheduled(d) { return this.post('/transactions/scheduled', d); },
    cancelScheduled(id) { return this.del('/transactions/scheduled/' + id); },

    // Statistics
    getAccountStats() { return this.get('/statistics/accounts'); },
    getCustomerLocations() { return this.get('/statistics/customers/location'); },
    getWeeklyReport() { return this.get('/statistics/transactions/weekly'); },
    getQuarterlyReport() { return this.get('/statistics/transactions/quarterly'); },
    getYearlyReport() { return this.get('/statistics/transactions/yearly'); },

    // Alerts
    getAlerts() { return this.get('/alerts'); },
    getAlertsByStatus(s) { return this.get('/alerts/status/' + s); },
    updateAlertStatus(id, s) { return this.patch('/alerts/' + id + '/status?status=' + s); },

    // Reports
    exportExcel() { return this.getBlob('/reports/transactions/excel'); },
    exportPdf() { return this.getBlob('/reports/transactions/pdf'); },
};
