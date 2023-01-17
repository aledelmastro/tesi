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
        var formula = c.asText("formula");

        var mappingType = c.asText("mappingType", "fast");
        switch (mappingType) {
            case "fast":
                mode = GreenMappingMode.FAST;
                break;
            case "average":
                mode = GreenMappingMode.AVERAGE;
                break;
        }

        return new GreenRoutingConfig(
                c.asText("inputFile"),
                c.asText("id"),
                c.asDouble("bufferSize"),
                variables,
                mode,
                formula,
                c.asText("outputFile")
        );
    }
}
