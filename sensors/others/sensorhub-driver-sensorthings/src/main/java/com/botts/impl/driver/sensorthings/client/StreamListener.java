package com.botts.impl.driver.sensorthings.client;

import net.opengis.swe.v20.DataBlock;

public interface StreamListener {

    void onDataReceived(DataBlock dataBlock);

}
