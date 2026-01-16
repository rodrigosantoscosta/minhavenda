// src/services/authService.js
import api from './api'

const authService = {
  /**
   * Login do usuário
   */
  async login(email, senha) {
    try {
      const response = await api.post('/auth/login', {
        email,
        senha
      })

      // Salvar token no localStorage
      if (response.data.token) {
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
      }

      return response.data
    } catch (error) {
      console.error('Erro no login:', error)
      throw error
    }
  },

  /**
   * Registro de novo usuário
   */
  async register(nome, email, senha) {
    try {
      const response = await api.post('/auth/register', {
        nome,
        email,
        senha
      })

      // Salvar token no localStorage
      if (response.data.token) {
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
      }

      return response.data
    } catch (error) {
      console.error('Erro no registro:', error)
      throw error
    }
  },

  /**
   * Logout do usuário
   */
  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },

  /**
   * Verificar se usuário está autenticado
   */
  isAuthenticated() {
    return !!localStorage.getItem('token')
  },

  /**
   * Obter usuário logado
   */
  getCurrentUser() {
    const user = localStorage.getItem('user')
    return user ? JSON.parse(user) : null
  },

  /**
   * Obter token
   */
  getToken() {
    return localStorage.getItem('token')
  }
}

export default authService