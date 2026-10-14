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

  let response;
  try {
    response = await fetch(url, {
      ...options,
      headers
    });
  } catch (netErr) {
    const error = new Error('Cannot connect to backend server. Please verify the Spring Boot service is running on port 8080.');
    error.status = 503;
    throw error;
  }

  if (response.status === 401 && !endpoint.startsWith('/api/auth/')) {
    tokenStorage.clear();
    window.location.hash = 'login';
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  let data;
  try {
    if (contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }
  } catch (parseErr) {
    data = null;
  }

  if (!response.ok) {
    let errorMsg = `Request failed with status ${response.status}`;
    let fieldErrors = null;

    if (data && typeof data === 'object') {
      if (data.message) {
        errorMsg = data.message;
      } else if (data.error) {
        errorMsg = data.error;
      }
      if (data.fieldErrors) {
        fieldErrors = data.fieldErrors;
      }
    } else if (typeof data === 'string' && (
      data.includes('ECONNREFUSED') || 
      data.includes('Proxy error') || 
      data.includes('Bad Gateway') || 
      data.includes('Service Unavailable') || 
      data.includes('<!DOCTYPE') || 
      data.includes('<html')
    )) {
      errorMsg = 'Cannot connect to backend server. Please verify the Spring Boot service is running on port 8080.';
    } else if (response.status === 502 || response.status === 503 || response.status === 504 || (response.status === 500 && !data)) {
      errorMsg = 'Cannot connect to backend server. Please verify the Spring Boot service is running on port 8080.';
    } else if (response.status === 500) {
      errorMsg = 'Backend service is starting or unavailable. Please ensure Spring Boot is running on port 8080.';
    }

    const error = new Error(errorMsg);
    error.status = response.status;
    error.data = data;
    error.fieldErrors = fieldErrors;
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
  }),

  // Job Discovery & Automated Matching
  getMatches: (params = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') query.append(k, v);
    });
    const qs = query.toString();
    return apiRequest(`/api/matches${qs ? `?${qs}` : ''}`);
  },
  getTopMatches: (limit = 5) => apiRequest(`/api/matches/top?limit=${limit}`),
  getMatch: (opportunityId) => apiRequest(`/api/matches/${opportunityId}`),
  recalculateMatches: () => apiRequest('/api/matches/recalculate', {
    method: 'POST'
  }),
  importJobs: (params = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') query.append(k, v);
    });
    const qs = query.toString();
    return apiRequest(`/api/opportunities/import${qs ? `?${qs}` : ''}`, {
      method: 'POST'
    });
  }
};
