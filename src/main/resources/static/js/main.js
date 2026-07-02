/**
 * FPTU Forum — main.js
 * Cac tinh nang JavaScript don gian cho UI.
 */

// ---- Toggle hien/an form report ----
function toggleReportForm(formId) {
    const form = document.getElementById(formId);
    if (form) {
        form.classList.toggle('hidden');
    }
}

// ---- Toggle hien/an form reply comment ----
function toggleReply(commentId) {
    const form = document.getElementById('reply-form-' + commentId);
    if (form) {
        form.classList.toggle('hidden');
        if (!form.classList.contains('hidden')) {
            const textarea = form.querySelector('textarea');
            if (textarea) textarea.focus();
        }
    }
}

// ---- Toggle hien/an password ----
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const eye = document.getElementById(inputId + '-eye');
    if (input) {
        if (input.type === 'password') {
            input.type = 'text';
            if (eye) { eye.classList.remove('fa-eye'); eye.classList.add('fa-eye-slash'); }
        } else {
            input.type = 'password';
            if (eye) { eye.classList.remove('fa-eye-slash'); eye.classList.add('fa-eye'); }
        }
    }
}

// ---- Dropdown toggle khi click ----
document.addEventListener('DOMContentLoaded', function () {
    const dropdowns = document.querySelectorAll('.dropdown');
    dropdowns.forEach(function (dropdown) {
        const toggle = dropdown.querySelector('.dropdown-toggle');
        if (toggle) {
            toggle.addEventListener('click', function (e) {
                e.stopPropagation();
                dropdown.classList.toggle('open');
            });
        }
    });

    // Dong dropdown khi click ra ngoai
    document.addEventListener('click', function () {
        dropdowns.forEach(function (dropdown) {
            dropdown.classList.remove('open');
        });
    });
});

// ---- Auto-hide flash messages sau 5 giay ----
document.addEventListener('DOMContentLoaded', function () {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(function () { alert.remove(); }, 500);
        }, 5000);
    });
});

// ---- Confirm xoa bai (backup cho onsubmit) ----
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-confirm]').forEach(function (el) {
        el.addEventListener('click', function (e) {
            if (!confirm(el.getAttribute('data-confirm'))) {
                e.preventDefault();
            }
        });
    });
});
