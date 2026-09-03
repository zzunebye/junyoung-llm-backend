package com.junyoung.llm_order_api.distance;

public interface DistanceService {
    int getDistance(double originLat, double originLon, double destLat, double destLon);

}
