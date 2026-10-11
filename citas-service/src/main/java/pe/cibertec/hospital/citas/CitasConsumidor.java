
package pe.cibertec.hospital.citas;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CitasConsumidor {

    @RabbitListener(queues = RabbitMQConfig.COLA_CITAS)
    public void recibirEventoCita(String mensaje) {

        System.out.println();
        System.out.println("========================================");
        System.out.println(" EVENTO RECIBIDO DESDE RABBITMQ");
        System.out.println("========================================");
        System.out.println("Mensaje recibido: " + mensaje);
        System.out.println("========================================");
        System.out.println();
    }
}
