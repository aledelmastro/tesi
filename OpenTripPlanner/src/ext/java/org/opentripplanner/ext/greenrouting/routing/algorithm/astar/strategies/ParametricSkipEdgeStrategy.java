package org.opentripplanner.ext.greenrouting.routing.algorithm.astar.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.opentripplanner.ext.greenrouting.edgetype.GreenFactor;
import org.opentripplanner.routing.algorithm.astar.strategies.SkipEdgeStrategy;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.ShortestPathTree;

public class ParametricSkipEdgeStrategy implements SkipEdgeStrategy {
    private final List<Predicate<GreenFactor>> numericalConditions = new ArrayList<>();
    private final List<Predicate<GreenFactor>> booleanConditions = new ArrayList<>();

    public ParametricSkipEdgeStrategy() {super();}

    @Override
    public boolean shouldSkipEdge(
            Set<Vertex> origins,
            Set<Vertex> targets,
            State current,
            Edge edge,
            ShortestPathTree spt,
            RoutingRequest traverseOptions
    ) {
        if (!(edge instanceof GreenFactor)) return false;

        var gf = (GreenFactor) edge;
        return numericalConditions.stream().anyMatch(c -> c.test(gf))
                || booleanConditions.stream().anyMatch(c -> c.test(gf));
    }

    public void addNumericalCondition(Predicate<GreenFactor> condition) {
        this.numericalConditions.add(condition);
    }

    public void addBooleanCondition(Predicate<GreenFactor> condition) {
        this.booleanConditions.add(condition);
    }
}
