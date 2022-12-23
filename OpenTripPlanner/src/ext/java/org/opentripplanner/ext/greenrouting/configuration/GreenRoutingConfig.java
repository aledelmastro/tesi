package org.opentripplanner.ext.greenrouting.configuration;

public class GreenRoutingConfig {
    public enum GreenMappingMode{
        FAST,
        WEIGHTED,
        FIT_DATA_SEGMENTS
    };

    private final String fileName;
    private final String id;
    private final String scores;
    private final GreenMappingMode mode;

    public GreenRoutingConfig(String fileName, String id, String scores, GreenMappingMode mode) {
        this.fileName = fileName;
        this.id = id;
        this.scores = scores;
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public String getScores() {
        return scores;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean fastMapping() {
        return this.mode == GreenMappingMode.FAST;
    }

    public boolean weightedMapping() {
        return this.mode == GreenMappingMode.WEIGHTED;
    }

    public boolean fitDataSegmentsMapping() {
        return this.mode == GreenMappingMode.FIT_DATA_SEGMENTS;
    }

}
