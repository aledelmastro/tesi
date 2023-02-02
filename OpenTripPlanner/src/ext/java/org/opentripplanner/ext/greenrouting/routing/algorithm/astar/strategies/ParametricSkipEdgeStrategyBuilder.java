package org.opentripplanner.ext.greenrouting.routing.algorithm.astar.strategies;

import org.opentripplanner.ext.greenrouting.api.resource.filters.FeatureDescription;
import org.opentripplanner.ext.greenrouting.api.resource.filters.ScoreDescription;

public class ParametricSkipEdgeStrategyBuilder {
    private ParametricSkipEdgeStrategy strategy;

    public ParametricSkipEdgeStrategyBuilder() {
        this.strategy = new ParametricSkipEdgeStrategy();
    }

    public ParametricSkipEdgeStrategy build() {
        return this.strategy;
    }

    public ParametricSkipEdgeStrategyBuilder has(FeatureDescription description) {
        if (description.isPresence())
            this.has(description.getName());
        else
            this.doesntHave(description.getName());

        return this;
    }

    public ParametricSkipEdgeStrategyBuilder has(ScoreDescription description) {
        if (description.isBelow())
            this.lowerThan(description.getName(), description.getThreshold());
        else
            this.higherThan(description.getName(), description.getThreshold());

        return this;
    }

    public ParametricSkipEdgeStrategyBuilder higherThan(String key, double threshold) {
        strategy.addNumericalCondition(edge -> edge.getScores().getOrDefault(key, threshold) > threshold);
        return this;
    }

    public ParametricSkipEdgeStrategyBuilder lowerThan(String key, double threshold) {
        strategy.addNumericalCondition(edge -> edge.getScores().getOrDefault(key, threshold) < threshold);
        return this;
    }

    public ParametricSkipEdgeStrategyBuilder has(String key) {
        strategy.addBooleanCondition(edge -> edge.getFeatures().getOrDefault(key, false));
        return this;
    }

    public ParametricSkipEdgeStrategyBuilder doesntHave(String key) {
        strategy.addBooleanCondition(edge -> !edge.getFeatures().getOrDefault(key, false));
        return this;
    }
}
