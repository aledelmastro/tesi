/*
package org.opentripplanner.ext.greenrouting.model.plan;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import org.opengis.filter.expression.Add;
import org.opentripplanner.ext.greenrouting.edgetype.GreenFactor;
import org.opentripplanner.ext.greenrouting.edgetype.GreenStreetEdge;
import org.opentripplanner.model.TripPattern;
import org.opentripplanner.model.plan.FrequencyTransitLeg;
import org.opentripplanner.model.transfer.ConstrainedTransfer;
import org.opentripplanner.routing.trippattern.TripTimes;

public class GreenFrequencyTransitLeg extends FrequencyTransitLeg implements AdditionalParams {

    private List<GreenFactor> edges;

    public GreenFrequencyTransitLeg(
            TripTimes tripTimes,
            TripPattern tripPattern,
            int boardStopIndexInPattern,
            int alightStopIndexInPattern,
            Calendar startTime,
            Calendar endTime,
            LocalDate serviceDate,
            ZoneId zoneId,
            ConstrainedTransfer transferFromPreviousLeg,
            ConstrainedTransfer transferToNextLeg,
            int generalizedCost,
            int frequencyHeadwayInSeconds
    ) {
        super(tripTimes, tripPattern, boardStopIndexInPattern, alightStopIndexInPattern, startTime,
                endTime, serviceDate, zoneId, transferFromPreviousLeg, transferToNextLeg,
                generalizedCost, frequencyHeadwayInSeconds
        );
    }

    @Override
    public List<GreenFactor> getEdges() {
        return this.edges;
    }

    @Override
    public void setEdges(List<GreenFactor> edges) {
        this.edges = edges;
    }
}
*/
