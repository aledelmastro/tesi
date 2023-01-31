package org.opentripplanner.ext.greenrouting.routing.algorithm.filterchain.deletionflagger;

import java.util.function.Function;

public class AboveTheThresholdFilter<T> implements SingleConditionFilter<T>{
    private double threshold;
    private Function<T, Double> valueExtractor;

    private AboveTheThresholdFilter() {}

    public AboveTheThresholdFilter(Function<T, Double> valueExtractor, double threshold) {
        this.threshold = threshold;
        this.valueExtractor = valueExtractor;
    }

    @Override
    public boolean filter(T value) {
        if (value == null)
            return false;

        return valueExtractor.apply(value) > this.threshold;
    }
}
