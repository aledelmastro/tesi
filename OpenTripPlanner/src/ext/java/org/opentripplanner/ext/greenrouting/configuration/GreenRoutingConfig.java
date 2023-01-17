package org.opentripplanner.ext.greenrouting.configuration;

import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class GreenRoutingConfig {

    private final String fileName;
    private final String id;
    private final double bufferSize;
    private final Set<String> properties;
    private final GreenMappingMode mode;
    private final Expression expression;
    private final String outputFileName;

    public GreenRoutingConfig(
            String fileName,
            String id,
            double bufferSize,
            Set<String> vars,
            GreenMappingMode mode,
            String formula,
            String outputFileName
    ) {
        this.fileName = fileName;
        this.id = id;
        this.bufferSize = bufferSize;
        this.properties = vars;
        this.mode = mode;
        this.outputFileName = outputFileName;

        this.expression = new ExpressionBuilder(formula).variables(properties).build();
    }

    public Expression getExpression() {
        return expression;
    }

    public String getId() {
        return id;
    }

    public String getOutputFileName() {
        return this.outputFileName;
    }

    public double getBufferSize() {
        return this.bufferSize;
    }

    public Set<String> getProperties() {
        return properties;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean fastMapping() {
        return this.mode == GreenMappingMode.FAST;
    }

    public boolean weightedAverageMapping() {
        return this.mode == GreenMappingMode.AVERAGE;
    }

    public boolean fitDataSegmentsMapping() {
        return this.mode == GreenMappingMode.FIT_DATA_SEGMENTS;
    }

    public enum GreenMappingMode {
        FAST, AVERAGE, FIT_DATA_SEGMENTS
    }

}
