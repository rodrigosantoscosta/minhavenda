import { 
  FiShoppingBag, 
  FiTag, 
  FiTruck, 
  FiPercent,
  FiCreditCard,
  FiMapPin
} from 'react-icons/fi'
import { calcularFrete } from '../../services/checkoutService'

/**
 * OrderSummary Component
 * 
 * Componente para exibir resumo do pedido no checkout
 * Mostra itens, valores, frete e informações de entrega
 */
export default function OrderSummary({ 
  items, 
  endereco, 
  pagamento,
  showAddress = true,
  showPayment = true,
  className = ''
}) {
  // Calcular valores
  const calcularValores = () => {
    const subtotal = items.reduce((total, item) => {
      return total + (item.preco * item.quantidade)
    }, 0)

    const desconto = items.reduce((total, item) => {
      if (item.precoOriginal > item.preco) {
        return total + ((item.precoOriginal - item.preco) * item.quantidade)
      }
      return total
    }, 0)

    const frete = endereco ? calcularFrete(subtotal, endereco) : 0
    const total = subtotal - desconto + frete

    return { subtotal, desconto, frete, total }
  }

  const { subtotal, desconto, frete, total } = calcularValores()

  // Formatar valores
  const formatarValor = (valor) => {
    return `R$ ${valor.toFixed(2)}`
  }

  // Contar itens totais
  const getTotalItems = () => {
    return items.reduce((total, item) => total + item.quantidade, 0)
  }

  // Verificar se tem frete grátis
  const temFreteGratis = frete === 0 && subtotal > 0
  const faltaParaFreteGratis = 200 - subtotal

  return (
    <div className={`bg-white rounded-lg shadow-sm border border-gray-200 ${className}`}>
      
      {/* Header */}
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary-50 rounded-lg">
            <FiShoppingBag className="w-5 h-5 text-primary-600" />
          </div>
          <div>
            <h3 className="font-semibold text-gray-900">
              Resumo do Pedido
            </h3>
            <p className="text-sm text-gray-600">
              {getTotalItems()} {getTotalItems() === 1 ? 'item' : 'itens'}
            </p>
          </div>
        </div>
      </div>

      <div className="p-4 space-y-4">
        
        {/* Lista de Itens */}
        <div className="space-y-3">
          {items.map((item) => (
            <div key={item.id} className="flex gap-3">
              
              {/* Imagem do Produto */}
              <div className="flex-shrink-0">
                <img
                  src={item.imagem || 'https://via.placeholder.com/60'}
                  alt={item.nome}
                  className="w-12 h-12 sm:w-14 sm:h-14 object-cover rounded-lg bg-gray-100"
                  onError={(e) => {
                    e.target.src = 'https://via.placeholder.com/60?text=Sem+Imagem'
                  }}
                />
              </div>

              {/* Informações do Item */}
              <div className="flex-1 min-w-0">
                <h4 className="text-sm font-medium text-gray-900 truncate">
                  {item.nome}
                </h4>
                <p className="text-xs text-gray-500">
                  {item.quantidade}x {formatarValor(item.preco)}
                </p>
                
                {/* Preço com desconto */}
                {item.precoOriginal > item.preco && (
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-xs text-gray-400 line-through">
                      {formatarValor(item.precoOriginal)}
                    </span>
                    <span className="text-xs bg-green-100 text-green-700 px-1.5 py-0.5 rounded">
                      -{Math.round(((item.precoOriginal - item.preco) / item.precoOriginal) * 100)}%
                    </span>
                  </div>
                )}
              </div>

              {/* Subtotal do Item */}
              <div className="flex-shrink-0 text-right">
                <p className="text-sm font-medium text-gray-900">
                  {formatarValor(item.preco * item.quantidade)}
                </p>
              </div>
            </div>
          ))}
        </div>

        {/* Divisor */}
        <div className="border-t border-gray-200" />

        {/* Valores */}
        <div className="space-y-2">
          
          {/* Subtotal */}
          <div className="flex justify-between text-sm text-gray-700">
            <span>Subtotal ({getTotalItems()} itens)</span>
            <span>{formatarValor(subtotal)}</span>
          </div>

          {/* Desconto */}
          {desconto > 0 && (
            <div className="flex justify-between text-sm text-green-600">
              <span className="flex items-center gap-1">
                <FiTag className="w-3 h-3" />
                Desconto
              </span>
              <span>- {formatarValor(desconto)}</span>
            </div>
          )}

          {/* Frete */}
          <div className="flex justify-between text-sm text-gray-700">
            <span className="flex items-center gap-1">
              <FiTruck className="w-3 h-3" />
              Frete
            </span>
            <span>
              {temFreteGratis ? (
                <span className="text-green-600 font-medium">Grátis</span>
              ) : (
                formatarValor(frete)
              )}
            </span>
          </div>

          {/* Mensagem Frete Grátis */}
          {!temFreteGratis && subtotal > 0 && subtotal < 200 && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
              <div className="flex items-center gap-2 text-sm text-blue-800">
                <FiTruck className="w-4 h-4 flex-shrink-0" />
                <span>
                  Faltam <strong>{formatarValor(faltaParaFreteGratis)}</strong> para frete grátis!
                </span>
              </div>
            </div>
          )}

          {/* Divisor */}
          <div className="border-t border-gray-200 my-2" />

          {/* Total */}
          <div className="flex justify-between text-base font-bold text-gray-900">
            <span>Total</span>
            <span className="text-primary-600 text-lg">
              {formatarValor(total)}
            </span>
          </div>
        </div>

        {/* Endereço de Entrega */}
        {showAddress && endereco && (
          <>
            <div className="border-t border-gray-200" />
            <div className="space-y-2">
              <div className="flex items-center gap-2 text-sm font-medium text-gray-900">
                <FiMapPin className="w-4 h-4" />
                Endereço de Entrega
              </div>
              <div className="text-sm text-gray-600 bg-gray-50 rounded-lg p-3">
                <p>
                  {endereco.rua}, {endereco.numero}
                  {endereco.complemento && ` - ${endereco.complemento}`}
                </p>
                <p>
                  {endereco.bairro} - {endereco.cidade}/{endereco.estado}
                </p>
                <p>CEP: {endereco.cep}</p>
              </div>
            </div>
          </>
        )}

        {/* Forma de Pagamento */}
        {showPayment && pagamento && (
          <>
            <div className="border-t border-gray-200" />
            <div className="space-y-2">
              <div className="flex items-center gap-2 text-sm font-medium text-gray-900">
                <FiCreditCard className="w-4 h-4" />
                Forma de Pagamento
              </div>
              <div className="text-sm text-gray-600 bg-gray-50 rounded-lg p-3">
                <p className="font-medium capitalize">
                  {pagamento.metodo?.replace('_', ' ') || 'Pix'}
                </p>
                {pagamento.parcelas && (
                  <p className="text-xs text-gray-500 mt-1">
                    {pagamento.parcelas}x de {formatarValor(total / pagamento.parcelas)}
                  </p>
                )}
              </div>
            </div>
          </>
        )}

        {/* Informações Adicionais */}
        <div className="border-t border-gray-200 pt-3">
          <div className="space-y-2 text-xs text-gray-600">
            <div className="flex items-start gap-2">
              <FiTruck className="w-3 h-3 text-primary-600 mt-0.5 flex-shrink-0" />
              <span>Entrega em até 7 dias úteis após aprovação do pagamento</span>
            </div>
            <div className="flex items-start gap-2">
              <FiPercent className="w-3 h-3 text-primary-600 mt-0.5 flex-shrink-0" />
              <span>Preços e condições válidos por 24 horas</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}