import { get, post } from './api'
import logger from '../utils/logger'

/**
 * OrderService
 * 
 * Serviço para gerenciamento de pedidos do usuário
 * Implementação híbrida: mock local + estrutura para API real
 */

// Mock data para desenvolvimento
const USE_MOCK = false // Mudar para false quando usar API real

// Mock de pedidos do usuário
let mockUserOrders = []

/**
 * Buscar todos os pedidos do usuário logado
 * @param {Object} options - Opções de busca
 * @param {string} options.status - Filtrar por status
 * @param {number} options.page - Número da página
 * @param {number} options.limit - Limite de resultados
 * @returns {Promise<Object>} - Lista de pedidos com paginação
 */
export const getMyOrders = async (options = {}) => {
  try {
    logger.info({ options }, 'Buscando pedidos do usuário')

    if (USE_MOCK) {
      return await getMyOrdersMock(options)
    }

    return await getMyOrdersAPI(options)

  } catch (error) {
    logger.error({ error, options }, 'Erro ao buscar pedidos do usuário')
    throw error
  }
}

/**
 * Implementação Mock para buscar pedidos do usuário
 */
async function getMyOrdersMock(options = {}) {
  // Simular delay de rede
  await new Promise(resolve => setTimeout(resolve, 800))

  // Carregar pedidos do localStorage
  const stored = localStorage.getItem('mockOrders')
  if (stored) {
    mockUserOrders = JSON.parse(stored)
  }

  // Se não tiver pedidos, criar alguns dados de exemplo
  if (mockUserOrders.length === 0) {
    mockUserOrders = generateMockOrders()
    localStorage.setItem('mockOrders', JSON.stringify(mockUserOrders))
  }

  let filteredOrders = [...mockUserOrders]

  // Filtrar por status
  if (options.status) {
    filteredOrders = filteredOrders.filter(order => order.status === options.status)
  }

  // Ordenar por data (mais recente primeiro)
  filteredOrders.sort((a, b) => new Date(b.dataCriacao) - new Date(a.dataCriacao))

  // Paginação
  const page = options.page || 1
  const limit = options.limit || 10
  const startIndex = (page - 1) * limit
  const endIndex = startIndex + limit

  const paginatedOrders = filteredOrders.slice(startIndex, endIndex)

  return {
    orders: paginatedOrders,
    pagination: {
      page,
      limit,
      total: filteredOrders.length,
      totalPages: Math.ceil(filteredOrders.length / limit),
      hasNext: endIndex < filteredOrders.length,
      hasPrev: page > 1
    }
  }
}

/**
 * Implementação API para buscar pedidos do usuário
 */
async function getMyOrdersAPI(options = {}) {
  const params = new URLSearchParams()
  
  if (options.status) params.append('status', options.status)
  if (options.page) params.append('page', options.page)
  if (options.limit) params.append('limit', options.limit)

  const response = await get(`/meus-pedidos?${params.toString()}`)
  return response
}

/**
 * Buscar detalhes de um pedido específico
 * @param {string} orderId - ID do pedido
 * @returns {Promise<Object>} - Detalhes do pedido
 */
export const getOrderDetails = async (orderId) => {
  try {
    logger.info({ orderId }, 'Buscando detalhes do pedido')

    if (USE_MOCK) {
      return await getOrderDetailsMock(orderId)
    }

    return await getOrderDetailsAPI(orderId)

  } catch (error) {
    logger.error({ error, orderId }, 'Erro ao buscar detalhes do pedido')
    throw error
  }
}

/**
 * Implementação Mock para buscar detalhes do pedido
 */
async function getOrderDetailsMock(orderId) {
  // Carregar pedidos do localStorage
  const stored = localStorage.getItem('mockOrders')
  if (stored) {
    mockUserOrders = JSON.parse(stored)
  }

  const order = mockUserOrders.find(o => o.id === orderId)
  
  if (!order) {
    throw new Error('Pedido não encontrado')
  }

  // Adicionar informações adicionais para detalhes
  return {
    ...order,
    rastreamento: generateTrackingInfo(order.status),
    historico: generateOrderHistory(order),
    estimativaEntrega: calculateDeliveryEstimate(order)
  }
}

