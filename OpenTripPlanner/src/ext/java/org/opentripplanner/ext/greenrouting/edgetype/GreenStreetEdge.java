package org.opentripplanner.ext.greenrouting.edgetype;

import java.util.HashMap;
import java.util.Map;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.common.model.P2;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.vertextype.SplitterVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.util.I18NString;

public class GreenStreetEdge extends StreetEdge implements GreenFactor {
    private Double greenyness = 0.0;

    private Map<String, Double> variables = new HashMap<>();
    private Map<String, Boolean> features = new HashMap<>();

    public GreenStreetEdge(
            StreetVertex v1,
            StreetVertex v2,
            LineString geometry,
            I18NString name,
            double length,
            StreetTraversalPermission permission,
            boolean back
    ) {
        super(v1, v2, geometry, name, length, permission, back);
    }

    public GreenStreetEdge(
            StreetVertex v1,
            StreetVertex v2,
            LineString geometry,
            I18NString name,
            StreetTraversalPermission permission,
            boolean back
    ) {
        super(v1, v2, geometry, name, permission, back);
    }

    @Override
    public double getGreenyness() {
        return greenyness;
    }

    @Override
    public void setGreenyness(double greenyness) {
        this.greenyness = greenyness;
    }

    @Override
    public Map<String, Double> getScores() {
        return this.variables;
    }

    @Override
    public void setScores(Map<String, Double> scores) {
        this.variables = scores;
    }

    @Override
    public void putScore(String label, Double value) {
        this.variables.put(label, value);
    }

    @Override
    public Map<String, Boolean> getFeatures() {
        return this.features;
    }

    @Override
    public void setFeatures(Map<String, Boolean> features) {
        this.features = features;
    }

    @Override
    public void putFeature(String label, boolean value) {
        this.features.put(label, value);
    }

    public P2<StreetEdge> splitDestructively(SplitterVertex v) {
        var splitEdges = super.splitDestructively(v);
        var greenSplitEdges = new StreetEdge[]{
                fromStreetEdge(splitEdges.first), fromStreetEdge(splitEdges.second)
        };

        return new P2<>(greenSplitEdges);
    }

    private static GreenStreetEdge fromStreetEdge(StreetEdge e) {
        GreenStreetEdge greenEdge = new GreenStreetEdge((StreetVertex) e.getFromVertex(),
                (StreetVertex) e.getToVertex(), e.getGeometry(), e.getName(), e.getPermission(),
                e.isBack()
        );

        e.getToVertex().removeIncoming(e);
        e.getFromVertex().removeOutgoing(e);

        greenEdge.setBicycleSafetyFactor(e.getBicycleSafetyFactor());
        greenEdge.setHasBogusName(e.hasBogusName());
        greenEdge.setStairs(e.isStairs());
        greenEdge.setWheelchairAccessible(e.isWheelchairAccessible());
        greenEdge.setBack(e.isBack());
        if (e.wayId == 532533560) {
            int i = 97;
        }
        greenEdge.wayId = e.wayId;

        return greenEdge;
    }
}
