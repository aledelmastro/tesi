package org.opentripplanner.ext.greenrouting.routing.algorithm.filterchain.deletionflagger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.opentripplanner.ext.greenrouting.model.plan.AdditionalParams;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.deletionflagger.ItineraryDeletionFlagger;

public class GreenFeatureFilter implements ItineraryDeletionFlagger {
    private final List<SingleConditionFilter<Map<String, Double>>> numFilters;
    private final List<SingleConditionFilter<Map<String, Boolean>>> boolFilters;

    public GreenFeatureFilter() {
        this.boolFilters = new ArrayList<>();
        this.numFilters = new ArrayList<>();
    }

    @Override
    public String name() {
        return "Green feature filter";
    }

    @Override
    public Predicate<Itinerary> predicate() {
        return itinerary -> {
            var suitableLegs = itinerary.legs.stream()
                    .filter(AdditionalParams.class::isInstance)
                    .map(AdditionalParams.class::cast)
                    .collect(Collectors.toList());

            if (suitableLegs.isEmpty()) {return false;}

            for (var filter: numFilters) {
                var num = suitableLegs.stream().anyMatch(l -> filter.filter(l.getNumerical()));
                if (num) return true;
            }

            for (var filter: boolFilters) {
                var bool = suitableLegs.stream().anyMatch(l -> filter.filter(l.getBoolean()));
                if (bool) return true;
            }
            
            return false;
        };
    }

    void addNumFilter(SingleConditionFilter<Map<String, Double>> filter) {
        numFilters.add(filter);
    }

    void addBooleanFilter(SingleConditionFilter<Map<String, Boolean>> filter) {
        boolFilters.add(filter);
    }
}
