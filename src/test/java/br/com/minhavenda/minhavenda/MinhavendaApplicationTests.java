package br.com.minhavenda.minhavenda;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifica que o ApplicationContext carrega corretamente.
 * RabbitMQ é mockado para não precisar de broker real nos testes.
 */
@SpringBootTest
@ActiveProfiles("test")
class MinhavendaApplicationTests {

    @MockBean
    ConnectionFactory connectionFactory;

    @Test
    void contextLoads() {
        // Se o contexto carregar sem exceção, o teste passa.
    }
}
