import { useState, useEffect } from 'react'
import Input from '../common/Input'
import { validarEndereco } from '../../services/checkoutService'
import { FiMapPin, FiSearch } from 'react-icons/fi'

/**
 * AddressForm Component
 * 
 * Formulário de endereço com validação em tempo real
 * e busca de CEP integrada
 */
export default function AddressForm({ 
  onAddressChange, 
  initialData = {}, 
  errors: externalErrors = {},
  showTitle = true 
}) {
  const [formData, setFormData] = useState({
    cep: '',
    rua: '',
    numero: '',
    complemento: '',
    bairro: '',
    cidade: '',
    estado: '',
    ...initialData
  })

  const [errors, setErrors] = useState({})
  const [isSearchingCep, setIsSearchingCep] = useState(false)
  const [cepNotFound, setCepNotFound] = useState(false)
  const [isTouched, setIsTouched] = useState(false)

  // Atualizar dados iniciais quando mudar
  useEffect(() => {
    if (Object.keys(initialData).length > 0) {
      setFormData(prev => ({ ...prev, ...initialData }))
    }
  }, [initialData])

  // Validar em tempo real (após usuário interagir)
  useEffect(() => {
    if (isTouched) {
      const validation = validarEndereco(formData)
      setErrors(validation.errors)
      
      // Notificar componente pai sobre mudanças e validação
      if (onAddressChange) {
        onAddressChange({
          address: formData,
          isValid: validation.isValid,
          errors: validation.errors
        })
      }
    } else {
      // Mesmo sem touched, notificar pai sobre estado inicial
      if (onAddressChange) {
        const validation = validarEndereco(formData)
        onAddressChange({
          address: formData,
          isValid: validation.isValid,
          errors: {}
        })
      }
    }
  }, [formData, onAddressChange, isTouched])

  // Auto-buscar CEP quando estiver completo
  useEffect(() => {
    const cep = formData.cep.replace(/\D/g, '')
    if (cep.length === 8 && !isSearchingCep && !cepNotFound) {
      handleCepSearch()
    }
  }, [formData.cep])

  // Lidar com mudanças nos campos
  const handleInputChange = (field, value) => {
    // Marcar formulário como "tocado" na primeira interação
    if (!isTouched) {
      setIsTouched(true)
    }

    setFormData(prev => ({
      ...prev,
      [field]: value
    }))

    // Limpar mensagens de erro do campo quando usuário digitar
    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: ''
      }))
    }

    // Limpar mensagem de CEP não encontrado quando usuário digitar
    if (field === 'cep' && cepNotFound) {
      setCepNotFound(false)
    }

    // Se o CEP foi alterado, limpar campos de endereço para nova busca
    if (field === 'cep') {
      const cepNumbers = value.replace(/\D/g, '')
      if (cepNumbers.length < 8) {
        setFormData(prev => ({
          ...prev,
          rua: '',
          bairro: '',
          cidade: '',
          estado: '',
          complemento: ''
        }))
      }
    }
  }

  // Buscar CEP
  const handleCepSearch = async () => {
    const cep = formData.cep.replace(/\D/g, '')
    
    if (cep.length !== 8) {
      setErrors(prev => ({
        ...prev,
        cep: 'CEP deve ter 8 dígitos'
      }))
      return
    }

    setIsSearchingCep(true)
    setCepNotFound(false)

    try {
      // Usar API ViaCEP (gratuita)
      const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`)
      const data = await response.json()

      if (data.erro) {
        setCepNotFound(true)
        setErrors(prev => ({
          ...prev,
          cep: 'CEP não encontrado'
        }))
      } else {
        // Preencher campos com dados do CEP
        setFormData(prev => ({
          ...prev,
          rua: data.logradouro || '',
          bairro: data.bairro || '',
          cidade: data.localidade || '',
          estado: data.uf || '',
          complemento: data.complemento || ''
        }))

        // Limpar erros relacionados
        setErrors(prev => ({
          ...prev,
          cep: '',
          rua: '',
          bairro: '',
          cidade: '',
          estado: ''
        }))
      }
    } catch (error) {
      console.error('Erro ao buscar CEP:', error)
      setErrors(prev => ({
        ...prev,
        cep: 'Erro ao buscar CEP. Tente novamente.'
      }))
    } finally {
      setIsSearchingCep(false)
    }
  }

  // Formatar CEP enquanto digita
  const handleCepChange = (value) => {
    // Marcar formulário como "tocado" na primeira interação
    if (!isTouched) {
      setIsTouched(true)
    }

    // Remove caracteres não numéricos
    const cepNumbers = value.replace(/\D/g, '')
    
    // Formata como 00000-000
    let formattedCep = ''
    if (cepNumbers.length > 5) {
      formattedCep = `${cepNumbers.slice(0, 5)}-${cepNumbers.slice(5, 8)}`
    } else {
      formattedCep = cepNumbers
    }
    
    handleInputChange('cep', formattedCep)
  }

  // Formatar campos
  const handleNumeroChange = (value) => {
    // Permitir apenas números e caracteres comuns
    const cleaned = value.replace(/[^a-zA-Z0-9\s-]/g, '')
    handleInputChange('numero', cleaned)
  }

  const handleEstadoChange = (value) => {
    // Converter para maiúsculas e limitar a 2 caracteres
    const cleaned = value.toUpperCase().slice(0, 2)
    handleInputChange('estado', cleaned)
  }

  // Combinar erros internos com erros externos (apenas se formulário foi tocado)
  const combinedErrors = isTouched ? { ...errors, ...externalErrors } : {}

  return (
    <div className="space-y-6">
      {showTitle && (
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 bg-primary-50 rounded-lg">
            <FiMapPin className="w-5 h-5 text-primary-600" />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              Endereço de Entrega
            </h3>
            <p className="text-sm text-gray-600">
              Informe onde deseja receber seu pedido
            </p>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        
        {/* CEP */}
        <div className="md:col-span-2">
           <Input
            label="CEP"
            placeholder="00000-000"
            value={formData.cep}
            onChange={(e) => handleCepChange(e.target.value)}
            onBlur={handleCepSearch}
            error={combinedErrors.cep}
            required
            maxLength={9}
            autoComplete="postal-code"
            name="postal_code"
            rightElement={
              <button
                type="button"
                onClick={handleCepSearch}
                disabled={isSearchingCep || formData.cep.length !== 9}
                className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                title="Buscar CEP"
              >
                {isSearchingCep ? (
                  <div className="w-4 h-4 border-2 border-primary-600 border-t-transparent rounded-full animate-spin" />
                ) : (
                  <FiSearch className="w-4 h-4" />
                )}
              </button>
            }
          />
          {cepNotFound && (
            <p className="mt-1 text-sm text-amber-600">
              CEP não encontrado. Preencha o endereço manualmente.
            </p>
          )}
        </div>

        {/* Rua */}
        <div className="md:col-span-2">
          <Input
            label="Rua"
            placeholder="Nome da rua"
            value={formData.rua}
            onChange={(e) => handleInputChange('rua', e.target.value)}
            error={combinedErrors.rua}
            required
            disabled={isSearchingCep}
            autoComplete="address-line1"
            name="street_address"
          />
        </div>

        {/* Número */}
        <div>
          <Input
            label="Número"
            placeholder="123"
            value={formData.numero}
            onChange={(e) => handleNumeroChange(e.target.value)}
            error={combinedErrors.numero}
            required
            disabled={isSearchingCep}
            autoComplete="address-line2"
            name="street_number"
          />
        </div>

        {/* Complemento */}
        <div>
          <Input
            label="Complemento"
            placeholder="Apto 101, Bloco A (opcional)"
            value={formData.complemento}
            onChange={(e) => handleInputChange('complemento', e.target.value)}
            disabled={isSearchingCep}
            autoComplete="address-line3"
            name="address_complement"
          />
        </div>

        {/* Bairro */}
        <div>
          <Input
            label="Bairro"
            placeholder="Nome do bairro"
            value={formData.bairro}
            onChange={(e) => handleInputChange('bairro', e.target.value)}
            error={combinedErrors.bairro}
            required
            disabled={isSearchingCep}
            autoComplete="address-level4"
            name="neighborhood"
          />
        </div>

        {/* Cidade */}
        <div>
          <Input
            label="Cidade"
            placeholder="Nome da cidade"
            value={formData.cidade}
            onChange={(e) => handleInputChange('cidade', e.target.value)}
            error={combinedErrors.cidade}
            required
            disabled={isSearchingCep}
            autoComplete="address-level2"
            name="city"
          />
        </div>

        {/* Estado */}
        <div>
          <Input
            label="Estado"
            placeholder="SP"
            value={formData.estado}
            onChange={(e) => handleEstadoChange(e.target.value)}
            error={combinedErrors.estado}
            required
            maxLength={2}
            disabled={isSearchingCep}
            autoComplete="address-level1"
            name="state"
          />
        </div>
      </div>

      {/* Informações de ajuda */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start gap-3">
          <FiMapPin className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
          <div className="text-sm text-blue-800">
            <p className="font-medium mb-1">Dica de preenchimento:</p>
            <ul className="space-y-1 text-blue-700">
              <li>• Digite o CEP e clique na 🔍 para auto-preencher</li>
              <li>• Verifique se todos os campos estão corretos</li>
              <li>• Use o complemento para apartamentos ou casas</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}