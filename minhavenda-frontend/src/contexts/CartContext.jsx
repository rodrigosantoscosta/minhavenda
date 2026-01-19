import { createContext, useContext, useState, useEffect } from 'react'
import { useToast } from '../components/common/Toast'
import storageUtil from '../utils/storageUtil'

// 1. Criar o Context
const CartContext = createContext(null)

// 2. Provider Component
export function CartProvider({ children }) {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const toast = useToast()

  // 3. Carregar carrinho do localStorage ao iniciar
  useEffect(() => {
    loadCart()
  }, [])

  // 4. Salvar carrinho no localStorage quando mudar
  useEffect(() => {
    if (items.length > 0) {
      saveCart()
    }
  }, [items])

  // Carregar do localStorage
  const loadCart = () => {
    try {
      const savedCart = storageUtil.getItem('cart')
      if (savedCart) {
        setItems(savedCart)
      }
    } catch (error) {
      console.error('Erro ao carregar carrinho:', error)
    }
  }

  // Salvar no localStorage
  const saveCart = () => {
    try {
      storageUtil.setItem('cart', items)
    } catch (error) {
      console.error('Erro ao salvar carrinho:', error)
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
          categoria: produto.categoriaNome || 'Sem categoria',
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
      
      // Se carrinho ficar vazio, limpar localStorage
      if (updatedItems.length === 0) {
        storageUtil.removeItem('cart')
      }
      
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
          if (novaQuantidade > item.quantidadeEstoque) {
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
  const clearCart = () => {
    try {
      setItems([])
      storageUtil.removeItem('cart')
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

  // 15. Valor que será compartilhado
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
  }

  // 16. Retornar Provider
  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  )
}

// 17. Hook customizado
export function useCart() {
  const context = useContext(CartContext)
  
  if (!context) {
    throw new Error('useCart deve ser usado dentro de CartProvider')
  }
  
  return context
}

export default CartContext
