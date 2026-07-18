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

// ---- Lay CSRF token tu meta tag hoac cookie ----
function getCsrfToken() {
    const meta = document.querySelector('meta[name="_csrf"]');
    const header = document.querySelector('meta[name="_csrf_header"]');
    if (meta && header) {
        return { token: meta.getAttribute('content'), header: header.getAttribute('content') };
    }
    // Fallback: lay tu hidden input dau tien tren trang
    const input = document.querySelector('input[name="_csrf"]');
    return input ? { token: input.value, header: 'X-CSRF-TOKEN' } : null;
}

// ---- AJAX: Like / Unlike bai viet ----
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.js-like-post').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const postId = btn.getAttribute('data-post-id');
            const csrf = getCsrfToken();
            const headers = { 'Content-Type': 'application/json' };
            if (csrf) headers[csrf.header] = csrf.token;

            fetch('/posts/' + postId + '/like', { method: 'POST', headers: headers })
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    btn.querySelector('.like-count').textContent = data.likeCount;
                    btn.querySelector('.like-label').textContent = data.liked ? 'Bỏ thích' : 'Thích';
                    btn.classList.toggle('btn-liked', data.liked);
                    btn.setAttribute('data-liked', data.liked);
                })
                .catch(function () { alert('Có lỗi xảy ra, vui lòng thử lại.'); });
        });
    });
});

// ---- AJAX: Like / Unlike comment ----
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.js-like-comment').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const commentId = btn.getAttribute('data-comment-id');
            const csrf = getCsrfToken();
            const headers = { 'Content-Type': 'application/json' };
            if (csrf) headers[csrf.header] = csrf.token;

            fetch('/comments/' + commentId + '/like', { method: 'POST', headers: headers })
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    btn.querySelector('.like-count').textContent = data.likeCount;
                    btn.classList.toggle('btn-liked', data.liked);
                    btn.setAttribute('data-liked', data.liked);
                })
                .catch(function () { alert('Có lỗi xảy ra, vui lòng thử lại.'); });
        });
    });
});

// ---- AJAX: Save / Unsave bai viet ----
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.js-save-post').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const postId = btn.getAttribute('data-post-id');
            const csrf = getCsrfToken();
            const headers = { 'Content-Type': 'application/json' };
            if (csrf) headers[csrf.header] = csrf.token;

            fetch('/posts/' + postId + '/save', { method: 'POST', headers: headers })
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    btn.querySelector('.save-label').textContent = data.saved ? 'Đã lưu' : 'Lưu bài';
                    btn.classList.toggle('btn-saved', data.saved);
                    btn.setAttribute('data-saved', data.saved);
                })
                .catch(function () { alert('Có lỗi xảy ra, vui lòng thử lại.'); });
        });
    });
});

// ---- AJAX: Unsave (Bo luu) tren trang bookmarks ----
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.js-unsave-bookmark').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const postId = btn.getAttribute('data-post-id');
            const csrf = getCsrfToken();
            const headers = { 'Content-Type': 'application/json' };
            if (csrf) headers[csrf.header] = csrf.token;

            btn.disabled = true;

            fetch('/posts/' + postId + '/save', { method: 'POST', headers: headers })
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    if (!data.saved) {
                        // Xoa card khoi DOM
                        const card = btn.closest('.post-card');
                        if (card) card.remove();

                        // Neu khong con card nao, reload de hien empty-state
                        if (document.querySelectorAll('.post-card').length === 0) {
                            window.location.reload();
                        }
                    } else {
                        // Trang thai bat ngo (saved lai) - phuc hoi nut
                        btn.disabled = false;
                    }
                })
                .catch(function () {
                    btn.disabled = false;
                    alert('Có lỗi xảy ra, vui lòng thử lại.');
                });
        });
    });
});
