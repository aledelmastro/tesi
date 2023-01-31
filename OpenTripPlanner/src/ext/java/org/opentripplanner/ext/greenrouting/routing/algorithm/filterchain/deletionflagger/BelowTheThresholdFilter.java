package org.opentripplanner.ext.greenrouting.routing.algorithm.filterchain.deletionflagger;

import java.util.function.Function;

public class BelowTheThresholdFilter<T> implements SingleConditionFilter<T>{
    private double threshold;
    private Function<T, Double> valueExtractor;

    private BelowTheThresholdFilter() {}

    public BelowTheThresholdFilter(Function<T, Double> valueExtractor, double threshold) {
        this.threshold = threshold;
        this.valueExtractor = valueExtractor;
    }

    @Override
    public boolean filter(T value) {
        if (value == null || valueExtractor.apply(value) == null)
            return false;

        return valueExtractor.apply(value) < this.threshold;
    }
}
