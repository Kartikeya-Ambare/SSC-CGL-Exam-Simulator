/* =========================================================
   SSC CGL EXAM PORTAL — MAIN JS
   ========================================================= */

'use strict';

// ---- Toast Notifications ----
const ToastManager = {
    container: null,

    init() {
        this.container = document.createElement('div');
        this.container.className = 'toast-container-custom';
        document.body.appendChild(this.container);
    },

    show(message, type = 'info', duration = 3000) {
        if (!this.container) this.init();
        const toast = document.createElement('div');
        toast.className = `toast-msg ${type}`;
        toast.textContent = message;
        this.container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.3s';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    },

    success(msg, dur) { this.show(msg, 'success', dur); },
    error(msg, dur) { this.show(msg, 'error', dur || 4000); },
    info(msg, dur) { this.show(msg, 'info', dur); }
};

// ---- Password Toggle ----
function initPasswordToggles() {
    document.querySelectorAll('.password-toggle').forEach(btn => {
        btn.addEventListener('click', function () {
            const input = document.querySelector(this.dataset.target || '#password');
            if (!input) return;
            if (input.type === 'password') {
                input.type = 'text';
                this.querySelector('i').className = 'bi bi-eye-slash';
            } else {
                input.type = 'password';
                this.querySelector('i').className = 'bi bi-eye';
            }
        });
    });
}

// ---- Confirm Password Validation ----
function initConfirmPasswordValidation() {
    const pwField = document.getElementById('password');
    const confirmField = document.getElementById('confirmPassword');
    if (!pwField || !confirmField) return;

    function checkMatch() {
        if (confirmField.value.length === 0) {
            confirmField.classList.remove('is-invalid', 'is-valid');
            return;
        }
        if (pwField.value === confirmField.value) {
            confirmField.classList.remove('is-invalid');
            confirmField.classList.add('is-valid');
        } else {
            confirmField.classList.remove('is-valid');
            confirmField.classList.add('is-invalid');
        }
    }

    pwField.addEventListener('input', checkMatch);
    confirmField.addEventListener('input', checkMatch);
}

// ---- Form Validation Enhancement ----
function initFormValidation() {
    document.querySelectorAll('form.needs-validation').forEach(form => {
        form.addEventListener('submit', e => {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });
}

// ---- Auto-dismiss alerts ----
function initAutoDismissAlerts() {
    document.querySelectorAll('.alert-auto-dismiss').forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 4000);
    });
}

// ---- Accuracy bar initialization ----
function initAccuracyBars() {
    document.querySelectorAll('[data-accuracy]').forEach(el => {
        const val = parseFloat(el.dataset.accuracy) || 0;
        const fill = el.querySelector('.accuracy-fill');
        if (fill) {
            fill.style.width = '0%';
            setTimeout(() => { fill.style.width = val + '%'; }, 100);
        }
    });
}

// ---- Progress bars animation ----
function initProgressBars() {
    document.querySelectorAll('.progress-bar[data-width]').forEach(bar => {
        const w = bar.dataset.width;
        bar.style.width = '0%';
        setTimeout(() => {
            bar.style.transition = 'width 1s ease';
            bar.style.width = w + '%';
        }, 200);
    });
}

// ---- Number Counter Animation ----
function animateCounter(el, target, duration = 1000) {
    let start = 0;
    const step = target / (duration / 16);
    const timer = setInterval(() => {
        start += step;
        if (start >= target) {
            el.textContent = target;
            clearInterval(timer);
        } else {
            el.textContent = Math.floor(start);
        }
    }, 16);
}

function initCounters() {
    document.querySelectorAll('[data-count]').forEach(el => {
        const target = parseInt(el.dataset.count) || 0;
        animateCounter(el, target, 800);
    });
}

// ---- Loading button state ----
function setButtonLoading(btn, loading) {
    if (!btn) return;
    if (loading) {
        btn.dataset.originalText = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Please wait...';
        btn.disabled = true;
    } else {
        btn.innerHTML = btn.dataset.originalText || btn.innerHTML;
        btn.disabled = false;
    }
}

// ---- Table sort (simple) ----
function initTableSort() {
    document.querySelectorAll('th[data-sort]').forEach(th => {
        th.style.cursor = 'pointer';
        th.addEventListener('click', function () {
            const table = this.closest('table');
            const colIdx = Array.from(this.parentElement.children).indexOf(this);
            const rows = Array.from(table.querySelectorAll('tbody tr'));
            const asc = this.dataset.sortDir !== 'asc';
            this.dataset.sortDir = asc ? 'asc' : 'desc';
            rows.sort((a, b) => {
                const av = a.cells[colIdx] ? a.cells[colIdx].textContent.trim() : '';
                const bv = b.cells[colIdx] ? b.cells[colIdx].textContent.trim() : '';
                const an = parseFloat(av);
                const bn = parseFloat(bv);
                if (!isNaN(an) && !isNaN(bn)) return asc ? an - bn : bn - an;
                return asc ? av.localeCompare(bv) : bv.localeCompare(av);
            });
            rows.forEach(r => table.querySelector('tbody').appendChild(r));
        });
    });
}

// ---- Active nav link ----
function initActiveNavLink() {
    const path = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        if (link.getAttribute('href') && path.startsWith(link.getAttribute('href'))) {
            link.classList.add('active');
        }
    });
}

// ---- Init ----
document.addEventListener('DOMContentLoaded', () => {
    ToastManager.init();
    initPasswordToggles();
    initConfirmPasswordValidation();
    initFormValidation();
    initAutoDismissAlerts();
    initAccuracyBars();
    initProgressBars();
    initCounters();
    initTableSort();
    initActiveNavLink();

    // Initialize Bootstrap tooltips
    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
        new bootstrap.Tooltip(el);
    });
});

// Global error handler
window.addEventListener('unhandledrejection', e => {
    console.error('Unhandled promise rejection:', e.reason);
});
