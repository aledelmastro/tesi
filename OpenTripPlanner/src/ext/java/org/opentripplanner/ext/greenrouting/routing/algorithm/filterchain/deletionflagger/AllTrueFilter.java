package org.opentripplanner.ext.greenrouting.routing.algorithm.filterchain.deletionflagger;

import java.util.Collection;
import java.util.function.Function;

public class AllTrueFilter<T> implements SingleConditionFilter<T> {

    private Function<T, Boolean> valueExtractor;

    private AllTrueFilter() {}

    public AllTrueFilter(Function<T, Boolean> valueExtractor) {
        this.valueExtractor = valueExtractor;
    }

    @Override
    public boolean filter(T value) {
        var extracted = valueExtractor.apply(value);
        return extracted != null ? extracted : true;
    }

    @Override
    public boolean filter(Collection<T> value) {
        return value.stream().allMatch(v -> filter(v));
    }
}
