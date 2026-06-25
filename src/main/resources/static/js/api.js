const API_BASE = '';

async function apiRequest(path, options = {}) {
  const config = {
    credentials: 'include',
    headers: {
      ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
      ...options.headers
    },
    ...options
  };
  const response = await fetch(API_BASE + path, config);
  if (response.status === 401 && !path.includes('/auth/login')) {
    throw new Error('Please login to continue');
  }
  const contentType = response.headers.get('content-type') || '';
  let data = null;
  if (contentType.includes('application/json')) {
    data = await response.json();
  } else if (!response.ok) {
    throw new Error('Request failed');
  }
  if (!response.ok) {
    throw new Error(data?.error || 'Request failed');
  }
  return data;
}

const Api = {
  register: (body) => apiRequest('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  verifyEmail: (body) => apiRequest('/api/auth/verify-email', { method: 'POST', body: JSON.stringify(body) }),
  resendOtp: (body) => apiRequest('/api/auth/resend-registration-otp', { method: 'POST', body: JSON.stringify(body) }),
  sendLoginOtp: (body) => apiRequest('/api/auth/send-login-otp', { method: 'POST', body: JSON.stringify(body) }),
  login: (body) => apiRequest('/api/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  verifyLoginOtp: (body) => apiRequest('/api/auth/verify-login-otp', { method: 'POST', body: JSON.stringify(body) }),
  me: () => apiRequest('/api/auth/me'),
  logout: () => apiRequest('/api/auth/logout', { method: 'POST' }).catch(() => {}),

  saveDonorProfile: (body) => apiRequest('/api/donor/profile', { method: 'POST', body: JSON.stringify(body) }),
  getDonorProfile: () => apiRequest('/api/donor/profile'),
  uploadDocument: (documentType, file) => {
    const form = new FormData();
    form.append('documentType', documentType);
    form.append('file', file);
    return apiRequest('/api/donor/documents', { method: 'POST', body: form });
  },

  searchDonors: (body) => apiRequest('/api/search/donors', { method: 'POST', body: JSON.stringify(body) }),

  adminStats: () => apiRequest('/api/admin/stats'),
  adminDonors: (status) => apiRequest('/api/admin/donors' + (status ? '?status=' + status : '')),
  adminDonorDocs: (donorId) => apiRequest('/api/admin/donors/' + donorId + '/documents'),
  verifyDonor: (donorId, body) => apiRequest('/api/admin/donors/' + donorId + '/verify', { method: 'PUT', body: JSON.stringify(body) }),
  verifyDocument: (docId, body) => apiRequest('/api/admin/documents/' + docId + '/verify', { method: 'PUT', body: JSON.stringify(body) }),
  documentFileUrl: (docId) => '/api/admin/documents/' + docId + '/file'
};

function showAlert(containerId, message, type = 'error') {
  const el = document.getElementById(containerId);
  if (!el) return;
  el.className = 'alert alert-' + type;
  el.textContent = message;
  el.classList.remove('hidden');
}

function hideAlert(containerId) {
  const el = document.getElementById(containerId);
  if (el) el.classList.add('hidden');
}

function bloodGroupOptions(selectEl) {
  const groups = [
    { v: 'A_POSITIVE', l: 'A+' },
    { v: 'A_NEGATIVE', l: 'A-' },
    { v: 'B_POSITIVE', l: 'B+' },
    { v: 'B_NEGATIVE', l: 'B-' },
    { v: 'AB_POSITIVE', l: 'AB+' },
    { v: 'AB_NEGATIVE', l: 'AB-' },
    { v: 'O_POSITIVE', l: 'O+' },
    { v: 'O_NEGATIVE', l: 'O-' }
  ];
  selectEl.innerHTML = groups.map(g => '<option value="' + g.v + '">' + g.l + '</option>').join('');
}

function getLocation(callback) {
  if (!navigator.geolocation) {
    callback(null, 'Geolocation is not supported by your browser');
    return;
  }
  navigator.geolocation.getCurrentPosition(
    (pos) => callback({ lat: pos.coords.latitude, lng: pos.coords.longitude }, null),
    () => callback(null, 'Unable to get location. Enter coordinates manually or allow location access.')
  );
}

async function requireRole(allowedRoles, redirect = '/') {
  try {
    const me = await Api.me();
    if (!me.authenticated || !allowedRoles.includes(me.role)) {
      window.location.href = redirect;
      return null;
    }
    return me;
  } catch {
    window.location.href = redirect;
    return null;
  }
}
