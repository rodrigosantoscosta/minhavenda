package br.com.minhavenda.minhavenda.infrastructure.notification;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
//    private final SpringTemplateEngine templateEngine; // Opcional - para templates HTML

    @Value("${spring.mail.username}")
    private String remetente;

    @Value("${app.mail.from-name:MinhaVenda}")
    private String nomeRemetente;

    @Override
    public void enviarEmailPedidoCriado(String destinatario, String nomeUsuario,
                                        UUID pedidoId, Double valorTotal,
                                        Integer quantidadeItens) {
        log.info("Enviando email de pedido criado para: {}", destinatario);

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

            log.info("✅ Email de pedido criado enviado para: {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email de pedido criado para: {}", destinatario, e);
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    @Override
    public void enviarEmailPedidoPago(String destinatario, String nomeUsuario,
                                      UUID pedidoId, Double valorPago,
                                      String metodoPagamento) {
        log.info("Enviando email de pedido pago para: {}", destinatario);

        try {
            String assunto = "💳 Pagamento confirmado - Pedido #" + pedidoId.toString().substring(0, 8);

            String corpo = String.format("""
                Olá %s,

                Seu pagamento foi confirmado!

                📦 Número do Pedido: %s
                💰 Valor Pago: R$ %.2f
                💳 Método: %s

                Seu pedido está sendo preparado para envio.
                Você receberá um email com o código de rastreio em breve.

                Obrigado!

                Atenciosamente,
                Equipe MinhaVenda
                """,
                    nomeUsuario,
                    pedidoId.toString().substring(0, 8),
                    valorPago,
                    metodoPagamento
            );

            enviarEmailSimples(destinatario, assunto, corpo);

            log.info("✅ Email de pedido pago enviado para: {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email de pedido pago para: {}", destinatario, e);
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    @Override
    public void enviarEmailPedidoEnviado(String destinatario, String nomeUsuario,
                                         UUID pedidoId, String codigoRastreio,
                                         String transportadora) {
        log.info("Enviando email de pedido enviado para: {}", destinatario);

        try {
            String assunto = "🚚 Pedido enviado - #" + pedidoId.toString().substring(0, 8);

            String corpo = String.format("""
                Olá %s,

                Seu pedido foi enviado!

                📦 Número do Pedido: %s
                🚚 Transportadora: %s
                📍 Código de Rastreio: %s

                Você pode acompanhar sua entrega através do código de rastreio.

                Obrigado!

                Atenciosamente,
                Equipe MinhaVenda
                """,
                    nomeUsuario,
                    pedidoId.toString().substring(0, 8),
                    transportadora,
                    codigoRastreio
            );

            enviarEmailSimples(destinatario, assunto, corpo);

            log.info("✅ Email de pedido enviado para: {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email de pedido enviado para: {}", destinatario, e);
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    @Override
    public void enviarEmailPedidoCancelado(String destinatario, String nomeUsuario,
                                           UUID pedidoId, String motivo) {
        log.info("Enviando email de pedido cancelado para: {}", destinatario);

        try {
            String assunto = "❌ Pedido cancelado - #" + pedidoId.toString().substring(0, 8);

            String corpo = String.format("""
                Olá %s,

                Seu pedido foi cancelado.

                📦 Número do Pedido: %s
                📝 Motivo: %s

                Se você tiver alguma dúvida, entre em contato conosco.

                Atenciosamente,
                Equipe MinhaVenda
                """,
                    nomeUsuario,
                    pedidoId.toString().substring(0, 8),
                    motivo
            );

            enviarEmailSimples(destinatario, assunto, corpo);

            log.info("✅ Email de pedido cancelado enviado para: {}", destinatario);

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email de pedido cancelado para: {}", destinatario, e);
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

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
    }

    /**
     * Envia email HTML (opcional - usando Thymeleaf).
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
//    }

}