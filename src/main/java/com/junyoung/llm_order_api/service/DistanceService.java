package com.junyoung.llm_order_api.service;

public interface DistanceService {
    int getDistance(double originLat, double originLon, double destLat, double destLon);

}
