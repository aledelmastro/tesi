package org.opentripplanner.ext.greenrouting.configuration;

import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class GreenRoutingConfig {

    private final String fileName;
    private final String id;
    private final double bufferSize;
    private final Set<String> properties;

    private final Set<String> features;

    private final Expression expression;
    private final String outputFileName;

    private final String logFileName;

    public GreenRoutingConfig(
            String fileName,
            String id,
            double bufferSize,
            Set<String> vars,
            Set<String> features,
            String formula,
            String outputFileName,
            String logFileName
    ) {
        this.fileName = fileName;
        this.id = id;
        this.bufferSize = bufferSize;
        this.properties = vars;
        this.features = features;
        this.outputFileName = outputFileName;
        this.logFileName = logFileName;

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

    public Set<String> getFeatures() {
        return features;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public enum GreenMappingMode {
        FAST, AVERAGE, FIT_DATA_SEGMENTS
    }

}
