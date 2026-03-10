package org.uteq.sgacfinal.event;

import lombok.Getter;
import org.uteq.sgacfinal.entity.Convocatoria;

@Getter
public class ConvocatoriaCreadaEvent {

    private final Integer idConvocatoria;

    public ConvocatoriaCreadaEvent(Convocatoria convocatoria) {
        this.idConvocatoria = convocatoria != null ? convocatoria.getIdConvocatoria() : null;
    }
}

