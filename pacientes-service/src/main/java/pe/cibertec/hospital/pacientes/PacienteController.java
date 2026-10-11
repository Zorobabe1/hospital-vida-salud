
package pe.cibertec.hospital.pacientes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteRepository pacienteRepository;

    public PacienteController(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @GetMapping
    public Flux<Paciente> listarPacientes() {
        return pacienteRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Paciente> buscarPorId(@PathVariable Long id) {
        return pacienteRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Paciente no encontrado"
                        )
                ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Paciente> registrarPaciente(
            @RequestBody Paciente paciente) {

        if (paciente.getTipoDocumento() == null
                || paciente.getTipoDocumento().isBlank()
                || paciente.getNumeroDocumento() == null
                || paciente.getNumeroDocumento().isBlank()
                || paciente.getNombres() == null
                || paciente.getNombres().isBlank()
                || paciente.getApellidos() == null
                || paciente.getApellidos().isBlank()) {

            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de documento, número, nombres y apellidos son obligatorios"
            ));
        }

        return pacienteRepository
                .findByTipoDocumentoAndNumeroDocumento(
                        paciente.getTipoDocumento(),
                        paciente.getNumeroDocumento())
                .hasElement()
                .flatMap(existe -> {
                    if (existe) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Ya existe un paciente con ese documento"
                        ));
                    }

                    paciente.setId(null);

                    if (paciente.getEstado() == null
                            || paciente.getEstado().isBlank()) {
                        paciente.setEstado("ACTIVO");
                    }

                    return pacienteRepository.save(paciente);
                });
    }

    @PutMapping("/{id}")
    public Mono<Paciente> actualizarPaciente(
            @PathVariable Long id,
            @RequestBody Paciente datos) {

        if (datos.getTipoDocumento() == null
                || datos.getTipoDocumento().isBlank()
                || datos.getNumeroDocumento() == null
                || datos.getNumeroDocumento().isBlank()
                || datos.getNombres() == null
                || datos.getNombres().isBlank()
                || datos.getApellidos() == null
                || datos.getApellidos().isBlank()) {

            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de documento, número, nombres y apellidos son obligatorios"
            ));
        }

        return pacienteRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Paciente no encontrado"
                        )
                ))
                .flatMap(paciente ->
                        pacienteRepository
                                .findByTipoDocumentoAndNumeroDocumento(
                                        datos.getTipoDocumento(),
                                        datos.getNumeroDocumento())
                                .filter(otro -> !otro.getId().equals(id))
                                .hasElement()
                                .flatMap(duplicado -> {
                                    if (duplicado) {
                                        return Mono.error(
                                                new ResponseStatusException(
                                                        HttpStatus.CONFLICT,
                                                        "El documento ya pertenece a otro paciente"
                                                )
                                        );
                                    }

                                    paciente.setTipoDocumento(
                                            datos.getTipoDocumento());
                                    paciente.setNumeroDocumento(
                                            datos.getNumeroDocumento());
                                    paciente.setNombres(datos.getNombres());
                                    paciente.setApellidos(datos.getApellidos());
                                    paciente.setFechaNacimiento(
                                            datos.getFechaNacimiento());
                                    paciente.setTelefono(datos.getTelefono());
                                    paciente.setCorreo(datos.getCorreo());
                                    paciente.setDireccion(datos.getDireccion());

                                    if (datos.getEstado() != null
                                            && !datos.getEstado().isBlank()) {
                                        paciente.setEstado(datos.getEstado());
                                    }

                                    return pacienteRepository.save(paciente);
                                }));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> eliminarPaciente(@PathVariable Long id) {
        return pacienteRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Paciente no encontrado"
                        )
                ))
                .flatMap(paciente -> pacienteRepository.deleteById(id));
    }
}
