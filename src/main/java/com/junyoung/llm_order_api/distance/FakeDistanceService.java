package com.junyoung.llm_order_api.distance;

import java.util.Random;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "maps.provider", havingValue = "FAKE")
public class FakeDistanceService implements DistanceService {

    @Override
    public int getDistance(double originLat, double originLon, double destLat, double destLon) {
        Random random = new Random();
        return random.nextInt(1000) + 1;
    }

}
