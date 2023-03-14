package org.opentripplanner.ext.greenrouting.edgetype;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.opentripplanner.ext.greenrouting.GreenRouting;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.slf4j.LoggerFactory;

public interface GreenFactor {
    org.slf4j.Logger LOG = LoggerFactory.getLogger(GreenFactor.class);

    double getGreenyness();

    void setGreenyness(double greenyness);

    Map<String, Double> getScores();

    void setScores(Map<String, Double> scores);

    void putScore(String label, Double value);

    Map<String, Boolean> getFeatures();

    void setFeatures(Map<String, Boolean> features);

    void putFeature(String label, boolean value);

    default StateEditor greenTraverse(State s0, RoutingRequest options, TraverseMode traverseMode, boolean walkingBike, StreetEdge edge, CreateEditorFunction createEditor){
        if (traverseMode == null) { return null; }
        boolean backWalkingBike = s0.isBackWalkingBike();
        TraverseMode backMode = s0.getBackMode();
        Edge backEdge = s0.getBackEdge();
        if (backEdge != null) {
            // No illegal U-turns.
            // NOTE(flamholz): we check both directions because both edges get a chance to decide
            // if they are the reverse of the other. Also, because it doesn't matter which direction
            // we are searching in - these traversals are always disallowed (they are U-turns in one direction
            // or the other).
            // TODO profiling indicates that this is a hot spot.
            if (edge.isReverseOf(backEdge) || backEdge.isReverseOf(edge)) {
                return null;
            }
        }

        /* Check whether this street allows the current mode. */
        if (!edge.canTraverse(options, traverseMode)) {
            return null;
        }

        // Automobiles have variable speeds depending on the edge type
        double speed = edge.calculateSpeed(options, traverseMode, walkingBike);

        double time;
        double weight;
        // TODO(flamholz): factor out this bike, wheelchair and walking specific logic to somewhere central.
        if (traverseMode == TraverseMode.WALK) {
            if (options.wheelchairAccessible) {
                time = edge.getEffectiveWalkDistance() / speed;
                weight = edge.getEffectiveBikeDistance() / speed;
            }
            else if (walkingBike) {
                // take slopes into account when walking bikes
                time = weight = edge.getEffectiveBikeDistance() / speed;
            }
            else {
                // take slopes into account when walking
                time = weight = edge.getEffectiveWalkDistance() / speed;
            }
        }
        else {
            time = weight = edge.getDistanceMeters() / speed;
        }

        if (edge.isStairs()) {
            weight *= options.stairsReluctance;
        } else {
            weight *= options.getReluctance(traverseMode, walkingBike);
        }

        var s1 = edge.createEditor(s0, edge, traverseMode, walkingBike);

        if (edge.isTraversalBlockedByNoThruTraffic(traverseMode, backEdge, s0, s1)) {
            return null;
        }

        int roundedTime = (int) Math.ceil(time);

        /* Compute turn cost. */
        StreetEdge backPSE;
        if (backEdge instanceof StreetEdge) {
            backPSE = (StreetEdge) backEdge;
            RoutingRequest backOptions = s0.getOptions();
            double backSpeed = backPSE.calculateSpeed(backOptions, backMode, backWalkingBike);
            final double realTurnCost;  // Units are seconds.

            // Apply turn restrictions
            if (options.arriveBy && !edge.canTurnOnto(backPSE, s0, backMode)) {
                return null;
            } else if (!options.arriveBy && !backPSE.canTurnOnto(edge, s0, traverseMode)) {
                return null;
            }

            /*
             * This is a subtle piece of code. Turn costs are evaluated differently during
             * forward and reverse traversal. During forward traversal of an edge, the turn
             * *into* that edge is used, while during reverse traversal, the turn *out of*
             * the edge is used.
             *
             * However, over a set of edges, the turn costs must add up the same (for
             * general correctness and specifically for reverse optimization). This means
             * that during reverse traversal, we must also use the speed for the mode of
             * the backEdge, rather than of the current edge.
             */
            if (options.arriveBy && edge.getToVertex() instanceof IntersectionVertex) { // arrive-by search
                IntersectionVertex traversedVertex = ((IntersectionVertex) edge.getToVertex());

                realTurnCost = backOptions.getIntersectionTraversalCostModel().computeTraversalCost(
                        traversedVertex, edge, backPSE, backMode, backOptions, (float) speed,
                        (float) backSpeed);
            } else if (!options.arriveBy && edge.getFromVertex() instanceof IntersectionVertex) { // depart-after search
                IntersectionVertex traversedVertex = ((IntersectionVertex) edge.getFromVertex());

                realTurnCost = options.getIntersectionTraversalCostModel().computeTraversalCost(
                        traversedVertex, backPSE, edge, traverseMode, options, (float) backSpeed,
                        (float) speed);
            } else {
                // In case this is a temporary edge not connected to an IntersectionVertex
                // LOG.debug("Not computing turn cost for edge {}", this);
                realTurnCost = 0;
            }

            if (!traverseMode.isDriving()) {
                s1.incrementWalkDistance(realTurnCost / 100);  // just a tie-breaker
            }

            int turnTime = (int) Math.ceil(realTurnCost);
            roundedTime += turnTime;
            weight += options.turnReluctance * realTurnCost;
        }

        if (!traverseMode.isDriving()) {
            s1.incrementWalkDistance(edge.getEffectiveBikeDistance());
        }

        if (options.expression != null) {
            var variables = new HashMap<>(getScores());
            getFeatures().forEach((key, value) -> variables.put(key, value ? 1d : 0d));

            if (options.expression.getVariableNames().contains("weight"))
                variables.put("weight", weight);

            var expression = options.expression.setVariables(variables);
            if (expression.validate().isValid()) {
                weight = expression.evaluate() * edge.getDistanceMeters();
                if (weight < 0) weight = 0;
            }
        }

        s1.incrementTimeInSeconds(roundedTime);

        s1.incrementWeight(weight);

        return s1;
    }
}
