package br.com.minhavenda.minhavenda.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Email de confirmação de pedido criado
     */
    public void enviarEmailPedidoCriado(String destinatario, String nomeUsuario,
                                        UUID pedidoId, Double valorTotal,
                                        Integer quantidadeItens) {
        String assunto = " Pedido Confirmado - MinhaVenda";

        String corpo = String.format("""
            Olá %s!
            
            Seu pedido foi criado com sucesso! 🎉
            
             Pedido: #%s
             Valor Total: R$ %.2f
             Itens: %d produto(s)
            
            Você receberá atualizações sobre o status do seu pedido.
            
            Obrigado por comprar na MinhaVenda!
            
            ---
            Equipe MinhaVenda
            """,
                nomeUsuario,
                pedidoId.toString().substring(0, 8),
                valorTotal,
                quantidadeItens
        );

        enviarEmail(destinatario, assunto, corpo);
    }

    /**
     * Email de pagamento confirmado
     */
    public void enviarEmailPagamentoConfirmado(String destinatario, UUID pedidoId,
                                               Double valorPago, String metodoPagamento) {
        String assunto = "Pagamento Confirmado - MinhaVenda";

        String corpo = String.format("""
            Pagamento confirmado!
            
            Seu pagamento foi processado com sucesso.
            
            Pedido: #%s
            Valor Pago: R$ %.2f
            Método: %s
            
            Seu pedido está sendo separado para envio.
            
            ---
            Equipe MinhaVenda
            """,
                pedidoId.toString().substring(0, 8),
                valorPago,
                metodoPagamento
        );

        enviarEmail(destinatario, assunto, corpo);
    }

    /**
     * Email de pedido enviado com código de rastreio
     */
    public void enviarEmailPedidoEnviado(String destinatario, UUID pedidoId,
                                         String codigoRastreio, String transportadora) {
        String assunto = "Pedido Enviado - MinhaVenda";

        String corpo = String.format("""
            Seu pedido foi enviado!
            
            Pedido: #%s
            Código de Rastreio: %s
            Transportadora: %s
            
            Acompanhe seu pedido através do código de rastreio acima.
            
            Previsão de entrega: 5-7 dias úteis
            
            ---
            Equipe MinhaVenda
            """,
                pedidoId.toString().substring(0, 8),
                codigoRastreio,
                transportadora
        );

        enviarEmail(destinatario, assunto, corpo);
    }

    /**
     * Email de pedido cancelado
     */
    public void enviarEmailPedidoCancelado(String destinatario, UUID pedidoId,
                                           String motivoCancelamento) {
        String assunto = "Pedido Cancelado - MinhaVenda";

        String corpo = String.format("""
            Seu pedido foi cancelado.
            
            Pedido: #%s
            Motivo: %s
            
            Se você não solicitou este cancelamento, entre em contato conosco.
            
            ---
            Equipe MinhaVenda
            """,
                pedidoId.toString().substring(0, 8),
                motivoCancelamento != null ? motivoCancelamento : "Não informado"
        );

        enviarEmail(destinatario, assunto, corpo);
    }

    /**
     * Método privado para envio real do email
     */
    private void enviarEmail(String destinatario, String assunto, String corpo) {
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setTo(destinatario);
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);
            mensagem.setFrom("noreply@minhavenda.com");

            mailSender.send(mensagem);

            log.info("📧 Email enviado: {} → {}", assunto, destinatario);

        } catch (Exception e) {
            log.error("Erro ao enviar email para: {}", destinatario, e);
            // Não lançar exceção - falha no email não deve quebrar o fluxo
        }
    }
}
