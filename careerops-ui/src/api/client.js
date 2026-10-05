const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export const tokenStorage = {
  get: () => localStorage.getItem('careerops_token'),
  set: (token) => localStorage.setItem('careerops_token', token),
  clear: () => localStorage.removeItem('careerops_token'),
  has: () => !!localStorage.getItem('careerops_token')
};

export async function apiRequest(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const headers = { ...options.headers };

  const token = tokenStorage.get();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (!(options.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(url, {
    ...options,
    headers
  });

  if (response.status === 401) {
    tokenStorage.clear();
    if (!window.location.pathname.startsWith('/login') && !window.location.pathname.startsWith('/register')) {
      window.location.href = '/login';
    }
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  let data;
  if (contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    const errorMsg = (data && typeof data === 'object' && (data.message || data.error))
      ? (data.message || data.error)
      : (typeof data === 'string' && data ? data : `Request failed with status ${response.status}`);
    const error = new Error(errorMsg);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

export const api = {
  // Auth
  login: (credentials) => apiRequest('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials)
  }),
  register: (data) => apiRequest('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(data)
  }),

  // Candidate
  getProfile: () => apiRequest('/api/candidates/me'),
  updateProfile: (data) => apiRequest('/api/candidates/me', {
    method: 'PUT',
    body: JSON.stringify(data)
  }),
  uploadResume: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiRequest('/api/candidates/me/resume', {
      method: 'POST',
      body: formData
    });
  },

  // Opportunities
  getOpportunities: (params = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') query.append(k, v);
    });
    return apiRequest(`/api/opportunities?${query.toString()}`);
  },
  getOpportunity: (id) => apiRequest(`/api/opportunities/${id}`),
  createOpportunity: (data) => apiRequest('/api/opportunities', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  updateOpportunity: (id, data) => apiRequest(`/api/opportunities/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  }),
  deleteOpportunity: (id) => apiRequest(`/api/opportunities/${id}`, {
    method: 'DELETE'
  }),
  getClosingSoon: (days = 14) => apiRequest(`/api/opportunities/closing-soon?days=${days}`),

  // Job Fit & AI Review
  getJobFit: (id) => apiRequest(`/api/opportunities/${id}/fit`),
  getAiReview: (id) => apiRequest(`/api/opportunities/${id}/ai-review`, {
    method: 'POST'
  }),
  getResumeAnalysis: () => apiRequest('/api/candidates/me/resume-analysis'),

  // Skills
  getSkills: () => apiRequest('/api/skills'),
  createSkill: (data) => apiRequest('/api/skills', {
    method: 'POST',
    body: JSON.stringify(data)
  }),

  // Applications
  getApplications: (params = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') query.append(k, v);
    });
    return apiRequest(`/api/applications?${query.toString()}`);
  },
  getApplicationStats: () => apiRequest('/api/applications/stats'),
  createApplication: (data) => apiRequest('/api/applications', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  updateApplication: (id, data) => apiRequest(`/api/applications/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  }),
  deleteApplication: (id) => apiRequest(`/api/applications/${id}`, {
    method: 'DELETE'
  })
};
