package org.opentripplanner.standalone.config.sandbox.greenrouting;

import java.util.Set;
import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig;
import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig.GreenMappingMode;
import org.opentripplanner.standalone.config.NodeAdapter;


public class GreenRoutingConfigMapper {

    public static GreenRoutingConfig map(NodeAdapter c) {
        if (c.isEmpty()) {
            return null;
        }

        GreenMappingMode mode = null;
        var variables = c.asTextSet("scores", Set.of());
        var features = c.asTextSet("features", Set.of());
        var formula = c.asText("formula");

        return new GreenRoutingConfig(
                c.asText("inputFile"),
                c.asText("id"),
                c.asDouble("bufferSize"),
                variables,
                features,
                formula,
                c.asText("outputFile"),
                c.asText("logFile")
        );
    }
}
