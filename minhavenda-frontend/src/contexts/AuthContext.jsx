import { createContext, useContext, useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import authService from '../services/authService'
import logger from '../utils/logger'

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
        logger.info({ userId: savedUser.id, email: savedUser.email }, 'Usuário autenticado')
      }
    } catch (error) {
      logger.error({ error }, 'Erro ao verificar autenticação')
      authService.logout()
    } finally {
      setLoading(false)
    }
  }

  const login = async (email, senha) => {
    try {
      logger.info({ email }, 'Tentando login')
      setLoading(true)
      
      const response = await authService.login(email, senha)
      logger.debug({ response }, 'Resposta do backend')
      
      // Extrair user da resposta
      // Backend pode retornar: { token, user } ou { token, nome, email, ... }
      let userData = response.user || response
      
      logger.debug({ userData }, 'Dados do usuário extraídos')
      
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
        logger.error({ response }, 'Resposta do backend sem campo "nome"')
        throw new Error('Resposta do servidor inválida')
      }
      
      setUser(userData)
      setIsAuthenticated(true)
      logger.info({ userId: userData.id, email: userData.email }, 'Login realizado com sucesso')
      
      alert(`Bem-vindo, ${userData.nome}!`)
      
      setTimeout(() => {
        navigate('/')
      }, 100)
      
      return { success: true }
    } catch (error) {
      logger.error({ error }, 'Erro ao fazer login')
      
      let message = 'Erro ao fazer login'
      
      if (error.response) {
        // Erro da API
        logger.error({ status: error.response.status }, 'Erro de resposta da API')
        message = error.response.data?.message || 
                 error.response.data?.error ||
                 `Erro ${error.response.status}: ${error.response.statusText}`
      } else if (error.request) {
        // Requisição enviada mas sem resposta
        logger.error('Servidor não respondeu')
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
      logger.info({ nome, email }, 'Tentando registrar')
      setLoading(true)
      
      const response = await authService.register(nome, email, senha)
      logger.debug({ response }, 'Resposta do backend')
      
      // Extrair user da resposta
      let userData = response.user || response
      
      logger.debug({ userData }, 'Dados do usuário extraídos')
      
      if (!userData.nome && response.nome) {
        userData = {
          nome: response.nome,
          email: response.email,
          id: response.id,
          tipo: response.tipo
        }
      }
      
      if (!userData.nome) {
        logger.error({ response }, 'Resposta do backend sem campo "nome"')
        throw new Error('Resposta do servidor inválida')
      }
      
      setUser(userData)
      setIsAuthenticated(true)
      logger.info({ userId: userData.id, email: userData.email }, 'Registrado com sucesso')
      
      alert('Conta criada com sucesso!')
      
      setTimeout(() => {
        navigate('/')
      }, 100)
      
      return { success: true }
    } catch (error) {
      logger.error({ error }, 'Erro ao criar conta')
      
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
    logger.info('Realizando logout')
    authService.logout()
    setUser(null)
    setIsAuthenticated(false)
    alert('Você saiu da sua conta')
    navigate('/login')
  }

  const updateUser = (updatedUser) => {
    logger.info({ userId: updatedUser?.id }, 'Atualizando usuário')
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