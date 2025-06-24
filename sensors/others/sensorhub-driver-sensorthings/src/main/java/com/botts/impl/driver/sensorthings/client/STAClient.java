package com.botts.impl.driver.sensorthings.client;

import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;

import java.net.http.HttpClient;

public class STAClient {
    SensorThingsService service;
    HttpClient httpClient;

    public STAClient(SensorThingsService service) {
        this.service = service;
    }

    public SensorThingsService getService() {
        return service;
    }

}
