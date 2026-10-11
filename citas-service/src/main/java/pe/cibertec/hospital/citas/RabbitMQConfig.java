
package pe.cibertec.hospital.citas;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String COLA_CITAS = "cola.citas";

    @Bean
    public Queue colaCitas() {
        return new Queue(COLA_CITAS, true);
    }
}
