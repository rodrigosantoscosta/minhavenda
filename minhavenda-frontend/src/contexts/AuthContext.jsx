import { createContext, useContext, useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import authService from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  
  const navigate = useNavigate()

  useEffect(() => {
    checkAuth()
  }, [])

  const checkAuth = () => {
    try {
      const token = authService.getToken()
      const savedUser = authService.getCurrentUser()
      
      if (token && savedUser) {
        setUser(savedUser)
        setIsAuthenticated(true)
        console.log('✅ Usuário autenticado:', savedUser)
      }
    } catch (error) {
      console.error('❌ Erro ao verificar autenticação:', error)
      authService.logout()
    } finally {
      setLoading(false)
    }
  }

  const login = async (email, senha) => {
    try {
      console.log('🔄 Tentando login...', { email })
      setLoading(true)
      
      const response = await authService.login(email, senha)
      console.log('📦 Resposta completa do backend:', response)
      
      // Extrair user da resposta
      // Backend pode retornar: { token, user } ou { token, nome, email, ... }
      let userData = response.user || response
      
      console.log('👤 Dados do usuário:', userData)
      
      // Se ainda não tiver nome, tentar pegar do próprio response
      if (!userData.nome && response.nome) {
        userData = {
          nome: response.nome,
          email: response.email,
          id: response.id,
          tipo: response.tipo
        }
      }
      
      // Verificar se tem dados mínimos
      if (!userData.nome) {
        console.error('❌ Resposta do backend sem campo "nome":', response)
        throw new Error('Resposta do servidor inválida')
      }
      
      setUser(userData)
      setIsAuthenticated(true)
      
      alert(`Bem-vindo, ${userData.nome}!`)
      
      setTimeout(() => {
        navigate('/')
      }, 100)
      
      return { success: true }
    } catch (error) {
      console.error('❌ Erro completo no login:', error)
      
      let message = 'Erro ao fazer login'
      
      if (error.response) {
        // Erro da API
        console.log('Response error:', error.response)
        message = error.response.data?.message || 
                 error.response.data?.error ||
                 `Erro ${error.response.status}: ${error.response.statusText}`
      } else if (error.request) {
        // Requisição enviada mas sem resposta
        console.log('Request error:', error.request)
        message = 'Servidor não respondeu. Verifique se o backend está rodando.'
      } else {
        // Erro na configuração da requisição
        message = error.message
      }
      
      alert(message)
      
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const register = async (nome, email, senha) => {
    try {
      console.log('🔄 Tentando registrar...', { nome, email })
      setLoading(true)
      
      const response = await authService.register(nome, email, senha)
      console.log('📦 Resposta completa do backend:', response)
      
      // Extrair user da resposta
      let userData = response.user || response
      
      console.log('👤 Dados do usuário:', userData)
      
      if (!userData.nome && response.nome) {
        userData = {
          nome: response.nome,
          email: response.email,
          id: response.id,
          tipo: response.tipo
        }
      }
      
      if (!userData.nome) {
        console.error('❌ Resposta do backend sem campo "nome":', response)
        throw new Error('Resposta do servidor inválida')
      }
      
      setUser(userData)
      setIsAuthenticated(true)
      
      alert('Conta criada com sucesso!')
      
      setTimeout(() => {
        navigate('/')
      }, 100)
      
      return { success: true }
    } catch (error) {
      console.error('❌ Erro completo no registro:', error)
      
      let message = 'Erro ao criar conta'
      
      if (error.response) {
        message = error.response.data?.message || 
                 error.response.data?.error ||
                 `Erro ${error.response.status}: ${error.response.statusText}`
      } else if (error.request) {
        message = 'Servidor não respondeu.'
      } else {
        message = error.message
      }
      
      alert(message)
      
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  const logout = () => {
    console.log('🔄 Fazendo logout...')
    authService.logout()
    setUser(null)
    setIsAuthenticated(false)
    alert('Você saiu da sua conta')
    navigate('/login')
  }

  const updateUser = (updatedUser) => {
    console.log('🔄 Atualizando usuário...', updatedUser)
    setUser(updatedUser)
    authService.updateCurrentUser(updatedUser)
  }

  const value = {
    user,
    loading,
    isAuthenticated,
    login,
    register,
    logout,
    updateUser,
    checkAuth,
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider')
  }
  
  return context
}

export default AuthContext