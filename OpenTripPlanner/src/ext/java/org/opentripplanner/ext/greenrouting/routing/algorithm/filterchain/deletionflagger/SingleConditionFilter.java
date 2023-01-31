package org.opentripplanner.ext.greenrouting.routing.algorithm.filterchain.deletionflagger;

import java.util.Collection;
import org.opentripplanner.ext.greenrouting.model.plan.AdditionalParams;

public interface SingleConditionFilter<T> {
    boolean filter(T value);
    default boolean filter(Collection<T> value) {
        return value.stream().anyMatch(v -> filter(v));
    }
}
