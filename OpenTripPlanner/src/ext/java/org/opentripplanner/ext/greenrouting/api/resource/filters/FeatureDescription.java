package org.opentripplanner.ext.greenrouting.api.resource.filters;

public class FeatureDescription {
    private String name;
    private boolean presence;

    public FeatureDescription() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPresence() {
        return presence;
    }

    public void setPresence(boolean presence) {
        this.presence = presence;
    }
}
