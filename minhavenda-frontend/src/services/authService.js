import api from './api'

const authService = {
  // Login
  async login(email, password) {
    const response = await api.post('/auth/login', {
      email,
      password,
    })
    
    // Salvar token no localStorage
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
    }
    
    return response.data
  },

  // Cadastro
  async register(userData) {
    const response = await api.post('/auth/register', userData)
    return response.data
  },

  // Logout
  logout() {
    localStorage.removeItem('token')
  },

  // Verificar se está logado
  isAuthenticated() {
    return !!localStorage.getItem('token')
  },

  // Pegar token
  getToken() {
    return localStorage.getItem('token')
  },
}

export default authService