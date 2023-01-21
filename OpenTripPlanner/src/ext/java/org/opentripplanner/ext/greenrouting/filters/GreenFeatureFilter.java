package org.opentripplanner.ext.greenrouting.filters;

import java.util.function.Predicate;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.deletionflagger.ItineraryDeletionFlagger;

public class GreenFeatureFilter implements ItineraryDeletionFlagger {

    private final double threshold;
    private final String feature;
    private final boolean below;

    public GreenFeatureFilter(double threshold, String feature, boolean below) {
        this.threshold = threshold;
        this.feature = feature;
        this.below = below;
    }

    @Override
    public String name() {
        return "Green feature filter";
    }

    @Override
    public Predicate<Itinerary> predicate() {
        return ItineraryDeletionFlagger.super.predicate();
    }
}
