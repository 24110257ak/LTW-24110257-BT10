/**
 * mainjs.js - Client-side JavaScript for JWT Authentication
 *
 * Features:
 *  1. Login form: AJAX POST to /auth/login, saves JWT to localStorage
 *  2. Register form: AJAX POST to /auth/signup
 *  3. Profile page: AJAX GET to /users/me with Bearer token in header
 *  4. Logout: Clears localStorage and redirects to /login
 *
 * Library: jQuery 3.7.1
 */

$(document).ready(function () {

    // ================================================================
    // === CONSTANTS
    // ================================================================
    const TOKEN_KEY = 'jwt_token';
    const API_BASE  = '';  // same origin

    // ================================================================
    // === UTILITY FUNCTIONS
    // ================================================================

    /**
     * Show an alert message
     * @param {string} elementId - The ID of the alert div
     * @param {string} message - The message to display
     * @param {boolean} isSuccess - true = green success, false = red error
     */
    function showAlert(elementId, message, isSuccess) {
        const el = $('#' + elementId);
        el.removeClass('alert-success alert-danger');
        el.addClass(isSuccess ? 'alert-success' : 'alert-danger');
        el.find('span[id$="Msg"]').last().text(message);
        // Find the matching message span
        if (elementId === 'alertSuccess') $('#successMsg').text(message);
        if (elementId === 'alertError')   $('#errorMsg').text(message);
        el.fadeIn(300);
        // Auto-hide after 5 seconds
        setTimeout(() => el.fadeOut(300), 5000);
    }

    /**
     * Save JWT token to localStorage
     */
    function saveToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    }

    /**
     * Get JWT token from localStorage
     */
    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    /**
     * Remove JWT token from localStorage (logout)
     */
    function removeToken() {
        localStorage.removeItem(TOKEN_KEY);
    }

    /**
     * Build Authorization header with Bearer token
     */
    function authHeader() {
        const token = getToken();
        return token ? { 'Authorization': 'Bearer ' + token } : {};
    }

    /**
     * Format ISO datetime string to Vietnamese locale
     */
    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        try {
            return new Date(dateStr).toLocaleString('vi-VN', {
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit'
            });
        } catch (e) {
            return dateStr;
        }
    }

    // ================================================================
    // === LOGIN PAGE (/login)
    // ================================================================

    if ($('#loginForm').length) {

        // ---- Login Form Submit ----
        $('#loginForm').on('submit', function (e) {
            e.preventDefault();

            const email    = $('#email').val().trim();
            const password = $('#password').val().trim();

            if (!email || !password) {
                showAlert('alertError', 'Vui lòng nhập đầy đủ email và mật khẩu.', false);
                return;
            }

            // Show spinner
            $('#btnText').text('Đang đăng nhập...');
            $('#btnSpinner').removeClass('d-none');
            $('#btnLogin').prop('disabled', true);

            // AJAX POST to /auth/login
            $.ajax({
                url: API_BASE + '/auth/login',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ email: email, password: password }),
                success: function (data) {
                    // data = { token: "eyJ...", expiresIn: 3600000 }
                    saveToken(data.token);

                    // Show token in debug box
                    $('#tokenBox').text(data.token).show();

                    showAlert('alertSuccess',
                        'Đăng nhập thành công! Token đã được lưu. Đang chuyển hướng...', true);

                    // Redirect to profile page after short delay
                    setTimeout(function () {
                        window.location.href = '/user/profile';
                    }, 1500);
                },
                error: function (xhr) {
                    let message = 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.';
                    if (xhr.status === 403 || xhr.status === 401) {
                        message = 'Email hoặc mật khẩu không đúng.';
                    } else if (xhr.status === 0) {
                        message = 'Không thể kết nối đến máy chủ.';
                    }
                    showAlert('alertError', message, false);
                },
                complete: function () {
                    // Reset button
                    $('#btnText').text('Đăng Nhập');
                    $('#btnSpinner').addClass('d-none');
                    $('#btnLogin').prop('disabled', false);
                }
            });
        });

        // ---- Register Form Submit (Modal) ----
        $('#btnRegister').on('click', function () {
            const fullName = $('#regFullName').val().trim();
            const email    = $('#regEmail').val().trim();
            const password = $('#regPassword').val().trim();

            if (!fullName || !email || !password) {
                $('#registerAlert')
                    .removeClass('alert-success')
                    .addClass('alert alert-danger')
                    .text('Vui lòng điền đầy đủ thông tin.')
                    .show();
                return;
            }

            if (password.length < 6) {
                $('#registerAlert')
                    .removeClass('alert-success')
                    .addClass('alert alert-danger')
                    .text('Mật khẩu phải có ít nhất 6 ký tự.')
                    .show();
                return;
            }

            $(this).prop('disabled', true).html(
                '<span class="spinner-border spinner-border-sm me-1"></span>Đang đăng ký...'
            );

            // AJAX POST to /auth/signup
            $.ajax({
                url: API_BASE + '/auth/signup',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ fullName: fullName, email: email, password: password }),
                success: function (user) {
                    $('#registerAlert')
                        .removeClass('alert-danger')
                        .addClass('alert alert-success')
                        .html('<i class="fas fa-check-circle me-1"></i>Đăng ký thành công! Email: <strong>' + user.email + '</strong>')
                        .show();

                    // Pre-fill login form
                    $('#email').val(email);
                    $('#password').val('');

                    // Close modal after delay
                    setTimeout(function () {
                        $('#registerModal').modal('hide');
                        $('#registerAlert').hide();
                        $('#registerForm')[0].reset();
                    }, 2000);
                },
                error: function (xhr) {
                    let message = 'Đăng ký thất bại.';
                    if (xhr.status === 400) {
                        message = 'Email đã tồn tại hoặc thông tin không hợp lệ.';
                    }
                    $('#registerAlert')
                        .removeClass('alert-success')
                        .addClass('alert alert-danger')
                        .text(message)
                        .show();
                },
                complete: function () {
                    $('#btnRegister').prop('disabled', false).html(
                        '<i class="fas fa-user-plus me-1"></i>Đăng Ký'
                    );
                }
            });
        });

        // ---- Auto-redirect if token already exists ----
        if (getToken()) {
            window.location.href = '/user/profile';
        }
    }

    // ================================================================
    // === PROFILE PAGE (/user/profile)
    // ================================================================

    if ($('#profileBody').length) {

        const token = getToken();

        // If no token, redirect to login
        if (!token) {
            window.location.href = '/login';
            return;
        }

        // Show token in display box
        $('#tokenDisplay').text(token);

        // AJAX GET to /users/me with Bearer token in Authorization header
        $.ajax({
            url: API_BASE + '/users/me',
            type: 'GET',
            headers: authHeader(),   // { Authorization: "Bearer eyJ..." }
            success: function (user) {
                // Update navbar
                $('#navUserName').text(user.fullName || user.email);

                // Update profile header
                $('#profileName').text(user.fullName || 'Người dùng');

                // Update info fields
                $('#userId').text(user.id || 'N/A');
                $('#userFullName').text(user.fullName || 'N/A');
                $('#userEmail').text(user.email || 'N/A');
                $('#userCreatedAt').text(formatDate(user.createdAt));

                // Show user info, hide loading spinner
                $('#loadingState').hide();
                $('#userInfoSection').show();
                $('#actionButtons').show().css('display', 'flex');
            },
            error: function (xhr) {
                if (xhr.status === 401 || xhr.status === 403) {
                    // Token expired or invalid — clear and redirect
                    removeToken();
                    alert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
                    window.location.href = '/login';
                } else {
                    $('#loadingState').html(
                        '<div class="text-danger"><i class="fas fa-exclamation-triangle me-2"></i>' +
                        'Không thể tải thông tin người dùng (HTTP ' + xhr.status + ')</div>'
                    );
                }
            }
        });

        // ---- Logout Buttons ----
        $('#btnLogout, #btnLogout2').on('click', function () {
            removeToken();
            window.location.href = '/login';
        });
    }

    // ================================================================
    // === UTILITY: Copy token to clipboard
    // ================================================================
    window.copyToken = function () {
        const token = getToken();
        if (token) {
            navigator.clipboard.writeText(token).then(function () {
                // Simple feedback
                const btn = $('[onclick="copyToken()"]');
                btn.html('<i class="fas fa-check text-success"></i>');
                setTimeout(() => btn.html('<i class="fas fa-copy"></i>'), 2000);
            });
        }
    };

});
