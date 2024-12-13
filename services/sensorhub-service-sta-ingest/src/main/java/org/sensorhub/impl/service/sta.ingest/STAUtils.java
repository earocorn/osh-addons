package org.sensorhub.impl.service.sta.ingest;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.*;
import de.fraunhofer.iosb.ilt.sta.model.builder.FeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import net.opengis.gml.v32.AbstractFeature;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.gml.v32.impl.GMLFactory;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.sensorml.v20.DocumentList;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataRecord;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.isotc211.v2005.gmd.CIOnlineResource;
import org.isotc211.v2005.gmd.impl.GMDFactory;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.api.data.IObsData;
import org.sensorhub.api.data.ObsData;
import org.sensorhub.api.datastore.feature.FeatureKey;
import org.sensorhub.api.feature.FeatureId;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.system.wrapper.SystemWrapper;
import org.sensorhub.utils.SWEDataUtils;
import org.vast.data.*;
import org.vast.ogc.gml.GenericFeatureImpl;
import org.vast.ogc.om.IObservation;
import org.vast.ogc.om.SamplingCurve;
import org.vast.ogc.om.SamplingPoint;
import org.vast.ogc.om.SamplingSurface;
import org.vast.sensorML.SMLFactory;
import org.vast.sensorML.SMLHelper;
import org.vast.swe.SWEBuilders;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.SWEUtils;
import org.vast.util.Asserts;

