import axios from 'axios'

// URL base da API (backend)
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

// Criar instância do Axios
const api = axios.create({
  // Em produção, usar URL relativa (Nginx faz proxy)
  baseURL: import.meta.env.PROD 
    ? '/api'  // Produção (Docker)
    : 'http://localhost:8080/api', // Desenvolvimento
  headers: {
    'Content-Type': 'application/json'
  }
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

/// 4. Interceptor de RESPOSTA (tratar erros globalmente)
api.interceptors.response.use(
  (response) => {
    // Log para debug (remover em produção)
    console.log(' Resposta:', {
      status: response.status,
      url: response.config.url,
      data: response.data,
    })
    
    return response
  },
  (error) => {
    // Pegar informações do erro
    const status = error.response?.status
    const message = error.response?.data?.message || error.message
    const url = error.config?.url
    
    console.error(' Erro na resposta:', {
      status,
      message,
      url,
    })
    
    // Tratamento de erros específicos
    switch (status) {
      case 401:
        // Não autorizado - Token inválido/expirado
        console.warn(' Token inválido ou expirado')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        
        // Redirecionar para login (apenas se não estiver na página de login)
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        break
        
      case 403:
        // Proibido - Sem permissão
        console.warn(' Sem permissão para acessar este recurso')
        break
        
      case 404:
        // Não encontrado
        console.warn(' Recurso não encontrado:', url)
        break
        
      case 500:
        // Erro interno do servidor
        console.error(' Erro interno do servidor')
        break
        
      case 503:
        // Serviço indisponível
        console.error(' Serviço temporariamente indisponível')
        break
        
      default:
        // Outros erros
        if (!error.response) {
          // Erro de rede (sem resposta do servidor)
          console.error(' Erro de rede - Servidor inacessível')
        }
    }
    
    return Promise.reject(error)
  }
)

// 5. Funções auxiliares para chamadas comuns

/**
 * GET - Buscar dados
 */
export const get = async (url, config = {}) => {
  try {
    const response = await api.get(url, config)
    return response.data
  } catch (error) {
    throw error
  }
}

/**
 * POST - Criar dados
 */
export const post = async (url, data = {}, config = {}) => {
  try {
    const response = await api.post(url, data, config)
    return response.data
  } catch (error) {
    throw error
  }
}

/**
 * PUT - Atualizar dados completos
 */
export const put = async (url, data = {}, config = {}) => {
  try {
    const response = await api.put(url, data, config)
    return response.data
  } catch (error) {
    throw error
  }
}

/**
 * PATCH - Atualizar dados parciais
 */
export const patch = async (url, data = {}, config = {}) => {
  try {
    const response = await api.patch(url, data, config)
    return response.data
  } catch (error) {
    throw error
  }
}

/**
 * DELETE - Deletar dados
 */
export const del = async (url, config = {}) => {
  try {
    const response = await api.delete(url, config)
    return response.data
  } catch (error) {
    throw error
  }
}

// 6. Exportar instância e funções
export default api