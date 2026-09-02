package com.junyoung.llm_order_api.service;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.junyoung.llm_order_api.maps.MapsProperties;

// Why need to return and store int not long?
@Service
@ConditionalOnProperty(name = "maps.provider", havingValue = "GOOGLE", matchIfMissing = true)
public class GoogleDistanceService implements DistanceService {
    private final RestClient restClient;

    public GoogleDistanceService(
            RestClient.Builder restClientBuilder,
            MapsProperties mapsProperties

    ) {
        this.restClient = restClientBuilder
                .baseUrl("https://routes.googleapis.com")
                .defaultHeader(
                        "X-Goog-Api-Key",
                        mapsProperties.apiKey())
                .build();
    }

    @Override
    public int getDistance(double originLat, double originLon, double destLat, double destLon) {

        var request = new ComputeRoutesRequest(
                new ComputeRoutesRequest.Waypoint(
                        new ComputeRoutesRequest.Location(new ComputeRoutesRequest.LatLng(originLat, originLon))),
                new ComputeRoutesRequest.Waypoint(
                        new ComputeRoutesRequest.Location(new ComputeRoutesRequest.LatLng(destLat, destLon))),
                "DRIVE");

        ComputeRoutesResponse response = restClient.post()
                .uri("/directions/v2:computeRoutes")
                .header(
                        "X-Goog-FieldMask",
                        "routes.distanceMeters")
                .body(request)
                .retrieve()
                .body(ComputeRoutesResponse.class);

        if (response == null || response.routes().isEmpty()) {
            throw new IllegalStateException("Route not found");
        }
        return (int) response.routes().get(0).distanceMeters();
    }

}

record ComputeRoutesRequest(
        Waypoint origin,
        Waypoint destination,
        String travelMode) {

    public record Waypoint(Location location) {
    }

    public record Location(LatLng latLng) {
    }

    public record LatLng(
            double latitude,
            double longitude) {
    }
}

record ComputeRoutesResponse(
        List<Route> routes) {

    public record Route(
            long distanceMeters) {
    }
}