import javax.xml.namespace.QName;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class STAUtils {

    SWEHelper fac;
    final String tempUidPrefix = "urn:osh:sta:";
    static final String GEOJSON_FORMAT = "application/vnd.geo+json";
    static final String UCUM_URI_PREFIX = "http://unitsofmeasure.org/ucum.html#";

    public STAUtils()
    {
        fac = new SWEHelper();
    }

    public AbstractProcess toSmlProcess(Sensor sensor)
    {
        String uid = /*tempUidPrefix + */SWEDataUtils.toNCName(sensor.getName()) + ":" + sensor.getId();

        var sys = new SMLHelper().createPhysicalSystem()
                .uniqueID(uid)
                .name(sensor.getName())
                .description(sensor.getDescription())
                .validFrom(OffsetDateTime.now())
                .build();

        if(sensor.getMetadata() instanceof String)
        {
            CIOnlineResource doc = new GMDFactory().newCIOnlineResource();
            doc.setProtocol(sensor.getEncodingType());
            doc.setLinkage((String)sensor.getMetadata());

            DocumentList docList = new SMLFactory().newDocumentList();
            docList.addDocument(doc);
            sys.getDocumentationList().add("sta_metadata", docList);
        }

        return sys;
    }

    public AbstractProcess toSmlProcess(Thing thing)
    {
        String uid = /*tempUidPrefix + */SWEDataUtils.toNCName(thing.getName()) + ":" + thing.getId();

        var sys = new SMLHelper().createPhysicalSystem()
                .uniqueID(uid)
                .name(thing.getName())
                .description(thing.getDescription())
                .validFrom(OffsetDateTime.now())
                .build();

        return sys;
    }

    protected IDataStreamInfo toSweDataStream(FeatureId systemId, Datastream ds) throws ServiceFailureException {
        var recordStruct = toSweCommon(ds);
        return new DataStreamInfo.Builder()
                .withName(ds.getName())
                .withDescription(ds.getDescription())
                .withSystem(systemId)
                .withRecordDescription(recordStruct)
                .withRecordEncoding(new TextEncodingImpl())
                .build();
    }


    protected DataRecord toSweCommon(Datastream ds) throws ServiceFailureException {
        var rec = fac.createRecord()
                .name(SWEDataUtils.toNCName(ds.getName()))
                .label(ds.getName())
                .description(ds.getDescription())
                .addField("time", fac.createTime().asPhenomenonTimeIsoUTC()
                        .label("Sampling Time"));

        ObservedProperty obsProp = ds.getObservedProperty();

        DataComponent comp = toComponent(
                ds.getObservationType(),
                obsProp,
                ds.getUnitOfMeasurement());

        rec.addField(comp.getName(), comp);

        return rec.build();
    }


    protected DataComponent toComponent(String obsType, ObservedProperty obsProp, UnitOfMeasurement uom)
    {
        SWEBuilders.DataComponentBuilder<? extends SWEBuilders.DataComponentBuilder<?,?>, ? extends DataComponent> comp = null;

        if (IObservation.OBS_TYPE_MEAS.equals(obsType))
        {
            comp = fac.createQuantity();

            if (uom.getDefinition() != null && uom.getDefinition().startsWith(UCUM_URI_PREFIX))
                ((SWEBuilders.QuantityBuilder)comp).uomCode(uom.getDefinition().replace(UCUM_URI_PREFIX, ""));
            else
                ((SWEBuilders.QuantityBuilder)comp).uomUri(uom.getDefinition());
        }
        else if (IObservation.OBS_TYPE_CATEGORY.equals(obsType))
            comp = fac.createCategory();
        else if (IObservation.OBS_TYPE_COUNT.equals(obsType))
            comp = fac.createCount();
        else if (IObservation.OBS_TYPE_RECORD.equals(obsType))
            comp = fac.createRecord();

        var definition = obsProp.getDefinition();
        try {
            URI.create(definition);
        } catch (IllegalArgumentException e) {
            definition = SWEHelper.getPropertyUri(definition.replace(" ", ""));
        }

        if (comp != null)
        {
            return comp.id(obsProp.getId().toString())
                    .name(SWEDataUtils.toNCName(obsProp.getName()))
                    .label(obsProp.getName())
                    .description(obsProp.getDescription())
                    .definition(definition)
                    .build();
        }

        return null;
    }

    public ObsData toObsData(Observation obs, BigId dsId, BigId foiId)
    {
        Instant phenomenonTime = obs.getPhenomenonTime().getAsDateTime().toInstant().truncatedTo(ChronoUnit.MILLIS);

        Instant resultTime = null;
        if(obs.getResultTime() != null)
            resultTime = obs.getResultTime().toInstant().truncatedTo(ChronoUnit.MILLIS);

        DataBlock dataBlock = createDataBlock(phenomenonTime, obs.getResult());

        return new ObsData.Builder()
                .withDataStream(dsId)
                .withFoi(foiId == null ? IObsData.NO_FOI : foiId)
                .withPhenomenonTime(phenomenonTime)
                .withResultTime(resultTime)
                .withResult(dataBlock)
                .build();
    }

    public DataBlock createDataBlock(Instant timestamp, Object val)
    {
        var timestampBlock = new DataBlockDouble(1);
        timestampBlock.setDoubleValue(timestamp.toEpochMilli() / 1000.);

        var dataBlock = createDataBlock(val);
        if(dataBlock instanceof DataBlockMixed)
        {
            ((DataBlockMixed)dataBlock).getUnderlyingObject()[0] = timestampBlock;
            return dataBlock;
        }
        else
        {
            var wrapperBlock = new DataBlockMixed(2, 2);
            wrapperBlock.getUnderlyingObject()[0] = timestampBlock;
            wrapperBlock.getUnderlyingObject()[1] = (AbstractDataBlock) dataBlock;
            return wrapperBlock;
        }
    }

    public DataBlock createDataBlock(Object val)
    {
        DataBlock dataBlock;

        if(val instanceof Integer)
        {
            dataBlock = new DataBlockInt(1);
            dataBlock.setIntValue((Integer)val);
        }
        else if (val instanceof Long)
        {
            dataBlock = new DataBlockLong(1);
            dataBlock.setLongValue((Long)val);
        }
        else if (val instanceof Number)
        {
            dataBlock = new DataBlockDouble(1);
            dataBlock.setDoubleValue(((Number)val).doubleValue());
        }
        else if (val instanceof String)
        {
            if(((String) val).isEmpty())
                dataBlock = new DataBlockString(1);
            else
                dataBlock = new DataBlockString(((String) val).length());
            dataBlock.setStringValue((String)val);
        }
        else if (val instanceof ArrayList)
        {
            var elements = (ArrayList)val;
            var numElements = elements.size();
            var blockSize = numElements + 1;
            dataBlock = new DataBlockMixed(blockSize, blockSize);
            for (int i = 0; i < numElements; i++)
            {
                var childBlock = (AbstractDataBlock)createDataBlock(elements.get(i));
                ((DataBlockMixed)dataBlock).getUnderlyingObject()[i+1] = childBlock;
            }
        }
        else
            throw new IllegalArgumentException("Unsupported result type: " + val.getClass().getSimpleName());

        return dataBlock;
    }

    protected AbstractFeature toGmlFeature(FeatureOfInterest foi, String uid)
    {
        Asserts.checkArgument(GEOJSON_FORMAT.equals(foi.getEncodingType()), "Unsupported feature format: %s", foi.getEncodingType());
        GeoJsonObject geojson = (GeoJsonObject)foi.getFeature();

        AbstractFeature f;
        if (geojson != null)
            f = toSamplingFeature(geojson);
        else
            f = new GenericFeatureImpl(new QName("Feature"));

        f.setUniqueIdentifier(uid);
        f.setName(foi.getName());
        f.setDescription(foi.getDescription());
        return f;
    }

    public static AbstractFeature toSamplingFeature(org.geojson.GeoJsonObject geojson)
    {
        if (geojson instanceof org.geojson.Feature)
            geojson = ((org.geojson.Feature)geojson).getGeometry();

        if (geojson instanceof org.geojson.Point)
        {
            SamplingPoint sf = new SamplingPoint();
            sf.setGeometry(toGmlGeometry(geojson));
            return sf;
        }
        else if (geojson instanceof org.geojson.LineString)
        {
            SamplingCurve sf = new SamplingCurve();
            sf.setGeometry(toGmlGeometry(geojson));
            return sf;
        }
        else if (geojson instanceof org.geojson.Polygon)
        {
            SamplingSurface sf = new SamplingSurface();
            sf.setGeometry(toGmlGeometry(geojson));
            return sf;
        }
        else
            throw new IllegalArgumentException("Unsupported geometry: " + geojson.getClass().getSimpleName());
    }

    public static AbstractGeometry toGmlGeometry(org.geojson.GeoJsonObject geojson)
    {
        GMLFactory fac = new GMLFactory(true);

        if (geojson instanceof org.geojson.Point)
        {
            LngLatAlt coords = ((org.geojson.Point)geojson).getCoordinates();

            var p = fac.newPoint();
            setGeomSrs(p, coords);
            p.setPos(coords.hasAltitude() ?
                    new double[] {coords.getLatitude(), coords.getLongitude(), coords.getAltitude()} :
                    new double[] {coords.getLatitude(), coords.getLongitude()});

            return p;
        }
        else if (geojson instanceof org.geojson.LineString)
        {
            var coords = ((org.geojson.LineString)geojson).getCoordinates();
            Asserts.checkArgument(coords.size() >= 2, "LineString must contain at least 2 points");

            var line = fac.newLineString();
            setGeomSrs(line, coords.get(0));
            line.setPosList(toPosList(coords, line.getSrsDimension()));

            return line;
        }
        else if (geojson instanceof org.geojson.Polygon)
        {

        }

        throw new IllegalArgumentException("Unsupported geometry: " + geojson.getClass().getSimpleName());
    }

    public static void setGeomSrs(net.opengis.gml.v32.AbstractGeometry geom, LngLatAlt lla)
    {
        if (lla.hasAltitude())
        {
            geom.setSrsDimension(3);
            geom.setSrsName(SWEConstants.REF_FRAME_4979);
        }
        else
        {
            geom.setSrsDimension(2);
            geom.setSrsName(SWEConstants.REF_FRAME_4326);
        }
    }

    public static double[] toPosList(List<LngLatAlt> coords, int numDims)
    {
        int i = 0;
        double[] posList = new double[coords.size()*numDims];

        for (LngLatAlt p: coords)
        {
            posList[i++] = p.getLatitude();
            posList[i++] = p.getLongitude();
            if (numDims == 3)
                posList[i++] = p.getAltitude();
        }

        return posList;
    }

}
