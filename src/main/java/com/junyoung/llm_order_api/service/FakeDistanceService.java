package com.junyoung.llm_order_api.service;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class FakeDistanceService implements DistanceService {

    @Override
    public int getDistance(double originLat, double originLon, double destLat, double destLon) {
        Random random = new Random();
        return random.nextInt(1000) + 1;
    }

}
