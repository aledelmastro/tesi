/*
package org.opentripplanner.ext.greenrouting.model.plan;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import org.opentripplanner.ext.greenrouting.edgetype.GreenFactor;
import org.opentripplanner.ext.greenrouting.edgetype.GreenStreetEdge;
import org.opentripplanner.model.TripPattern;
import org.opentripplanner.model.plan.ScheduledTransitLeg;
import org.opentripplanner.model.transfer.ConstrainedTransfer;
import org.opentripplanner.routing.trippattern.TripTimes;

public class GreenScheduledTransitLeg extends ScheduledTransitLeg implements AdditionalParams{
    private List<GreenFactor> edges;

    public GreenScheduledTransitLeg(
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
            int generalizedCost
    ) {
        super(tripTimes, tripPattern, boardStopIndexInPattern, alightStopIndexInPattern, startTime,
                endTime, serviceDate, zoneId, transferFromPreviousLeg, transferToNextLeg,
                generalizedCost
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
