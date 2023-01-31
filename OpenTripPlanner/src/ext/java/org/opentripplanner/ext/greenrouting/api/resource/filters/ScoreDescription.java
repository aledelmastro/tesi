package org.opentripplanner.ext.greenrouting.api.resource.filters;

public class ScoreDescription {
    private String name;
    private boolean below;
    private double threshold;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBelow() {
        return below;
    }

    public void setBelow(boolean below) {
        this.below = below;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public ScoreDescription() {}


}
