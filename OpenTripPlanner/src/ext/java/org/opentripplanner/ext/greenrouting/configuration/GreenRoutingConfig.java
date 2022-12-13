package org.opentripplanner.ext.greenrouting.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GreenRoutingConfig {
    private final String fileName;
    private final String id;

    public GreenRoutingConfig(String fileName, String id) {
        this.fileName = fileName;
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public Path toPath() {
        return Paths.get(fileName);
    }
}