/**
 * Implementação API para buscar detalhes do pedido
 */
async function getOrderDetailsAPI(orderId) {
  const response = await get(`/pedidos/${orderId}`)
  return response
}

/**
 * Cancelar um pedido
 * @param {string} orderId - ID do pedido
 * @param {string} motivo - Motivo do cancelamento
 * @returns {Promise<Object>} - Pedido atualizado
 */
export const cancelOrder = async (orderId, motivo = '') => {
  try {
    logger.info({ orderId, motivo }, 'Cancelando pedido')

    if (USE_MOCK) {
      return await cancelOrderMock(orderId, motivo)
    }

    return await cancelOrderAPI(orderId, motivo)

  } catch (error) {
    logger.error({ error, orderId }, 'Erro ao cancelar pedido')
    throw error
  }
}

/**
 * Implementação Mock para cancelar pedido
 */
async function cancelOrderMock(orderId, motivo) {
  await new Promise(resolve => setTimeout(resolve, 1000))

  // Carregar pedidos do localStorage
  const stored = localStorage.getItem('mockOrders')
  if (stored) {
    mockUserOrders = JSON.parse(stored)
  }

  const orderIndex = mockUserOrders.findIndex(o => o.id === orderId)
  
  if (orderIndex === -1) {
    throw new Error('Pedido não encontrado')
  }

  const order = mockUserOrders[orderIndex]
  
  // Verificar se pode cancelar
  if (order.status === 'ENVIADO' || order.status === 'ENTREGUE') {
    throw new Error('Este pedido não pode mais ser cancelado')
  }

  // Atualizar status
  mockUserOrders[orderIndex] = {
    ...order,
    status: 'CANCELADO',
    dataCancelamento: new Date().toISOString(),
    motivoCancelamento: motivo
  }

  // Salvar no localStorage
  localStorage.setItem('mockOrders', JSON.stringify(mockUserOrders))

  return mockUserOrders[orderIndex]
}

/**
 * Implementação API para cancelar pedido
 */
async function cancelOrderAPI(orderId, motivo) {
  const response = await post(`/pedidos/${orderId}/cancelar`, { motivo })
  return response
}

/**
 * Gerar pedidos mock para testes
 */
function generateMockOrders() {
  const statuses = ['PENDENTE', 'PAGO', 'ENVIADO', 'ENTREGUE', 'CANCELADO']
  const produtos = [
    { id: '1', nome: 'Notebook Gamer Pro', imagem: 'https://via.placeholder.com/100' },
    { id: '2', nome: 'Mouse Wireless RGB', imagem: 'https://via.placeholder.com/100' },
    { id: '3', nome: 'Teclado Mecânico', imagem: 'https://via.placeholder.com/100' },
    { id: '4', nome: 'Monitor 4K', imagem: 'https://via.placeholder.com/100' },
    { id: '5', nome: 'Headset Bluetooth', imagem: 'https://via.placeholder.com/100' }
  ]

  const orders = []
  
  // Gerar 5 pedidos de exemplo
  for (let i = 1; i <= 5; i++) {
    const numItens = Math.floor(Math.random() * 3) + 1
    const itens = []
    
    for (let j = 0; j < numItens; j++) {
      const produto = produtos[Math.floor(Math.random() * produtos.length)]
      const quantidade = Math.floor(Math.random() * 2) + 1
      const preco = Math.floor(Math.random() * 500) + 100
      const precoOriginal = preco + Math.floor(Math.random() * 200)
      
      itens.push({
        id: produto.id,
        produto,
        quantidade,
        precoUnitario: preco,
        precoOriginal,
        subtotal: preco * quantidade
      })
    }

    const subtotal = itens.reduce((total, item) => total + item.subtotal, 0)
    const desconto = Math.floor(Math.random() * 100)
    const frete = subtotal >= 200 ? 0 : 15
    const total = subtotal - desconto + frete

    // Data aleatória nos últimos 30 dias
    const diasAtras = Math.floor(Math.random() * 30)
    const dataCriacao = new Date()
    dataCriacao.setDate(dataCriacao.getDate() - diasAtras)

    orders.push({
      id: `PED${String(i).padStart(6, '0')}`,
      dataCriacao: dataCriacao.toISOString(),
      status: statuses[Math.floor(Math.random() * statuses.length)],
      itens,
      endereco: {
        rua: 'Rua das Flores',
        numero: '123',
        bairro: 'Centro',
        cidade: 'São Paulo',
        estado: 'SP',
        cep: '01234-567'
      },
      pagamento: {
        metodo: 'PIX',
        status: 'PAGO'
      },
      valores: {
        subtotal,
        desconto,
        frete,
        total
      },
      usuario: {
        id: '1',
        nome: 'Usuário Teste',
        email: 'teste@exemplo.com'
      }
    })
  }

  return orders
}

