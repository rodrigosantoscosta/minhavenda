/**
 * Badge Component
 * 
 * @param {string} variant - primary | secondary | success | danger | warning | info
 * @param {string} size - sm | md | lg
 * @param {ReactNode} leftIcon - Ícone à esquerda
 * @param {ReactNode} rightIcon - Ícone à direita
 * @param {boolean} dot - Mostrar ponto de status
 * @param {ReactNode} children - Texto do badge
 */
export default function Badge({
  variant = 'primary',
  size = 'md',
  leftIcon,
  rightIcon,
  dot = false,
  children,
  className = '',
}) {
  // Base styles
  const baseStyles = 'inline-flex items-center font-medium rounded-full'

  // Size variants
  const sizeStyles = {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-3 py-1 text-sm',
    lg: 'px-4 py-1.5 text-base',
  }

  // Color variants
  const variantStyles = {
    primary: 'bg-primary-100 text-primary-800',
    secondary: 'bg-gray-100 text-gray-800',
    success: 'bg-green-100 text-green-800',
    danger: 'bg-red-100 text-red-800',
    warning: 'bg-yellow-100 text-yellow-800',
    info: 'bg-blue-100 text-blue-800',
  }

  // Dot color variants
  const dotVariants = {
    primary: 'bg-primary-600',
    secondary: 'bg-gray-600',
    success: 'bg-green-600',
    danger: 'bg-red-600',
    warning: 'bg-yellow-600',
    info: 'bg-blue-600',
  }

  const classes = `
    ${baseStyles}
    ${sizeStyles[size]}
    ${variantStyles[variant]}
    ${className}
  `.trim().replace(/\s+/g, ' ')

  return (
    <span className={classes}>
      {/* Dot */}
      {dot && (
        <span className={`w-2 h-2 rounded-full ${dotVariants[variant]} mr-2`} />
      )}

      {/* Left Icon */}
      {leftIcon && (
        <span className="mr-1.5">{leftIcon}</span>
      )}

      {/* Content */}
      <span>{children}</span>

      {/* Right Icon */}
      {rightIcon && (
        <span className="ml-1.5">{rightIcon}</span>
      )}
    </span>
  )
}

/**
 * Status Badge - Badge pré-configurado para status de pedidos
 */
export function StatusBadge({ status }) {
  const config = {
    PENDENTE: {
      variant: 'warning',
      label: 'Pendente',
      dot: true,
    },
    PAGO: {
      variant: 'info',
      label: 'Pago',
      dot: true,
    },
    ENVIADO: {
      variant: 'info',
      label: 'Enviado',
      dot: true,
    },
    ENTREGUE: {
      variant: 'success',
      label: 'Entregue',
      dot: true,
    },
    CANCELADO: {
      variant: 'danger',
      label: 'Cancelado',
      dot: true,
    },
  }

  const { variant, label, dot } = config[status] || {
    variant: 'secondary',
    label: status,
    dot: false,
  }

  return (
    <Badge variant={variant} dot={dot}>
      {label}
    </Badge>
  )
}

/**
 * Stock Badge - Badge para status de estoque
 */
export function StockBadge({ quantity }) {
  if (quantity === 0) {
    return <Badge variant="danger">Esgotado</Badge>
  }

  if (quantity < 10) {
    return (
      <Badge variant="warning">
        Apenas {quantity} {quantity === 1 ? 'unidade' : 'unidades'}
      </Badge>
    )
  }

  return <Badge variant="success">Em estoque</Badge>
}

/**
 * Discount Badge - Badge para desconto
 */
export function DiscountBadge({ percentage }) {
  return (
    <Badge variant="danger" size="sm">
      -{percentage}%
    </Badge>
  )
}

/**
 * Exemplo de uso:
 * 
 * // Badge simples
 * <Badge variant="primary">Novo</Badge>
 * 
 * // Badge com ícone
 * <Badge variant="success" leftIcon={<CheckIcon />}>
 *   Verificado
 * </Badge>
 * 
 * // Badge com dot
 * <Badge variant="warning" dot>
 *   Pendente
 * </Badge>
 * 
 * // Status Badge
 * <StatusBadge status="ENVIADO" />
 * 
 * // Stock Badge
 * <StockBadge quantity={5} />
 * 
 * // Discount Badge
 * <DiscountBadge percentage={25} />
 */
