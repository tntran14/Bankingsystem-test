/**
 * Simple hash-based SPA Router with auth guard.
 */
class Router {
    constructor() {
        this.routes = {};
        this.currentPage = null;
        window.addEventListener('hashchange', () => this.handleRoute());
    }

    register(path, handler) {
        this.routes[path] = handler;
    }

    navigate(path) {
        window.location.hash = path;
    }

    handleRoute() {
        const hash = window.location.hash || '#/dashboard';
        const path = hash.replace('#', '');

        // Auth guard
        if (path !== '/login' && !api.isAuthenticated()) {
            this.navigate('/login');
            return;
        }
        if (path === '/login' && api.isAuthenticated()) {
            this.navigate('/dashboard');
            return;
        }

        // Show/hide login vs app
        const loginPage = document.getElementById('login-page');
        const appLayout = document.getElementById('app-layout');
        if (path === '/login') {
            loginPage.classList.remove('hidden');
            appLayout.classList.add('hidden');
            if (this.routes['/login']) this.routes['/login']();
            return;
        } else {
            loginPage.classList.add('hidden');
            appLayout.classList.remove('hidden');
        }

        // Update active nav
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.toggle('active', link.getAttribute('data-page') === path.replace('/', ''));
        });

        // Update page title
        const titles = {
            '/dashboard': 'Dashboard',
            '/customers': 'Quản Lý Khách Hàng',
            '/accounts': 'Quản Lý Tài Khoản',
            '/transactions': 'Quản Lý Giao Dịch',
            '/alerts': 'Cảnh Báo Giao Dịch'
        };
        document.getElementById('page-title').textContent = titles[path] || 'Dashboard';

        // Render page
        const handler = this.routes[path];
        if (handler) {
            this.currentPage = path;
            const content = document.getElementById('page-content');
            content.innerHTML = '<div class="loading-spinner"><div class="spinner"></div></div>';
            handler();
        } else {
            this.navigate('/dashboard');
        }
    }

    start() {
        if (!window.location.hash) {
            window.location.hash = api.isAuthenticated() ? '#/dashboard' : '#/login';
        }
        this.handleRoute();
    }
}

const router = new Router();
