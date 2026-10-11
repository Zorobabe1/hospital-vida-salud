package pe.cibertec.hospital.citas;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/citas")
public class CitasController {

    private final CitasRepository citasRepository;
    private final WebClient pacientesWebClient;
    private final CitasProductor citasProductor;

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "PROGRAMADA",
            "CONFIRMADA",
            "ATENDIDA",
            "CANCELADA"
    );

    public CitasController(
            CitasRepository citasRepository,
            WebClient.Builder webClientBuilder,
            CitasProductor citasProductor) {

        this.citasRepository = citasRepository;
        this.citasProductor = citasProductor;

        this.pacientesWebClient = webClientBuilder
                .baseUrl("http://localhost:8081")
                .build();
    }

    @GetMapping
    public Flux<Citas> listarCitas() {
        return citasRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Citas> buscarPorId(@PathVariable Long id) {
        return citasRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cita no encontrada"
                        )
                ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Citas> registrarCita(@RequestBody Citas cita) {

        if (cita.getPacienteId() == null
                || cita.getPacienteId() <= 0
                || cita.getFechaHora() == null
                || cita.getMotivo() == null
                || cita.getMotivo().isBlank()) {

            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Paciente, fecha y hora, y motivo son obligatorios"
                    )
            );
        }

        if (cita.getFechaHora().isBefore(LocalDateTime.now())) {
            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "La fecha de la cita no puede estar en el pasado"
                    )
            );
        }

        if (cita.getEstado() == null || cita.getEstado().isBlank()) {
            cita.setEstado("PROGRAMADA");
        } else {
            cita.setEstado(cita.getEstado().trim().toUpperCase());
        }

        if (!ESTADOS_VALIDOS.contains(cita.getEstado())) {
            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Estado de cita no válido"
                    )
            );
        }

        return verificarPaciente(cita.getPacienteId())
                .then(Mono.defer(() -> {
                    cita.setId(null);
                    cita.setFechaCreacion(LocalDateTime.now());
                    return citasRepository.save(cita);
                }))
                .flatMap(citaGuardada ->
                        Mono.fromCallable(() -> {
                            citasProductor.enviarEventoCita(citaGuardada);
                            return citaGuardada;
                        })
                );
    }

    private Mono<Void> verificarPaciente(Long pacienteId) {

        return pacientesWebClient.get()
                .uri("/api/pacientes/{id}", pacienteId)
                .exchangeToMono(response -> {

                    if (response.statusCode().is2xxSuccessful()) {
                        return response.releaseBody();
                    }

                    if (response.statusCode().value() == 404) {
                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "No existe el paciente con ID " + pacienteId
                                )
                        );
                    }

                    return response.releaseBody()
                            .then(Mono.error(
                                    new ResponseStatusException(
                                            HttpStatus.BAD_GATEWAY,
                                            "El servicio de pacientes respondió con "
                                                    + response.statusCode().value()
                                    )
                            ));
                })
                .onErrorMap(
                        org.springframework.web.reactive.function.client
                                .WebClientRequestException.class,
                        error -> new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No se pudo conectar con el servicio de pacientes"
                        )
                );
    }

    @PatchMapping("/{id}/estado")
    public Mono<Citas> cambiarEstado(
            @PathVariable Long id,
            @RequestBody EstadoCitaRequest solicitud) {

        if (solicitud == null
                || solicitud.getEstado() == null
                || solicitud.getEstado().isBlank()) {

            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "El estado es obligatorio"
                    )
            );
        }

        String nuevoEstado = solicitud.getEstado()
                .trim()
                .toUpperCase();

        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Estado de cita no válido"
                    )
            );
        }

        return citasRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cita no encontrada"
                        )
                ))
                .flatMap(cita -> {
                    cita.setEstado(nuevoEstado);
                    return citasRepository.save(cita);
                });
    }

    public static class EstadoCitaRequest {

        private String estado;

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
}
