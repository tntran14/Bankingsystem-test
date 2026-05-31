/**
 * Login Page Handler
 */
function initLoginPage() {
    const form = document.getElementById('login-form');
    const errorDiv = document.getElementById('login-error');
    const btn = document.getElementById('login-btn');
    const btnText = btn.querySelector('.btn-text');
    const btnLoader = btn.querySelector('.btn-loader');

    form.onsubmit = async (e) => {
        e.preventDefault();
        const username = document.getElementById('login-username').value.trim();
        const password = document.getElementById('login-password').value.trim();

        if (!username || !password) {
            showLoginError('Vui lòng nhập đầy đủ thông tin');
            return;
        }

        btnText.classList.add('hidden');
        btnLoader.classList.remove('hidden');
        btn.disabled = true;
        errorDiv.classList.add('hidden');

        try {
            const res = await api.login(username, password);
            api.setToken(res.token);
            api.setUsername(res.username);
            document.getElementById('current-user').textContent = res.username;
            router.navigate('/dashboard');
        } catch (err) {
            showLoginError(err.message || 'Sai tên đăng nhập hoặc mật khẩu');
        } finally {
            btnText.classList.remove('hidden');
            btnLoader.classList.add('hidden');
            btn.disabled = false;
        }
    };

    function showLoginError(msg) {
        errorDiv.textContent = msg;
        errorDiv.classList.remove('hidden');
    }
}
