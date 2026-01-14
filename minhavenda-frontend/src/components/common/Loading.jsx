import { FiLoader } from 'react-icons/fi'

/**
 * Spinner Component
 * 
 * @param {string} size - sm | md | lg | xl
 * @param {string} variant - primary | white | current
 */
export function Spinner({ size = 'md', variant = 'primary' }) {
  const sizeStyles = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
    xl: 'w-16 h-16',
  }

  const colorStyles = {
    primary: 'text-primary-600',
    white: 'text-white',
    current: 'text-current',
  }

  return (
    <FiLoader
      className={`${sizeStyles[size]} ${colorStyles[variant]} animate-spin`}
    />
  )
}

/**
 * Loading Overlay
 * 
 * Overlay de loading para tela inteira
 */
export function LoadingOverlay({ message = 'Carregando...' }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-lg p-8 flex flex-col items-center space-y-4">
        <Spinner size="xl" />
        <p className="text-gray-700 font-medium">{message}</p>
      </div>
    </div>
  )
}

/**
 * Loading Container
 * 
 * Container de loading para seções
 */
export function LoadingContainer({ 
  loading, 
  children, 
  message = 'Carregando...',
  minHeight = '200px' 
}) {
  if (loading) {
    return (
      <div 
        className="flex flex-col items-center justify-center"
        style={{ minHeight }}
      >
        <Spinner size="lg" />
        <p className="mt-4 text-gray-600">{message}</p>
      </div>
    )
  }

  return children
}

/**
 * Skeleton Component
 * 
 * Placeholder animado enquanto carrega
 */
export function Skeleton({ className = '', variant = 'default' }) {
  const baseStyles = 'animate-pulse bg-gray-200 rounded'
  
  const variantStyles = {
    default: '',
    circle: 'rounded-full',
    text: 'h-4',
    title: 'h-8',
  }

  return (
    <div className={`${baseStyles} ${variantStyles[variant]} ${className}`} />
  )
}

/**
 * ProductCardSkeleton
 * 
 * Skeleton para card de produto
 */
export function ProductCardSkeleton() {
  return (
    <div className="bg-white rounded-lg shadow-sm p-4 border border-gray-100">
      {/* Image */}
      <Skeleton className="w-full aspect-square mb-4" />
      
      {/* Category */}
      <Skeleton variant="text" className="w-20 mb-2" />
      
      {/* Title */}
      <Skeleton variant="title" className="w-full mb-2" />
      <Skeleton variant="text" className="w-3/4 mb-4" />
      
      {/* Price */}
      <Skeleton className="w-32 h-8 mb-4" />
      
      {/* Button */}
      <Skeleton className="w-full h-10" />
    </div>
  )
}

/**
 * OrderCardSkeleton
 * 
 * Skeleton para card de pedido
 */
export function OrderCardSkeleton() {
  return (
    <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-200">
      <div className="flex items-center justify-between mb-4 pb-4 border-b">
        <div className="flex items-center space-x-3">
          <Skeleton variant="circle" className="w-12 h-12" />
          <div>
            <Skeleton className="w-32 h-6 mb-2" />
            <Skeleton variant="text" className="w-24" />
          </div>
        </div>
        <Skeleton className="w-20 h-6" />
      </div>

      <Skeleton variant="text" className="w-40 mb-2" />
      <div className="flex -space-x-2 mb-4">
        <Skeleton variant="circle" className="w-12 h-12" />
        <Skeleton variant="circle" className="w-12 h-12" />
        <Skeleton variant="circle" className="w-12 h-12" />
      </div>

      <div className="flex justify-between items-center pt-4 border-t">
        <div>
          <Skeleton variant="text" className="w-16 mb-1" />
          <Skeleton className="w-24 h-8" />
        </div>
        <Skeleton variant="text" className="w-28" />
      </div>
    </div>
  )
}

/**
 * TableSkeleton
 * 
 * Skeleton para tabelas
 */
export function TableSkeleton({ rows = 5, cols = 4 }) {
  return (
    <div className="space-y-3">
      {/* Header */}
      <div className="flex space-x-4">
        {Array.from({ length: cols }).map((_, i) => (
          <Skeleton key={i} className="flex-1 h-10" />
        ))}
      </div>

      {/* Rows */}
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex space-x-4">
          {Array.from({ length: cols }).map((_, j) => (
            <Skeleton key={j} variant="text" className="flex-1" />
          ))}
        </div>
      ))}
    </div>
  )
}

/**
 * Exemplo de uso:
 * 
 * // Spinner simples
 * <Spinner />
 * 
 * // Loading overlay
 * <LoadingOverlay message="Processando pagamento..." />
 * 
 * // Loading container
 * <LoadingContainer loading={isLoading}>
 *   <ProductList />
 * </LoadingContainer>
 * 
 * // Skeleton
 * {loading ? (
 *   <ProductCardSkeleton />
 * ) : (
 *   <ProductCard product={product} />
 * )}
 */
