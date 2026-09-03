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
  resolveFinding: (findingId) => api.put(`/compliance/findings/${findingId}/resolve`),
};

export default api;
