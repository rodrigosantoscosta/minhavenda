import { createContext, useContext, useState, useEffect } from 'react'
import { useAuth } from './AuthContext'
import { useToast } from '../components/common/Toast'
import storageUtil from '../utils/storageUtil'
import api from '../services/api'

// 1. Criar o Context
const CartContext = createContext(null)

// 2. Provider Component
export function CartProvider({ children }) {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const toast = useToast()
  const { user, isAuthenticated } = useAuth()

  // 3. Carregar carrinho ao iniciar e quando auth mudar
  useEffect(() => {
    loadCart()
  }, [isAuthenticated, user])

  // 4. Salvar carrinho quando items mudar
  useEffect(() => {
    if (items.length > 0) {
      saveCart()
    } else if (items.length === 0) {
      // Se carrinho vazio, limpar storage
      storageUtil.removeItem('cart')
    }
  }, [items])

  // Carregar carrinho (localStorage ou backend)
  const loadCart = async () => {
    try {
      if (isAuthenticated && user) {
        // Se autenticado, tentar carregar do backend
        await loadCartFromBackend()
      } else {
        // Se não autenticado, carregar do localStorage
        loadCartFromLocalStorage()
      }
    } catch (error) {
      console.error('Erro ao carregar carrinho:', error)
      // Fallback para localStorage
      loadCartFromLocalStorage()
    }
  }

  // Carregar do localStorage
  const loadCartFromLocalStorage = () => {
    try {
      const savedCart = storageUtil.getItem('cart')
      if (savedCart && Array.isArray(savedCart)) {
        setItems(savedCart)
      }
    } catch (error) {
      console.error('Erro ao carregar carrinho do localStorage:', error)
    }
  }

  // Carregar do backend (quando logado)
  const loadCartFromBackend = async () => {
    try {
      setLoading(true)
      
      // TODO: Endpoint GET /api/carrinho
      // const response = await api.get('/carrinho')
      // const backendItems = response.data.items || []
      
      // Por enquanto, mesclar localStorage com backend
      const localItems = storageUtil.getItem('cart') || []
      
      // Se tiver items locais, sincronizar com backend
      if (localItems.length > 0) {
        await syncCartWithBackend(localItems)
      }
      
      // Carregar items do backend
      // setItems(backendItems)
      
      // Temporário: usar localStorage até backend estar pronto
      setItems(localItems)
      
    } catch (error) {
      console.error('Erro ao carregar carrinho do backend:', error)
      // Fallback para localStorage
      loadCartFromLocalStorage()
    } finally {
      setLoading(false)
    }
  }

  // Sincronizar carrinho local com backend
  const syncCartWithBackend = async (localItems) => {
    try {
      // TODO: Endpoint POST /api/carrinho/sync
      // await api.post('/carrinho/sync', { items: localItems })
      
      console.log('Sincronizando carrinho com backend:', localItems)
      
      // Após sincronizar, limpar localStorage
      // storageUtil.removeItem('cart')
    } catch (error) {
      console.error('Erro ao sincronizar carrinho:', error)
    }
  }

  // Salvar no localStorage (e backend se autenticado)
  const saveCart = async () => {
    try {
      // Sempre salvar no localStorage (backup)
      storageUtil.setItem('cart', items)
      
      // Se autenticado, salvar no backend também
      if (isAuthenticated && user) {
        await saveCartToBackend()
      }
    } catch (error) {
      console.error('Erro ao salvar carrinho:', error)
    }
  }

  // Salvar no backend
  const saveCartToBackend = async () => {
    try {
      // TODO: Endpoint PUT /api/carrinho
      // await api.put('/carrinho', { items })
      
      console.log('Salvando carrinho no backend:', items)
    } catch (error) {
      console.error('Erro ao salvar carrinho no backend:', error)
    }
  }

  // 5. Adicionar item ao carrinho
  const addItem = (produto, quantidade = 1) => {
    try {
      // Verificar se produto já está no carrinho
      const existingItemIndex = items.findIndex(item => item.id === produto.id)

      if (existingItemIndex >= 0) {
        // Produto já existe, aumentar quantidade
        const updatedItems = [...items]
        const novaQuantidade = updatedItems[existingItemIndex].quantidade + quantidade

        // Verificar estoque
        if (novaQuantidade > produto.quantidadeEstoque) {
          toast.warning('Quantidade solicitada maior que o estoque disponível')
          return
        }

        updatedItems[existingItemIndex].quantidade = novaQuantidade
        setItems(updatedItems)
        toast.success('Quantidade atualizada no carrinho')
      } else {
        // Produto novo, adicionar
        if (quantidade > produto.quantidadeEstoque) {
          toast.warning('Quantidade solicitada maior que o estoque disponível')
          return
        }

        const newItem = {
          id: produto.id,
          nome: produto.nome,
          preco: produto.precoPromocional || produto.preco.valor,
          precoOriginal: produto.preco.valor,
          quantidade: quantidade,
          estoque: produto.quantidadeEstoque,
          imagem: produto.imagem,
          categoria: produto.categoria?.nome || 'Sem categoria',
        }

        setItems([...items, newItem])
        toast.success(`${produto.nome} adicionado ao carrinho!`)
      }
    } catch (error) {
      console.error('Erro ao adicionar item:', error)
      toast.error('Erro ao adicionar ao carrinho')
    }
  }

  // 6. Remover item do carrinho
  const removeItem = (produtoId) => {
    try {
      const updatedItems = items.filter(item => item.id !== produtoId)
      setItems(updatedItems)
      toast.info('Item removido do carrinho')
    } catch (error) {
      console.error('Erro ao remover item:', error)
      toast.error('Erro ao remover do carrinho')
    }
  }

  // 7. Atualizar quantidade de um item
  const updateQuantity = (produtoId, novaQuantidade) => {
    try {
      if (novaQuantidade <= 0) {
        removeItem(produtoId)
        return
      }

      const updatedItems = items.map(item => {
        if (item.id === produtoId) {
          // Verificar estoque
          if (novaQuantidade > item.estoque) {
            toast.warning('Quantidade maior que o estoque disponível')
            return item
          }
          return { ...item, quantidade: novaQuantidade }
        }
        return item
      })

      setItems(updatedItems)
    } catch (error) {
      console.error('Erro ao atualizar quantidade:', error)
      toast.error('Erro ao atualizar quantidade')
    }
  }

  // 8. Limpar carrinho completamente
  const clearCart = async () => {
    try {
      setItems([])
      storageUtil.removeItem('cart')
      
      // Se autenticado, limpar no backend também
      if (isAuthenticated && user) {
        // TODO: Endpoint DELETE /api/carrinho
        // await api.delete('/carrinho')
      }
      
      toast.info('Carrinho esvaziado')
    } catch (error) {
      console.error('Erro ao limpar carrinho:', error)
    }
  }

  // 9. Verificar se produto está no carrinho
  const isInCart = (produtoId) => {
    return items.some(item => item.id === produtoId)
  }

  // 10. Obter quantidade de um produto no carrinho
  const getItemQuantity = (produtoId) => {
    const item = items.find(item => item.id === produtoId)
    return item ? item.quantidade : 0
  }

  // 11. Calcular total de itens (quantidade)
  const getTotalItems = () => {
    return items.reduce((total, item) => total + item.quantidade, 0)
  }

  // 12. Calcular subtotal (sem descontos/frete)
  const getSubtotal = () => {
    return items.reduce((total, item) => total + (item.preco * item.quantidade), 0)
  }

  // 13. Calcular desconto total
  const getTotalDiscount = () => {
    return items.reduce((total, item) => {
      const desconto = (item.precoOriginal - item.preco) * item.quantidade
      return total + desconto
    }, 0)
  }

  // 14. Calcular total final
  const getTotal = (frete = 0) => {
    return getSubtotal() + frete
  }

  // 15. Transferir carrinho após login
  const transferLocalCartToBackend = async () => {
    try {
      const localItems = storageUtil.getItem('cart')
      
      if (localItems && localItems.length > 0) {
        console.log('Transferindo carrinho local para usuário autenticado')
        await syncCartWithBackend(localItems)
      }
    } catch (error) {
      console.error('Erro ao transferir carrinho:', error)
    }
  }

  // 16. Valor que será compartilhado
  const value = {
    items,
    loading,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    isInCart,
    getItemQuantity,
    getTotalItems,
    getSubtotal,
    getTotalDiscount,
    getTotal,
    transferLocalCartToBackend,
  }

  // 17. Retornar Provider
  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  )
}

// 18. Hook customizado
export function useCart() {
  const context = useContext(CartContext)
  
  if (!context) {
    throw new Error('useCart deve ser usado dentro de CartProvider')
  }
  
  return context
}

export default CartContext