package com.alexalmanza.impl.datastore.mongodb.feature;

import com.alexalmanza.impl.datastore.mongodb.MongoDataStoreInfo;
import com.mongodb.DBObject;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.BSONObject;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.sensorhub.api.datastore.IdProvider;
import org.sensorhub.api.datastore.feature.FeatureFilterBase;
import org.sensorhub.api.datastore.feature.IFeatureStoreBase;
import org.vast.ogc.gml.IFeature;

import java.util.Map;
import java.util.Set;

public class MongoBaseFeatureStoreImpl<V extends IFeature, VF extends FeatureField, F extends FeatureFilterBase<? super V>> implements IFeatureStoreBase<V, VF, F> {

    private static final String FEATURE_IDS_MAP_NAME = "feature_ids";
    private static final String FEATURE_UIDS_MAP_NAME = "feature_uids";
    private static final String FEATURE_RECORDS_MAP_NAME = "feature_records";
    private static final String FEATURE_SPATIAL_INDEX_MAP_NAME = "feature_geom";
    private static final String FEATURE_FULLTEXT_MAP_NAME = "feature_text";

    protected MongoDatabase mongoDatabase;
    protected MongoDataStoreInfo dataStoreInfo;
    protected IdProvider<V> idProvider;
    protected int idScope;

    // Main collection holding feature objects
    protected MongoCollection<>

    public MongoBaseFeatureStoreImpl() {

        CodecRegistries.
    }

}
