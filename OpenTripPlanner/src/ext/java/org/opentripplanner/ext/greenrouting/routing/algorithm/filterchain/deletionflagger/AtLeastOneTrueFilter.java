package org.opentripplanner.ext.greenrouting.routing.algorithm.filterchain.deletionflagger;

import java.util.function.Function;

public class AtLeastOneTrueFilter<T> implements SingleConditionFilter<T> {

    private Function<T, Boolean> valueExtractor;

    private AtLeastOneTrueFilter() {}

    public AtLeastOneTrueFilter(Function<T, Boolean> valueExtractor) {
        this.valueExtractor = valueExtractor;
    }

    @Override
    public boolean filter(T value) {
        var extracted = valueExtractor.apply(value);
        return extracted != null ? extracted : false;
    }
}
