import api from './api'

const categoryService = {
  /**
   * Listar todas categorias ativas
   */
  async getCategorias() {
    try {
      const response = await api.get('/categorias')
      return response.data
    } catch (error) {
      console.error('Erro ao buscar categorias:', error)
      throw error
    }
  },

  /**
   * Listar categorias com paginação
   */
  async getCategoriasPaginado(params = {}) {
    try {
      const response = await api.get('/categorias/paginado', { params })
      return response.data
    } catch (error) {
      console.error('Erro ao buscar categorias paginadas:', error)
      throw error
    }
  },

  /**
   * Buscar categoria por ID
   */
  async getCategoriaById(id) {
    try {
      const response = await api.get(`/categorias/${id}`)
      return response.data
    } catch (error) {
      console.error('Erro ao buscar categoria:', error)
      throw error
    }
  },

  /**
   * Listar todas categorias (incluindo inativas) - ADMIN
   */
  async getTodasCategorias() {
    try {
      const response = await api.get('/categorias/todas')
      return response.data
    } catch (error) {
      console.error('Erro ao buscar todas categorias:', error)
      throw error
    }
  }
}

export default categoryService