package org.opentripplanner.ext.greenrouting.api.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Information about a street edge.
 */
public class ApiGreenEdgeInfo implements Serializable {
    public final Double length;
    public final Map<String, Double> scores;
    public final Map<String, Boolean> features;

    public ApiGreenEdgeInfo(
            Double length,
            Map<String, Double> scores,
            Map<String, Boolean> features
    ) {
        this.features = features;
        this.scores = scores;
        this.length = length;
    }
}
