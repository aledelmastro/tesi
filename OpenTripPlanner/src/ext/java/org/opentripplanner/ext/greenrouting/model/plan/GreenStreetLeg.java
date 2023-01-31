package org.opentripplanner.ext.greenrouting.model.plan;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.ext.greenrouting.edgetype.GreenFactor;
import org.opentripplanner.ext.greenrouting.edgetype.GreenStreetEdge;
import org.opentripplanner.model.plan.Place;
import org.opentripplanner.model.plan.StreetLeg;
import org.opentripplanner.model.plan.WalkStep;
import org.opentripplanner.routing.core.TraverseMode;

public class GreenStreetLeg extends StreetLeg implements AdditionalParams {

    // TODO controllare RaptorPathToItineraryMapper

    private List<Map<String, Double>> numericalP;
    private List<Map<String, Boolean>> booleanP;

    public GreenStreetLeg(
            TraverseMode mode,
            Calendar startTime,
            Calendar endTime,
            Place from,
            Place to,
            Double distanceMeters,
            int generalizedCost,
            LineString geometry,
            List<WalkStep> walkSteps
    ) {
        super(
                mode, startTime, endTime, from, to, distanceMeters, generalizedCost, geometry,
                walkSteps
        );
    }


    @Override
    public void setNumerical(List<Map<String, Double>> numericalP) {
        this.numericalP = numericalP;
    }

    @Override
    public void setBoolean(List<Map<String, Boolean>> booleanP) {
        this.booleanP = booleanP;
    }

    @Override
    public List<Map<String, Double>> getNumerical() {
        return this.numericalP;
    }

    @Override
    public List<Map<String, Boolean>> getBoolean() {
        return this.booleanP;
    }
}
