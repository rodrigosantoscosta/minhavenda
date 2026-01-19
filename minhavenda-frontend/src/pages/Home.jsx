import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useCart } from '../contexts/CartContext'
import productService from '../services/productService'
import ProductCard from '../components/common/products/ProductCard'
import CategoryFilter from '../components/product/CategoryFilter'
import Hero from '../components/home/Hero'
import FeaturedCategories from '../components/home/FeaturedCategories'
import ProductsGrid from '../components/product/ProductsGrid'
import Pagination from '../components/common/Pagination'
import Loading from '../components/common/Loading'
import EmptyState from '../components/common/EmptyState'
import Button from '../components/common/Button'


import { 
  FiShoppingBag, 
  FiPackage
} from 'react-icons/fi'

export default function Home() {
  const { addItem } = useCart()

  // Estados
  const [produtos, setProdutos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  
  // Paginação
  const [page, setPage] = useState(0) // Backend usa 0-indexed
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const pageSize = 12

  // Filtros
  const [selectedCategory, setSelectedCategory] = useState(null)

  // Carregar dados ao montar
  useEffect(() => {
    loadInitialData()
  }, [])

  // Recarregar produtos quando categoria ou página mudar
  useEffect(() => {
    if (!loading) {
      loadProdutos()
    }
  }, [selectedCategory, page])

  const loadInitialData = async () => {
    try {
      setLoading(true)
      console.log('🔄 Carregando dados iniciais...')
      
      await Promise.all([
        loadCategorias(),
        loadProdutos(),
      ])
    } catch (error) {
      console.error('❌ Erro ao carregar dados iniciais:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadCategorias = async () => {
    try {
      console.log('📂 Buscando categorias...')
      const data = await productService.getCategorias()
      console.log('✅ Categorias recebidas:', data)
      
      // Verifica se é array
      if (Array.isArray(data)) {
        setCategorias(data)
      } else {
        console.warn('⚠️ Categorias não é um array:', data)
        setCategorias([])
      }
    } catch (error) {
      console.error('❌ Erro ao carregar categorias:', error)
      setCategorias([])
    }
  }

  const loadProdutos = async () => {
    try {
      if (page === 0) {
        setLoading(true)
      } else {
        setLoadingMore(true)
      }

      const params = {
        page: page,
        size: pageSize,
        sort: 'dataCriacao,desc',
        ativo: true
      }

      if (selectedCategory) {
        params.categoriaId = selectedCategory
      }

      console.log('📦 Buscando produtos com params:', params)
      const data = await productService.getProdutos(params)
      console.log('✅ Resposta completa do backend:', data)

      // TRATAMENTO CORRETO DOS DADOS
      let produtosArray = []
      
      // Cenário 1: Resposta paginada (Spring Page)
      if (data && data.content && Array.isArray(data.content)) {
        console.log('📄 Resposta paginada detectada')
        produtosArray = data.content
        setTotalPages(data.totalPages || 0)
        setTotalElements(data.totalElements || 0)
      }
      // Cenário 2: Array direto
      else if (Array.isArray(data)) {
        console.log('📋 Array direto detectado')
        produtosArray = data
        setTotalPages(1)
        setTotalElements(data.length)
      }
      // Cenário 3: Objeto com produtos
      else if (data && data.produtos && Array.isArray(data.produtos)) {
        console.log('📦 Objeto com array de produtos detectado')
        produtosArray = data.produtos
        setTotalPages(data.totalPages || 1)
        setTotalElements(data.total || data.produtos.length)
      }
      else {
        console.warn('⚠️ Formato de resposta desconhecido:', data)
        produtosArray = []
      }

      console.log('✅ Produtos processados:', produtosArray.length, 'itens')
      console.log('📊 Dados de paginação:', {
        totalPages,
        totalElements,
        currentPage: page
      })

      setProdutos(produtosArray)
      
    } catch (error) {
      console.error('❌ Erro ao carregar produtos:', error)
      setProdutos([])
    } finally {
      setLoading(false)
      setLoadingMore(false)
    }
  }

  const handleAddToCart = (produto) => {
    console.log('🛒 Adicionando ao carrinho:', produto)
    addItem(produto, 1)
  }

  const handleCategoryChange = (categoryId) => {
    console.log('🔄 Mudando categoria para:', categoryId)
    setSelectedCategory(categoryId)
    setPage(0) // Reset para primeira página
  }

  const handlePageChange = (newPage) => {
    console.log('📄 Mudando para página:', newPage)
    setPage(newPage - 1) // Converter para 0-indexed
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  if (loading && page === 0) {
    return <Loading />
  }

  console.log('🎨 Renderizando Home com:', produtos.length, 'produtos')

  return (
    <div className="bg-gray-50">
      {/* Hero Section */}
      <Hero />

      {/* Featured Categories */}
      {categorias.length > 0 && (
        <section className="py-12 bg-white">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-8">
              <h2 className="text-3xl font-bold text-gray-900 mb-2">
                Categorias em Destaque
              </h2>
              <p className="text-gray-600">
                Encontre o que você procura
              </p>
            </div>
            <FeaturedCategories 
              categorias={categorias} 
              onCategorySelect={handleCategoryChange}
              selectedCategory={selectedCategory}
            />
          </div>
        </section>
      )}

      {/* Catálogo Principal */}
      <section className="py-12 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center">
              <FiPackage className="text-primary-600 mr-3" size={28} />
              <div>
                <h2 className="text-3xl font-bold text-gray-900">
                  {selectedCategory 
                    ? categorias.find(c => c.id === selectedCategory)?.nome 
                    : 'Todos os Produtos'}
                </h2>
                <p className="text-gray-600 mt-1">
                  {totalElements} {totalElements === 1 ? 'produto' : 'produtos'} encontrados
                </p>
              </div>
            </div>
            
            {selectedCategory && (
              <Button
                variant="ghost"
                onClick={() => handleCategoryChange(null)}
              >
                Limpar Filtro
              </Button>
            )}
          </div>

          {/* Filtro de Categorias */}
          {categorias.length > 0 && (
            <div className="mb-8">
              <CategoryFilter
                categorias={categorias}
                selectedCategory={selectedCategory}
                onCategoryChange={handleCategoryChange}
              />
            </div>
          )}

          {/* Debug Info (REMOVER EM PRODUÇÃO) */}
          <div className="mb-4 p-4 bg-blue-50 border border-blue-200 rounded">
            <p className="text-sm text-blue-800">
              🔍 Debug: {produtos.length} produtos no estado | Página: {page + 1} | Total Páginas: {totalPages}
            </p>
          </div>

          {/* Grid de Produtos */}
          {produtos.length === 0 ? (
            <EmptyState
              icon={<FiShoppingBag size={64} />}
              title="Nenhum produto encontrado"
              description={
                selectedCategory
                  ? "Não há produtos nesta categoria no momento"
                  : "Estamos preparando novidades para você!"
              }
              action={
                selectedCategory && (
                  <Button onClick={() => handleCategoryChange(null)}>
                    Ver Todos os Produtos
                  </Button>
                )
              }
            />
          ) : (
            <>
              <ProductsGrid
                produtos={produtos}
                onAddToCart={handleAddToCart}
                loading={loadingMore}
              />

              {/* Paginação */}
              {totalPages > 1 && (
                <div className="mt-12">
                  <Pagination
                    currentPage={page + 1} // Converter para 1-indexed para UI
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                  />
                </div>
              )}
            </>
          )}
        </div>
      </section>

      {/* Call to Action */}
      <section className="py-16 bg-gradient-to-r from-primary-600 to-primary-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl font-bold text-white mb-4">
            Não encontrou o que procura?
          </h2>
          <p className="text-primary-100 mb-8 text-lg">
            Explore nossa coleção completa de produtos
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/produtos">
              <Button variant="white" size="lg">
                Ver Todos os Produtos
              </Button>
            </Link>
          </div>
        </div>
      </section>
    </div>
  )
}