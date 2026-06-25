let authMode = 'login';
let pendingEmail = null;

const overlay = document.getElementById('modalOverlay');
const authForm = document.getElementById('authForm');
const modalTitle = document.getElementById('modalTitle');

function openModal(mode) {
  authMode = mode;
  modalTitle.textContent = mode === 'login' ? 'Login' : 'Create Account';
  document.getElementById('authSubmit').textContent = mode === 'login' ? 'Login' : 'Register';
  hideAlert('modalAlert');
  overlay.classList.remove('hidden');
  resetAuthForm();
}

function resetAuthForm() {
  authForm.innerHTML = `
    <div class="form-group" id="emailGroup">
      <label for="authEmail">Email</label>
      <input type="email" id="authEmail" required>
    </div>

    <div class="form-group">
      <label for="authPassword">Password</label>
      <input type="password" id="authPassword" required minlength="6">
    </div>

    <div class="modal-actions">
      <button type="submit" class="btn btn-primary btn-block" id="authSubmit">
        ${authMode === 'login' ? 'Login' : 'Register'}
      </button>

      <button type="button" class="btn btn-outline" id="modalClose">
        Cancel
      </button>
    </div>
  `;

  document
    .getElementById('modalClose')
    .addEventListener('click', closeModal);
}
function closeModal() {
  overlay.classList.add('hidden');
  resetAuthForm();
}

document.getElementById('btnLogin').addEventListener('click', () => openModal('login'));
document.getElementById('btnRegister').addEventListener('click', () => openModal('register'));
document.getElementById('heroRegister').addEventListener('click', () => openModal('register'));
document.getElementById('registerUserBtn').addEventListener('click', () => openModal('register'));
overlay.addEventListener('click', (e) => { if (e.target === overlay) closeModal(); });

authForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const email = document.getElementById('authEmail').value.trim();
  const password = document.getElementById('authPassword').value;
  try {
    if (authMode === 'register') {
      await Api.register({ email, password });
      showAlert('modalAlert', 'Registered! OTP sent to your email. Please verify.', 'success');
      showOtpVerification(email, 'REGISTRATION');
      return;
    }
    // Login - first verify password
    const res = await Api.login({ email, password });
    showAlert('modalAlert', 'Password verified. OTP sent to your email.', 'success');
    showOtpVerification(email, 'LOGIN');
  } catch (err) {
    // Check if error is about unverified email
    if (err.message.includes('not verified') || err.message.includes('Resend OTP') || err.message.includes('already registered')) {
      showAlert('modalAlert', err.message + ' Click "Resend OTP" below to get a new verification code.', 'error');
      showResendOtpButton(email);
    } else {
      showAlert('modalAlert', err.message);
    }
  }
});

function showOtpVerification(email, otpType) {
  pendingEmail = email;
  modalTitle.textContent = otpType === 'REGISTRATION' ? 'Verify Email' : 'Verify Login';
  hideAlert('modalAlert');
  
  const formHtml = `
    <div class="form-group">
      <label for="otpCode">Enter OTP sent to ${email}</label>
      <input type="text" id="otpCode" required maxlength="6" pattern="[0-9]{6}" placeholder="123456">
    </div>
    <div class="modal-actions">
      <button type="submit" class="btn btn-primary btn-block" id="otpSubmit">Verify</button>
      <button type="button" class="btn btn-outline" id="modalClose">Cancel</button>
    </div>
  `;
  authForm.innerHTML = formHtml;
  
  document.getElementById('modalClose').addEventListener('click', closeModal);
  
  authForm.onsubmit = async (e) => {
    e.preventDefault();
    const otpCode = document.getElementById('otpCode').value;
    try {
      if (otpType === 'REGISTRATION') {
        await Api.verifyEmail({ email, otpCode });
        showAlert('modalAlert', 'Email verified! Please login.', 'success');
        setTimeout(() => {
          closeModal();
          openModal('login');
        }, 1500);
      } else {
        await Api.verifyLoginOtp({ email, otpCode });
        closeModal();
        redirectByRole('USER');
      }
    } catch (err) {
      showAlert('modalAlert', err.message);
    }
  };
}

function showResendOtpButton(email) {
  // Remove existing resend button if any
  const existingBtn = document.getElementById('resendOtpBtn');
  if (existingBtn) existingBtn.remove();
  
  const actionsDiv = document.querySelector('.modal-actions');
  if (actionsDiv) {
    const resendBtn = document.createElement('button');
    resendBtn.type = 'button';
    resendBtn.className = 'btn btn-primary';
    resendBtn.id = 'resendOtpBtn';
    resendBtn.textContent = 'Resend OTP';
    resendBtn.style.marginTop = '10px';
    resendBtn.style.width = '100%';
    resendBtn.style.display = 'block';
    
    resendBtn.addEventListener('click', async () => {
      try {
        await Api.resendOtp({ email });
        showAlert('modalAlert', 'OTP resent to your email. Please check your email or console.', 'success');
        showOtpVerification(email, 'REGISTRATION');
      } catch (err) {
        showAlert('modalAlert', err.message);
      }
    });
    
    actionsDiv.appendChild(resendBtn);
  }
}

function redirectByRole(role) {
  if (role === 'USER') window.location.href = '/pages/user.html';
  else if (role === 'ADMIN') window.location.href = '/pages/admin.html';
  else window.location.reload();
}

async function initNav() {
  try {
    const me = await Api.me();
    if (me.authenticated) {
      const nav = document.getElementById('navLinks');
      nav.innerHTML = '<span style="color:#fff;opacity:0.9">' + me.email + ' (' + me.role + ')</span>';
      const dash = document.createElement('a');
      dash.href = me.role === 'USER' ? '/pages/user.html' : '/pages/admin.html';
      dash.textContent = 'Dashboard';
      const logout = document.createElement('button');
      logout.textContent = 'Logout';
      logout.addEventListener('click', async () => {
        await Api.logout();
        window.location.reload();
      });
      nav.appendChild(dash);
      nav.appendChild(logout);
    }
  } catch (_) {}
}

initNav();
