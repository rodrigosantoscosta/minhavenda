import { createContext, useContext, useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import authService from '../services/authService'
import { useToast } from '../components/common/Toast'

// 1. Criar o Context
const AuthContext = createContext(null)

// 2. Provider Component
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  
  const navigate = useNavigate()
  const toast = useToast()

  // 3. Carregar usuário ao iniciar (se tiver token salvo)
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
      }
    } catch (error) {
      console.error('Erro ao verificar autenticação:', error)
      authService.logout()
    } finally {
      setLoading(false)
    }
  }

  // 4. Função de Login
  const login = async (email, senha) => {
    try {
      setLoading(true)
      const response = await authService.login(email, senha)
      
      setUser(response.user)
      setIsAuthenticated(true)
      
      toast.success(`Bem-vindo, ${response.user.nome}!`)
      navigate('/')
      
      return { success: true }
    } catch (error) {
      const message = error.response?.data?.message || 'Erro ao fazer login'
      toast.error(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  // 5. Função de Registro
  const register = async (nome, email, senha) => {
    try {
      setLoading(true)
      const response = await authService.register(nome, email, senha)
      
      setUser(response.user)
      setIsAuthenticated(true)
      
      toast.success('Conta criada com sucesso!')
      navigate('/')
      
      return { success: true }
    } catch (error) {
      const message = error.response?.data?.message || 'Erro ao criar conta'
      toast.error(message)
      return { success: false, error: message }
    } finally {
      setLoading(false)
    }
  }

  // 6. Função de Logout
  const logout = () => {
    authService.logout()
    setUser(null)
    setIsAuthenticated(false)
    toast.info('Você saiu da sua conta')
    navigate('/login')
  }

  // 7. Atualizar dados do usuário
  const updateUser = (updatedUser) => {
    setUser(updatedUser)
    authService.updateCurrentUser(updatedUser)
  }

  // 8. Valor que será compartilhado
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

  // 9. Retornar Provider com valor
  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

// 10. Hook customizado para usar o contexto
export function useAuth() {
  const context = useContext(AuthContext)
  
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider')
  }
  
  return context
}

export default AuthContext