/**
 * Gerar informações de rastreamento
 */
function generateTrackingInfo(status) {
  if (status === 'PENDENTE' || status === 'CANCELADO') {
    return null
  }

  const codigos = {
    'PAGO': 'AGUARDANDO_ENVIO',
    'ENVIADO': 'BR123456789BR',
    'ENTREGUE': 'BR123456789BR'
  }

  return {
    codigo: codigos[status] || null,
    status: status === 'ENTREGUE' ? 'Entregue' : 'Em trânsito',
    ultimaAtualizacao: new Date().toISOString()
  }
}

/**
 * Gerar histórico do pedido
 */
function generateOrderHistory(order) {
  const history = [
    {
      data: order.dataCriacao,
      status: 'PENDENTE',
      descricao: 'Pedido criado e aguardando pagamento'
    }
  ]

  // Adicionar histórico baseado no status atual
  if (order.status === 'PAGO' || order.status === 'ENVIADO' || order.status === 'ENTREGUE') {
    history.push({
      data: new Date(order.dataCriacao).getTime() + 3600000, // +1 hora
      status: 'PAGO',
      descricao: 'Pagamento confirmado'
    })
  }

  if (order.status === 'ENVIADO' || order.status === 'ENTREGUE') {
    history.push({
      data: new Date(order.dataCriacao).getTime() + 7200000, // +2 horas
      status: 'ENVIADO',
      descricao: 'Pedido enviado para transportadora'
    })
  }

  if (order.status === 'ENTREGUE') {
    history.push({
      data: new Date(order.dataCriacao).getTime() + 86400000, // +1 dia
      status: 'ENTREGUE',
      descricao: 'Pedido entregue com sucesso'
    })
  }

  if (order.status === 'CANCELADO') {
    history.push({
      data: new Date(order.dataCriacao).getTime() + 3600000, // +1 hora
      status: 'CANCELADO',
      descricao: order.motivoCancelamento || 'Pedido cancelado pelo usuário'
    })
  }

  return history
}

/**
 * Calcular estimativa de entrega
 */
function calculateDeliveryEstimate(order) {
  if (order.status === 'ENTREGUE' || order.status === 'CANCELADO') {
    return null
  }

  const dataCriacao = new Date(order.dataCriacao)
  const diasUteis = 7

  // Calcular data estimada (ignorando fins de semana)
  let dataEstimada = new Date(dataCriacao)
  let diasAdicionados = 0

  while (diasAdicionados < diasUteis) {
    dataEstimada.setDate(dataEstimada.getDate() + 1)
    
    // Ignorar fins de semana (0 = domingo, 6 = sábado)
    if (dataEstimada.getDay() !== 0 && dataEstimada.getDay() !== 6) {
      diasAdicionados++
    }
  }

  return dataEstimada.toISOString()
}

/**
 * Limpar dados mock (para testes)
 */
export const clearOrderMockData = () => {
  mockUserOrders = []
  localStorage.removeItem('mockOrders')
  localStorage.removeItem('lastOrder')
}