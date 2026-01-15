import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useToast } from '../components/common/Toast'
import Input from '../components/common/Input'
import Button from '../components/common/Button'
import { FiMail, FiLock } from 'react-icons/fi'
import authService from '../services/authService'

/**
 * Página de login
 * 
 * @returns {JSX.Element} - Componente React que representa a página de login
 */

export default function Login() {
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [loading, setLoading] = useState(false)
  
  const toast = useToast()
  const navigate = useNavigate()

  const handleLogin = async (e) => {
    e.preventDefault()
    
    // Validação simples
    if (!email || !senha) {
      toast.error('Preencha todos os campos')
      return
    }

    setLoading(true)

    try {
      const response = await authService.login(email, senha)

      // Salvar token no localStorage
      localStorage.setItem('token', response.token)

      // Salvar dados do usuário no localStorage
      localStorage.setItem('user', JSON.stringify(response.user))
      
      // Redirecionar para login
      setTimeout(() => {
        setLoading(false)
        toast.success('Login realizado com sucesso!')
        navigate('/')
      }, 2000)
    } catch (error) {
      setLoading(false)
        toast.error(error.response?.data?.message || 'Email ou senha incorretos')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-center mb-8">
          Login
        </h1>

        <form onSubmit={handleLogin} className="space-y-4">
          <Input
            type="email"
            label="Email"
            placeholder="seu@email.com"
            leftIcon={<FiMail />}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <Input
            type="password"
            label="Senha"
            placeholder="••••••••"
            leftIcon={<FiLock />}
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
          />

          <Button
            type="submit"
            variant="primary"
            fullWidth
            loading={loading}
          >
            Entrar
          </Button>
        </form>

        <p className="text-center mt-4 text-sm text-gray-600">
          Não tem conta?{' '}
          <a href="/cadastro" className="text-primary-600 hover:underline">
            Cadastre-se
          </a>
        </p>
      </div>
    </div>
  )
}