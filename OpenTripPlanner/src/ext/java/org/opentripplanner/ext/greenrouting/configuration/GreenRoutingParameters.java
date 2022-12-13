package org.opentripplanner.ext.greenrouting.configuration;

import java.util.HashMap;
import java.util.Map;

public class GreenRoutingParameters {
    private Map<String, Double> map;

    public GreenRoutingParameters() {
        map = new HashMap<>();
    }

    public void add(String id, Double score) {
        map.put(id, score);
    }

    public boolean hasSCore(String id) {
        return map.containsKey(id);
    }

    public Double getScore(String id) {
        return map.get(id);
    }

}
