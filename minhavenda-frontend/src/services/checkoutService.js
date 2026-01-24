import { post, get } from './api'
import logger from '../utils/logger'

/**
 * CheckoutService
 * 
 * Serviço para processamento de checkout e pedidos
 * Implementação híbrida: mock local + estrutura para API real
 */

// Mock data para desenvolvimento
const USE_MOCK = false // Mudar para false quando usar API real

// Mock de pedidos criados
let mockOrders = []
let mockOrderIdCounter = 1

/**
 * Criar um novo pedido
 * @param {Object} orderData - Dados do pedido
 * @param {Array} orderData.items - Itens do carrinho
 * @param {Object} orderData.endereco - Endereço de entrega
 * @param {Object} orderData.pagamento - Dados do pagamento
 * @param {number} orderData.total - Valor total do pedido
 * @returns {Promise<Object>} - Pedido criado
 */
export const createOrder = async (orderData) => {
  try {
    logger.info({ orderData }, 'Iniciando criação de pedido')

    // Validação básica
    if (!orderData.items || orderData.items.length === 0) {
      throw new Error('Carrinho vazio. Adicione itens antes de finalizar.')
    }

    if (!orderData.endereco) {
      throw new Error('Endereço de entrega é obrigatório')
    }

    if (!orderData.total || orderData.total <= 0) {
      throw new Error('Valor total inválido')
    }

    // Mock implementation
    if (USE_MOCK) {
      return await createOrderMock(orderData)
    }

    // API implementation (quando disponível)
    return await createOrderAPI(orderData)

  } catch (error) {
    logger.error({ error, orderData }, 'Erro ao criar pedido')
    throw error
  }
}

/**
 * Implementação Mock para criação de pedido
 */
async function createOrderMock(orderData) {
  // Simular delay de rede
  await new Promise(resolve => setTimeout(resolve, 1500))

  const newOrder = {
    id: `PED${String(mockOrderIdCounter++).padStart(6, '0')}`,
    dataCriacao: new Date().toISOString(),
    status: 'PENDENTE',
    itens: orderData.items.map(item => ({
      id: item.id,
      produto: {
        id: item.id,
        nome: item.nome,
        imagem: item.imagem
      },
      quantidade: item.quantidade,
      precoUnitario: item.preco,
      precoOriginal: item.precoOriginal,
      subtotal: item.preco * item.quantidade
    })),
    endereco: {
      ...orderData.endereco,
      id: `END${Date.now()}`
    },
    pagamento: {
      metodo: orderData.pagamento?.metodo || 'PIX',
      status: 'PENDENTE',
      ...orderData.pagamento
    },
    valores: {
      subtotal: orderData.valores?.subtotal || calcularSubtotal(orderData.items),
      desconto: orderData.valores?.desconto || calcularDesconto(orderData.items),
      frete: orderData.valores?.frete || 15.00,
      total: orderData.total
    },
    usuario: {
      id: orderData.usuario?.id || '1',
      nome: orderData.usuario?.nome || 'Usuário Teste',
      email: orderData.usuario?.email || 'teste@exemplo.com'
    }
  }

  // Salvar no mock storage
  mockOrders.push(newOrder)
  
  // Salvar no localStorage para persistência
  localStorage.setItem('mockOrders', JSON.stringify(mockOrders))
  localStorage.setItem('lastOrder', JSON.stringify(newOrder))

  logger.info({ orderId: newOrder.id }, 'Pedido criado com sucesso (mock)')

  return newOrder
}

/**
 * Implementação API para criação de pedido
 */
async function createOrderAPI(orderData) {
  // Montar objeto no formato esperado pelo backend Java (CheckoutRequest)
  const checkoutRequest = {
    enderecoEntrega: JSON.stringify(orderData.endereco),
    observacoes: orderData.observacoes || ''
  }

  const response = await post('/checkout/finalizar', checkoutRequest)
  return response
}

/**
 * Buscar pedido por ID
 * @param {string} orderId - ID do pedido
 * @returns {Promise<Object>} - Pedido encontrado
 */
export const getOrderById = async (orderId) => {
  try {
    logger.info({ orderId }, 'Buscando pedido por ID')

    if (USE_MOCK) {
      return await getOrderByIdMock(orderId)
    }

    return await getOrderByIdAPI(orderId)

  } catch (error) {
    logger.error({ error, orderId }, 'Erro ao buscar pedido')
    throw error
  }
}

/**
 * Implementação Mock para buscar pedido por ID
 */
async function getOrderByIdMock(orderId) {
  // Carregar do localStorage
  const stored = localStorage.getItem('mockOrders')
  if (stored) {
    mockOrders = JSON.parse(stored)
  }

  const order = mockOrders.find(o => o.id === orderId)
  
  if (!order) {
    throw new Error('Pedido não encontrado')
  }

  return order
}

/**
 * Implementação API para buscar pedido por ID
 */
async function getOrderByIdAPI(orderId) {
  const response = await get(`/pedidos/${orderId}`)
  return response
}

/**
 * Calcular subtotal dos itens
 */
function calcularSubtotal(items) {
  return items.reduce((total, item) => {
    return total + (item.preco * item.quantidade)
  }, 0)
}

/**
 * Calcular desconto total dos itens
 */
function calcularDesconto(items) {
  return items.reduce((total, item) => {
    if (item.precoOriginal > item.preco) {
      return total + ((item.precoOriginal - item.preco) * item.quantidade)
    }
    return total
  }, 0)
}

/**
 * Calcular frete (simulado)
 */
export const calcularFrete = (subtotal, endereco) => {
  // Frete grátis acima de R$ 200
  if (subtotal >= 200) return 0
  
  // Simular diferentes valores por região
  const cep = endereco?.cep
  if (!cep) return 15.00

  // Lógica simples baseada no CEP
  const firstDigit = parseInt(cep[0])
  switch (firstDigit) {
    case 0: case 1: case 2: // Norte, Nordeste
      return 30.00
    case 3: case 4: case 5: // Sudeste, Centro-Oeste
      return 15.00
    case 6: case 7: case 8: case 9: // Sul
      return 20.00
    default:
      return 15.00
  }
}

/**
 * Validar dados do endereço
 */
export const validarEndereco = (endereco) => {
  const errors = {}

  if (!endereco.cep || endereco.cep.length < 8) {
    errors.cep = 'CEP inválido'
  }

  if (!endereco.rua || endereco.rua.trim().length < 5) {
    errors.rua = 'Rua é obrigatória'
  }

  if (!endereco.numero || endereco.numero.trim().length < 1) {
    errors.numero = 'Número é obrigatório'
  }

  if (!endereco.bairro || endereco.bairro.trim().length < 3) {
    errors.bairro = 'Bairro é obrigatório'
  }

  if (!endereco.cidade || endereco.cidade.trim().length < 3) {
    errors.cidade = 'Cidade é obrigatória'
  }

  if (!endereco.estado || endereco.estado.length !== 2) {
    errors.estado = 'Estado inválido'
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  }
}

/**
 * Limpar dados mock (para testes)
 */
export const clearMockData = () => {
  mockOrders = []
  mockOrderIdCounter = 1
  localStorage.removeItem('mockOrders')
  localStorage.removeItem('lastOrder')
}

/**
 * Obter último pedido criado (mock)
 */
export const getLastOrder = () => {
  if (USE_MOCK) {
    const stored = localStorage.getItem('lastOrder')
    return stored ? JSON.parse(stored) : null
  }
  return null
}