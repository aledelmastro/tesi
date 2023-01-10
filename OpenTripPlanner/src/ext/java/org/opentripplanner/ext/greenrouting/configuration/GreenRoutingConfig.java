package org.opentripplanner.ext.greenrouting.configuration;

import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.opentripplanner.ext.greenrouting.GreenFeature;

public class GreenRoutingConfig {

    private final String fileName;
    private final String id;
    private final Set<String> variables;
    private final GreenMappingMode mode;
    private final Expression expression;

    public GreenRoutingConfig(
            String fileName,
            String id,
            Set<String> variables,
            GreenMappingMode mode,
            String formula
    ) {
        this.fileName = fileName;
        this.id = id;
        this.variables = variables;
        this.mode = mode;

        this.expression = new ExpressionBuilder(formula).variables(variables).build();
    }

    public Expression getExpression() {
        return expression;
    }

    public String getId() {
        return id;
    }

    public Set<String> getVariables() {
        return variables;
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
        FAST,
        AVERAGE,
        FIT_DATA_SEGMENTS
    }

}
