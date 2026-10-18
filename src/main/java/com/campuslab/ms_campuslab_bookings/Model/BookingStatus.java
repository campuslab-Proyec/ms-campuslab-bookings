package com.campuslab.ms_campuslab_bookings.Model;

import java.util.Set;

/**
 * Ciclo de vida de una reserva:
 * SOLICITADA -> APROBADA -> EN_PREPARACION -> EN_USO -> DEVUELTA,
 * y se puede CANCELAR mientras el equipo aún no se haya entregado.
 */
public enum BookingStatus {
    SOLICITADA,
    APROBADA,
    EN_PREPARACION,
    EN_USO,
    DEVUELTA,
    CANCELADA;

    public Set<BookingStatus> nextStatuses() {
        return switch (this) {
            case SOLICITADA -> Set.of(APROBADA, CANCELADA);
            case APROBADA -> Set.of(EN_PREPARACION, CANCELADA);
            case EN_PREPARACION -> Set.of(EN_USO, CANCELADA);
            case EN_USO -> Set.of(DEVUELTA);
            case DEVUELTA, CANCELADA -> Set.of();
        };
    }

    public boolean canTransitionTo(BookingStatus target) {
        return nextStatuses().contains(target);
    }
}
