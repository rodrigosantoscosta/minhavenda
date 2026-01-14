import axios from 'axios'

// URL base da API (backend)
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

// Criar instância do Axios
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Interceptor para adicionar token em todas requisições
api.interceptors.request.use(
  (config) => {
    // Pegar token do localStorage
    const token = localStorage.getItem('token')
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor para tratar erros de resposta
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    // Se erro 401 (não autorizado), redirecionar para login
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    
    return Promise.reject(error)
  }
)

export default api