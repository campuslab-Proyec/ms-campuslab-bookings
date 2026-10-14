package com.campuslab.ms_campuslab_bookings.Service;

import com.campuslab.ms_campuslab_bookings.Model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AuditClient {

    private final RestClient restClient;

    public AuditClient(@Value("${audit.service.url}") String auditServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(auditServiceUrl).build();
    }

    public void registerEvent(Booking booking, String action, String actorId, String actorRole, String bearerToken) {
        Map<String, Object> body = Map.of(
                "bookingId", booking.getId().toString(),
                "action", action,
                "actorId", actorId,
                "actorRole", actorRole,
                "timestamp", LocalDateTime.now().toString()
        );

        restClient.post()
                .uri("/api/audit/events")
                .headers(headers -> {
                    // El servicio de audit exige JWT; reenviamos el token del usuario autenticado.
                    if (StringUtils.hasText(bearerToken)) {
                        headers.setBearerAuth(bearerToken);
                    }
                })
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
