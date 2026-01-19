import { useState } from 'react'
import { useToast } from '../components/common/Toast'
import Button from '../components/common/Button'
import Badge, { StockBadge } from '../components/common/Badge'
import { FiShoppingCart, FiHeart } from 'react-icons/fi'
import { LoadingContainer } from '../components/common/Loading'

function ProductPage() {
  const [loading, setLoading] = useState(false)
  const toast = useToast()

  const product = {
    nome: 'Notebook Gamer Pro',
    preco: { valor: 4999.90 },
    precoPromocional: 3999.90,
    estoque: 5,
    novo: true,
  }

  const handleAddToCart = () => {
    setLoading(true)
    setTimeout(() => {
      setLoading(false)
      toast.success('Produto adicionado ao carrinho!')
    }, 1000)
  }

  return (
    <LoadingContainer loading={loading}>
      <div className="container mx-auto px-4 py-8">
        <div className="grid md:grid-cols-2 gap-8">
          {/* Imagem */}
          <div>
            <img src="/notebook.jpg" alt={product.nome} />
          </div>

          {/* Info */}
          <div>
            <div className="flex gap-2 mb-4">
              {product.novo && <Badge variant="success">Novo</Badge>}
              <StockBadge quantity={product.quantidadeEstoque} />
            </div>

            <h1 className="text-3xl font-bold mb-4">{product.nome}</h1>

            <div className="mb-6">
              <p className="text-gray-500 line-through">
                R$ {product.preco.valor.toFixed(2)}
              </p>
              <p className="text-4xl font-bold text-primary-600">
                R$ {product.precoPromocional.toFixed(2)}
              </p>
            </div>

            <div className="flex gap-4">
              <Button
                variant="primary"
                size="lg"
                leftIcon={<FiShoppingCart />}
                onClick={handleAddToCart}
                fullWidth
              >
                Adicionar ao Carrinho
              </Button>

              <Button variant="outline" size="lg">
                <FiHeart />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </LoadingContainer>
  )
}