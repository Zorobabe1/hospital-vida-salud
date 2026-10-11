package pe.cibertec.hospital.citas;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CitasProductor {

    private final RabbitTemplate rabbitTemplate;

    public CitasProductor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarEventoCita(Citas cita) {

        String mensaje = "CITA_REGISTRADA"
                + "|id=" + cita.getId()
                + "|pacienteId=" + cita.getPacienteId()
                + "|fechaHora=" + cita.getFechaHora()
                + "|estado=" + cita.getEstado();

        // Enviar el evento solamente a cola.citas
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.COLA_CITAS,
                mensaje
        );

        System.out.println("Evento enviado a RabbitMQ: " + mensaje);
    }
}
