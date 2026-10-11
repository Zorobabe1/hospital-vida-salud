package pe.cibertec.hospital.citas;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CitasControllerTest {

    private CitasController crearControlador() {
        return new CitasController(
                null,
                WebClient.builder(),
                null
        );
    }

    @Test
    void debeRechazarCitaSinPaciente() {

        Citas cita = new Citas();
        cita.setPacienteId(null);
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setMotivo("Consulta de prueba");

        StepVerifier.create(crearControlador().registrarCita(cita))
                .expectErrorSatisfies(error -> {
                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.BAD_REQUEST,
                            excepcion.getStatusCode()
                    );
                })
                .verify();
    }

    @Test
    void debeRechazarCitaSinMotivo() {

        Citas cita = new Citas();
        cita.setPacienteId(1L);
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setMotivo("");

        StepVerifier.create(crearControlador().registrarCita(cita))
                .expectErrorSatisfies(error -> {
                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.BAD_REQUEST,
                            excepcion.getStatusCode()
                    );
                })
                .verify();
    }

    @Test
    void debeRechazarFechaEnElPasado() {

        Citas cita = new Citas();
        cita.setPacienteId(1L);
        cita.setFechaHora(LocalDateTime.now().minusDays(1));
        cita.setMotivo("Consulta de prueba");
        cita.setEstado("PROGRAMADA");

        StepVerifier.create(crearControlador().registrarCita(cita))
                .expectErrorSatisfies(error -> {
                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.BAD_REQUEST,
                            excepcion.getStatusCode()
                    );
                })
                .verify();
    }

    @Test
    void debeRechazarEstadoNoValido() {

        Citas cita = new Citas();
        cita.setPacienteId(1L);
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setMotivo("Consulta de prueba");
        cita.setEstado("PENDIENTE_DESCONOCIDO");

        StepVerifier.create(crearControlador().registrarCita(cita))
                .expectErrorSatisfies(error -> {
                    ResponseStatusException excepcion =
                            (ResponseStatusException) error;

                    assertEquals(
                            HttpStatus.BAD_REQUEST,
                            excepcion.getStatusCode()
                    );
                })
                .verify();
    }
}
