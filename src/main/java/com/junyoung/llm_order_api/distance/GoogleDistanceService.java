package com.junyoung.llm_order_api.distance;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.junyoung.llm_order_api.exceptions.BusinessException;
import com.junyoung.llm_order_api.exceptions.ErrorCode;

// Why need to return and store int not long?
@Service
@ConditionalOnProperty(name = "maps.provider", havingValue = "GOOGLE", matchIfMissing = true)
public class GoogleDistanceService implements DistanceService {
        private final RestClient restClient;

        private static final String BASE_URL = "https://routes.googleapis.com";
        private static final String COMPUTE_ROUTES_URI = "/directions/v2:computeRoutes";
        private static final String X_GOOG_FIELD_MASK = "routes.distanceMeters";

        public GoogleDistanceService(
                        RestClient.Builder restClientBuilder,
                        MapsProperties mapsProperties

        ) {
                this.restClient = restClientBuilder
                                .baseUrl(BASE_URL)
                                .defaultHeader(
                                                "X-Goog-Api-Key",
                                                mapsProperties.apiKey())
                                .build();
        }

        @Override
        public int getDistance(double originLat, double originLon, double destLat, double destLon) {
                try {
                        var request = new ComputeRoutesRequest(
                                        new ComputeRoutesRequest.Waypoint(
                                                        new ComputeRoutesRequest.Location(
                                                                        new ComputeRoutesRequest.LatLng(originLat,
                                                                                        originLon))),
                                        new ComputeRoutesRequest.Waypoint(
                                                        new ComputeRoutesRequest.Location(
                                                                        new ComputeRoutesRequest.LatLng(destLat,
                                                                                        destLon))),
                                        "DRIVE");
                        ComputeRoutesResponse response = restClient.post()
                                        .uri(COMPUTE_ROUTES_URI)
                                        .header(
                                                        "X-Goog-FieldMask",
                                                        X_GOOG_FIELD_MASK)
                                        .body(request)
                                        .retrieve()
                                        .body(ComputeRoutesResponse.class);

                        if (response == null || response.routes() == null || response.routes().isEmpty()) {
                                throw new BusinessException(ErrorCode.ROUTE_NOT_FOUND);
                        }
                        return (int) response.routes().get(0).distanceMeters();
                } catch (Exception e) {
                        throw new BusinessException(ErrorCode.DISTANCE_SERVICE_ERROR);
                }
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