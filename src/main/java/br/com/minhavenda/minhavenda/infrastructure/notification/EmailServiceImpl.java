package br.com.minhavenda.minhavenda.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
// @Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
//    private final SpringTemplateEngine templateEngine; // Opcional - para templates HTML

    @Value("${app.mail.from:noreply@minhavenda.com.br}")
    private String remetente;

    @Value("${app.mail.from-name:MinhaVenda}")
    private String nomeRemetente;

    // ========================================================================
    // PEDIDO CRIADO
    // ========================================================================

    @Override
    public void enviarEmailPedidoCriado(String destinatario, String nomeUsuario,
                                        UUID pedidoId, Double valorTotal,
                                        Integer quantidadeItens) {
        log.info("📧 Enviando email de pedido criado para: {}", destinatario);

        try {
            String assunto = "✅ Pedido #" + pedidoId.toString().substring(0, 8) + " criado com sucesso!";

            String corpo = String.format("""
                Olá %s,
                
                Seu pedido foi criado com sucesso!
                
                📦 Número do Pedido: %s
                💰 Valor Total: R$ %.2f
                📊 Quantidade de Itens: %d
                
                Aguardamos a confirmação do pagamento para processar seu pedido.
                
                Obrigado por comprar conosco!
                
                Atenciosamente,
                Equipe MinhaVenda
                """,
                    nomeUsuario,
                    pedidoId.toString().substring(0, 8),
                    valorTotal,
                    quantidadeItens
            );

            enviarEmailSimples(destinatario, assunto, corpo);

            log.info("✅ Email de pedido criado enviado com sucesso para: {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email de pedido criado para: {}", destinatario, e);
            throw new RuntimeException("Erro ao enviar email de pedido criado", e);
        }
    }

    // ========================================================================
    // PEDIDO PAGO
    // ========================================================================

    @Override
    public void enviarEmailPedidoPago(String destinatario, UUID pedidoId,
                                      Double valorPago, String metodoPagamento) {
        log.info("📧 Enviando email de pedido pago para: {}", destinatario);

        try {
            String assunto = "💳 Pagamento confirmado - Pedido #" + pedidoId.toString().substring(0, 8);

            String corpo = String.format("""
                Olá,
                
                Seu pagamento foi confirmado!
                
                📦 Número do Pedido: %s
                💰 Valor Pago: R$ %.2f
                💳 Método de Pagamento: %s
                
                Seu pedido está sendo preparado para envio.
                Você receberá um email com o código de rastreio em breve.
                
                Obrigado pela sua compra!
                
                Atenciosamente,
                Equipe MinhaVenda
                """,
                    pedidoId.toString().substring(0, 8),
                    valorPago,
                    metodoPagamento
            );

            enviarEmailSimples(destinatario, assunto, corpo);

            log.info("✅ Email de pedido pago enviado com sucesso para: {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email de pedido pago para: {}", destinatario, e);
            throw new RuntimeException("Erro ao enviar email de pedido pago", e);
        }
    }

    // ========================================================================
    // PEDIDO ENVIADO
    // ========================================================================

    @Override
    public void enviarEmailPedidoEnviado(String destinatario, String nomeUsuario,
                                         UUID pedidoId, String codigoRastreio,
                                         String transportadora, String telefone) {
        log.info("📧 Enviando email de pedido enviado para: {}", destinatario);

        try {
            String assunto = "🚚 Pedido enviado - #" + pedidoId.toString().substring(0, 8);

            String telefoneInfo = (telefone != null && !telefone.equals("Não informado"))
                    ? "\n📱 Telefone de Contato: " + telefone
                    : "";

            String corpo = String.format("""
                Olá %s,
                
                Seu pedido foi enviado!
                
                📦 Número do Pedido: %s
                🚚 Transportadora: %s
                📍 Código de Rastreio: %s%s
                
                Você pode acompanhar sua entrega através do código de rastreio acima.
                Em breve seu pedido chegará ao destino!
                
                Obrigado pela preferência!
                
                Atenciosamente,
                Equipe MinhaVenda
                """,
                    nomeUsuario,
                    pedidoId.toString().substring(0, 8),
                    transportadora,
                    codigoRastreio,
                    telefoneInfo
            );

            enviarEmailSimples(destinatario, assunto, corpo);

            log.info("✅ Email de pedido enviado com sucesso para: {} - Rastreio: {}",
                    destinatario, codigoRastreio);

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email de pedido enviado para: {}", destinatario, e);
            throw new RuntimeException("Erro ao enviar email de pedido enviado", e);
        }
    }

    // ========================================================================
    // PEDIDO CANCELADO
    // ========================================================================

    @Override
    public void enviarEmailPedidoCancelado(String destinatario, UUID pedidoId, String motivo) {
        log.info("📧 Enviando email de pedido cancelado para: {}", destinatario);

        try {
            String assunto = "❌ Pedido cancelado - #" + pedidoId.toString().substring(0, 8);

            String corpo = String.format("""
                Olá,
                
                Informamos que seu pedido foi cancelado.
                
                📦 Número do Pedido: %s
                📝 Motivo do Cancelamento: %s
                
                Se você tiver alguma dúvida ou não solicitou este cancelamento,
                entre em contato conosco imediatamente.
                
                Esperamos atendê-lo novamente em breve!
                
                Atenciosamente,
                Equipe MinhaVenda
                """,
                    pedidoId.toString().substring(0, 8),
                    motivo
            );

            enviarEmailSimples(destinatario, assunto, corpo);

            log.info("✅ Email de pedido cancelado enviado com sucesso para: {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email de pedido cancelado para: {}", destinatario, e);
            throw new RuntimeException("Erro ao enviar email de pedido cancelado", e);
        }
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    /**
     * Envia email de texto simples.
     */
    private void enviarEmailSimples(String destinatario, String assunto, String corpo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(String.format("%s <%s>", nomeRemetente, remetente));
        message.setTo(destinatario);
        message.setSubject(assunto);
        message.setText(corpo);

        mailSender.send(message);

        log.debug("📨 Email enviado - De: {} Para: {} Assunto: {}",
                remetente, destinatario, assunto);
    }

    /**
     * Envia email HTML (opcional - usando Thymeleaf).
     *
     * Para usar templates HTML, adicione estas dependências:
     * - spring-boot-starter-thymeleaf
     * - thymeleaf-spring6
     *
     * Exemplo de uso:
     * Context context = new Context();
     * context.setVariable("nomeUsuario", "João");
     * context.setVariable("pedidoId", pedidoId);
     * enviarEmailHtml(destinatario, assunto, "email-pedido-criado", context);
     */
//    private void enviarEmailHtml(String destinatario, String assunto,
//                                 String template, Context context) throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//        helper.setFrom(String.format("%s <%s>", nomeRemetente, remetente));
//        helper.setTo(destinatario);
//        helper.setSubject(assunto);
//
//        String htmlContent = templateEngine.process(template, context);
//        helper.setText(htmlContent, true);
//
//        mailSender.send(message);
//
//        log.debug("📨 Email HTML enviado - Template: {} Para: {}", template, destinatario);
//    }
}
