import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  verifyEmail: (token) => api.post('/auth/verify-email', { token }),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (token, newPassword) => api.post('/auth/reset-password', { token, newPassword }),
};

// Profile API
export const profileAPI = {
  getProfile: () => api.get('/profile'),
  updateProfile: (data) => api.put('/profile', data),
  changePassword: (data) => api.post('/profile/change-password', data),
};

// Company API
export const companyAPI = {
  getMyCompanies: () => api.get('/companies'),
  getCompany: (id) => api.get(`/companies/${id}`),
  createCompany: (data) => api.post('/companies', data),
  updateCompany: (id, data) => api.put(`/companies/${id}`, data),
};

// Document API
export const documentAPI = {
  upload: (companyId, file, type) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return api.post(`/documents/upload/${companyId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
  getDocuments: (companyId) => api.get(`/documents/company/${companyId}`),
};

// Compliance API
export const complianceAPI = {
  runCheck: (companyId) => api.post(`/compliance/check/${companyId}`),
  getFindings: (companyId, category) =>
    api.get(`/compliance/findings/${companyId}`, { params: { category } }),
  getScores: (companyId) => api.get(`/compliance/scores/${companyId}`),
  getScoreHistory: (companyId) => api.get(`/compliance/scores/${companyId}/history`),
  resolveFinding: (findingId) => api.put(`/compliance/findings/${findingId}/resolve`),
  getGapReport: (companyId) => api.get(`/compliance/report/${companyId}`),
  getPdfReport: (companyId) => api.get(`/compliance/report/${companyId}/pdf`, { responseType: 'text' }),
};

// Stripe API
export const stripeAPI = {
  createCheckout: (plan) => api.post('/stripe/checkout', { plan }),
};

export default api;
