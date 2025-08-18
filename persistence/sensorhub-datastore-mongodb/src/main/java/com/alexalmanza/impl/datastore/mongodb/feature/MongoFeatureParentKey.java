package com.alexalmanza.impl.datastore.mongodb.feature;

import org.sensorhub.api.common.BigId;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.vast.util.Asserts;

import java.time.Instant;

public class MongoFeatureParentKey extends FeatureKey {
    protected long parentID;

    public MongoFeatureParentKey(long parentID, BigId internalID, Instant validStartTime)
    {
        super(internalID, validStartTime);

        Asserts.checkArgument(parentID >= 0, "Invalid parentID");
        this.parentID = parentID;
    }


    public MongoFeatureParentKey(int idScope, long parentID, long internalID, Instant validStartTime)
    {
        super(idScope, internalID, validStartTime);

        Asserts.checkArgument(parentID >= 0, "Invalid parentID");
        this.parentID = parentID;
    }


    public long getParentID()
    {
        return parentID;
    }
}
