package com.campuslab.ms_campuslab_bookings.Service;

import com.campuslab.ms_campuslab_bookings.Model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AuditClient {

    private final RestClient restClient;

    public AuditClient(@Value("${audit.service.url}") String auditServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(auditServiceUrl).build();
    }

    public void registerEvent(Booking booking, String action, String actorId, String actorRole) {
        Map<String, Object> body = Map.of(
                "bookingId", booking.getId().toString(),
                "action", action,
                "actorId", actorId,
                "actorRole", actorRole,
                "timestamp", LocalDateTime.now().toString()
        );

        restClient.post()
                .uri("/api/audit/events")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
