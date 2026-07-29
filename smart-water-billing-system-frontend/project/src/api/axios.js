import axios from 'axios'

// Base URL points at the Spring Boot backend.
// In dev, Vite proxies "/api" to http://localhost:8080 (see vite.config.js).
// In production, set VITE_API_BASE_URL to your deployed backend URL.
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' }
})

// Attach JWT (issued by Spring Security backend) to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('awb_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Global 401 handling -> force re-login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem('awb_token')
      localStorage.removeItem('awb_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api
