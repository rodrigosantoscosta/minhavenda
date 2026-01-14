import { useState } from 'react'
import { useToast } from '../components/common/Toast'
import Button from '../components/common/Button'
import Input from '../components/common/Input'
import ProductCard from '../components/common/ProductCard'
import OrderCard from '../components/common/OrderCard'
import Modal, { ConfirmModal } from '../components/common/Modal'
import Badge, { StatusBadge, StockBadge, DiscountBadge } from '../components/common/Badge'
import { 
  Spinner, 
  LoadingOverlay, 
  LoadingContainer,
  ProductCardSkeleton 
} from '../components/common/Loading'
import { 
  FiShoppingCart, 
  FiHeart, 
  FiMail,   
  FiLock,
  FiUser 
} from 'react-icons/fi'

export default function TestComponents() {
  const toast = useToast()
  
  // Estados para controlar modais
  const [showModal, setShowModal] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [loading, setLoading] = useState(false)
  const [showOverlay, setShowOverlay] = useState(false)

  // Produto de exemplo
  const produtoExemplo = {
    id: 1,
    nome: 'Notebook Gamer Pro',
    descricao: 'Notebook potente para jogos e trabalho pesado',
    preco: { valor: 4999.90 },
    precoPromocional: 3999.90,
    imagem: 'https://via.placeholder.com/300',
    estoque: 5,
    categoria: { nome: 'Eletrônicos' },
    novo: true,
    emPromocao: true,
  }

  // Pedido de exemplo
  const pedidoExemplo = {
    id: 12345,
    dataCriacao: '2025-01-13',
    status: 'ENVIADO',
    total: 4999.90,
    itens: [
      { 
        produto: { 
          nome: 'Notebook', 
          imagem: 'https://via.placeholder.com/50' 
        } 
      },
      { 
        produto: { 
          nome: 'Mouse', 
          imagem: 'https://via.placeholder.com/50' 
        } 
      },
    ],
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold mb-8">
        🧪 Teste de Componentes
      </h1>

      {/* ========== SEÇÃO 1: BUTTONS ========== */}
      <section className="mb-12">
        <h2 className="text-2xl font-bold mb-4">1️⃣ Buttons (Botões)</h2>
        <div className="bg-white p-6 rounded-lg shadow space-y-4">
          
          <div>
            <p className="text-sm text-gray-600 mb-2">Variantes:</p>
            <div className="flex flex-wrap gap-2">
              <Button variant="primary" onClick={() => toast.success('Primary clicado!')}>
                Primary
              </Button>
              <Button variant="secondary">
                Secondary
              </Button>
              <Button variant="outline">
                Outline
              </Button>
              <Button variant="danger">
                Danger
              </Button>
              <Button variant="ghost">
                Ghost
              </Button>
              <Button variant="success">
                Success
              </Button>
            </div>
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Tamanhos:</p>
            <div className="flex items-center gap-2">
              <Button size="sm">Small</Button>
              <Button size="md">Medium</Button>
              <Button size="lg">Large</Button>
            </div>
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Com Ícones:</p>
            <div className="flex gap-2">
              <Button leftIcon={<FiShoppingCart />}>
                Adicionar
              </Button>
              <Button variant="outline" leftIcon={<FiHeart />}>
                Favoritar
              </Button>
            </div>
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Estados:</p>
            <div className="flex gap-2">
              <Button loading={true}>
                Loading...
              </Button>
              <Button disabled>
                Disabled
              </Button>
              <Button fullWidth>
                Full Width
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* ========== SEÇÃO 2: INPUTS ========== */}
      <section className="mb-12">
        <h2 className="text-2xl font-bold mb-4">2️⃣ Inputs (Campos)</h2>
        <div className="bg-white p-6 rounded-lg shadow space-y-4">
          
          <Input
            label="Nome Completo"
            placeholder="Digite seu nome"
            leftIcon={<FiUser />}
          />

          <Input
            type="email"
            label="Email"
            placeholder="seu@email.com"
            leftIcon={<FiMail />}
            helperText="Nunca compartilharemos seu email"
          />

          <Input
            type="password"
            label="Senha"
            placeholder="••••••••"
            leftIcon={<FiLock />}
          />

          <Input
            type="number"
            label="Quantidade"
            placeholder="0"
          />

          <Input
            label="Campo com Erro"
            error="Este campo é obrigatório"
          />

          <div className="grid grid-cols-3 gap-4">
            <Input size="sm" placeholder="Small" />
            <Input size="md" placeholder="Medium" />
            <Input size="lg" placeholder="Large" />
          </div>
        </div>
      </section>

      {/* ========== SEÇÃO 3: BADGES ========== */}
      <section className="mb-12">
        <h2 className="text-2xl font-bold mb-4">3️⃣ Badges (Etiquetas)</h2>
        <div className="bg-white p-6 rounded-lg shadow space-y-4">
          
          <div>
            <p className="text-sm text-gray-600 mb-2">Variantes:</p>
            <div className="flex flex-wrap gap-2">
              <Badge variant="primary">Primary</Badge>
              <Badge variant="secondary">Secondary</Badge>
              <Badge variant="success">Success</Badge>
              <Badge variant="danger">Danger</Badge>
              <Badge variant="warning">Warning</Badge>
              <Badge variant="info">Info</Badge>
            </div>
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Com Dot:</p>
            <div className="flex gap-2">
              <Badge variant="success" dot>Ativo</Badge>
              <Badge variant="danger" dot>Inativo</Badge>
            </div>
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Status de Pedidos:</p>
            <div className="flex flex-wrap gap-2">
              <StatusBadge status="PENDENTE" />
              <StatusBadge status="PAGO" />
              <StatusBadge status="ENVIADO" />
              <StatusBadge status="ENTREGUE" />
              <StatusBadge status="CANCELADO" />
            </div>
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Estoque:</p>
            <div className="flex gap-2">
              <StockBadge quantity={50} />
              <StockBadge quantity={5} />
              <StockBadge quantity={0} />
            </div>
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Desconto:</p>
            <DiscountBadge percentage={25} />
          </div>
        </div>
      </section>

      {/* ========== SEÇÃO 4: TOAST ========== */}
      <section className="mb-12">
        <h2 className="text-2xl font-bold mb-4">4️⃣ Toast (Notificações)</h2>
        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-sm text-gray-600 mb-4">
            Clique nos botões para ver as notificações aparecerem no canto da tela:
          </p>
          <div className="flex flex-wrap gap-2">
            <Button 
              variant="success"
              onClick={() => toast.success('Operação realizada com sucesso!')}
            >
              Success Toast
            </Button>
            <Button 
              variant="danger"
              onClick={() => toast.error('Erro ao processar a operação')}
            >
              Error Toast
            </Button>
            <Button 
              variant="warning"
              onClick={() => toast.warning('Atenção: estoque baixo!')}
            >
              Warning Toast
            </Button>
            <Button 
              onClick={() => toast.info('Produto atualizado')}
            >
              Info Toast
            </Button>
          </div>
        </div>
      </section>

      {/* ========== SEÇÃO 5: LOADING ========== */}
      <section className="mb-12">
        <h2 className="text-2xl font-bold mb-4">5️⃣ Loading (Carregamento)</h2>
        <div className="bg-white p-6 rounded-lg shadow space-y-6">
          
          <div>
            <p className="text-sm text-gray-600 mb-2">Spinners:</p>
            <div className="flex items-center gap-4">
              <Spinner size="sm" />
              <Spinner size="md" />
              <Spinner size="lg" />
              <Spinner size="xl" />
            </div>
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Loading Container:</p>
            <LoadingContainer loading={loading} message="Carregando dados...">
              <div className="p-4 bg-green-50 rounded">
                <p className="text-green-800">✅ Conteúdo carregado!</p>
              </div>
            </LoadingContainer>
            <Button 
              onClick={() => setLoading(!loading)}
              className="mt-2"
            >
              Toggle Loading
            </Button>
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Loading Overlay (Tela Inteira):</p>
            <Button onClick={() => {
              setShowOverlay(true)
              setTimeout(() => setShowOverlay(false), 2000)
            }}>
              Mostrar Overlay (2s)
            </Button>
            {showOverlay && <LoadingOverlay message="Processando..." />}
          </div>

          <div>
            <p className="text-sm text-gray-600 mb-2">Skeleton (Placeholder):</p>
            <div className="grid grid-cols-3 gap-4">
              <ProductCardSkeleton />
              <ProductCardSkeleton />
              <ProductCardSkeleton />
            </div>
          </div>
        </div>
      </section>

      {/* ========== SEÇÃO 6: MODALS ========== */}
      <section className="mb-12">
        <h2 className="text-2xl font-bold mb-4">6️⃣ Modals (Janelas)</h2>
        <div className="bg-white p-6 rounded-lg shadow space-y-4">
          
          <Button onClick={() => setShowModal(true)}>
            Abrir Modal Customizado
          </Button>

          <Button 
            variant="danger"
            onClick={() => setShowConfirm(true)}
          >
            Abrir Modal de Confirmação
          </Button>

          {/* Modal Customizado */}
          <Modal
            isOpen={showModal}
            onClose={() => setShowModal(false)}
            title="Exemplo de Modal"
            size="lg"
            footer={
              <>
                <Button variant="ghost" onClick={() => setShowModal(false)}>
                  Cancelar
                </Button>
                <Button variant="primary" onClick={() => {
                  toast.success('Modal confirmado!')
                  setShowModal(false)
                }}>
                  Confirmar
                </Button>
              </>
            }
          >
            <p className="text-gray-700">
              Este é um exemplo de modal customizado. Você pode colocar qualquer conteúdo aqui:
              formulários, textos, imagens, etc.
            </p>
            <Input 
              label="Campo de exemplo" 
              placeholder="Digite algo..."
              className="mt-4"
            />
          </Modal>

          {/* Modal de Confirmação */}
          <ConfirmModal
            isOpen={showConfirm}
            onClose={() => setShowConfirm(false)}
            onConfirm={() => {
              toast.success('Item excluído!')
              setShowConfirm(false)
            }}
            title="Confirmar Exclusão"
            message="Tem certeza que deseja excluir este item? Esta ação não pode ser desfeita."
            confirmText="Sim, excluir"
            cancelText="Cancelar"
            variant="danger"
          />
        </div>
      </section>

      {/* ========== SEÇÃO 7: CARDS ========== */}
      <section className="mb-12">
        <h2 className="text-2xl font-bold mb-4">7️⃣ Cards</h2>
        
        <div className="mb-6">
          <h3 className="text-lg font-semibold mb-3">Product Card:</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <ProductCard
              product={produtoExemplo}
              onAddToCart={(p) => toast.success(`${p.nome} adicionado!`)}
              onToggleFavorite={(p) => toast.info(`${p.nome} favoritado!`)}
            />
          </div>
        </div>

        <div>
          <h3 className="text-lg font-semibold mb-3">Order Card:</h3>
          <div className="max-w-2xl">
            <OrderCard order={pedidoExemplo} />
          </div>
        </div>
      </section>

      {/* ========== RESUMO ========== */}
      <section className="mb-12">
        <div className="bg-primary-50 border-l-4 border-primary-600 p-6 rounded">
          <h3 className="text-lg font-bold text-primary-900 mb-2">
            ✅ Todos os Componentes Testados!
          </h3>
          <p className="text-primary-800">
            Se você consegue ver esta página sem erros, todos os componentes estão funcionando corretamente!
          </p>
          <p className="text-primary-800 mt-2">
            Pressione F12 para abrir o Console e verificar se não há erros.
          </p>
        </div>
      </section>
    </div>
  )
}