package br.com.fiap.cuidai.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Regra do CuidAi: o medico ja tem consulta agendada nesse horario. Devolve 409. */
public class HorarioOcupadoException extends RegraNegocioException {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

    public HorarioOcupadoException(Long medicoId, LocalDateTime dataConsulta) {
        super("O medico " + medicoId + " ja tem uma consulta agendada em "
                + dataConsulta.format(FORMATO) + ". Escolha outro horario.");
    }
}
