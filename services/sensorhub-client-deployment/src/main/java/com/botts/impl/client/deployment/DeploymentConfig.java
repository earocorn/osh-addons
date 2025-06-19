package com.botts.impl.client.deployment;

import org.sensorhub.api.config.DisplayInfo;

public class DeploymentConfig {

    @DisplayInfo.Required
    @DisplayInfo(label = "Deployment Name")
    public String name;

    @DisplayInfo.Required
    @DisplayInfo(label = "Deployment ID")
    public String id;

    @DisplayInfo(label = "Deployment Description")
    public String description;

}
