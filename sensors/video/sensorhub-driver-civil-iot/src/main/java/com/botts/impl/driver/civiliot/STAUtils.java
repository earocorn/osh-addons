/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 The Initial Developer is Botts Innovative Research Inc. Portions created by the Initial
 Developer are Copyright (C) 2025 the Initial Developer. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/

package com.botts.impl.driver.civiliot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import net.opengis.gml.v32.AbstractFeature;
import net.opengis.gml.v32.AbstractGeometry;
import net.opengis.gml.v32.impl.GMLFactory;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.vast.ogc.gml.GenericFeature;
import org.vast.ogc.gml.GenericFeatureImpl;
import org.vast.ogc.om.SamplingCurve;
import org.vast.ogc.om.SamplingFeature;
import org.vast.ogc.om.SamplingPoint;
import org.vast.ogc.om.SamplingSurface;
import org.vast.swe.SWEConstants;
import org.vast.util.Asserts;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.List;

public class STAUtils {
    public static final String SENSOR_THINGS_PREFIX = "urn:osh:sta:";
    public static final String THING_UID_PREFIX = SENSOR_THINGS_PREFIX + "thing:";
    public static final String SENSOR_UID_PREFIX = SENSOR_THINGS_PREFIX + "sensor:";
    public static final String FEATURE_UID_PREFIX = SENSOR_THINGS_PREFIX + "feature:";
    public static final String VND_GEOJSON_FORMAT = "application/vnd.geo+json";
    public static final String GEOJSON_FORMAT = "application/geo+json";

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
        else if (geojson instanceof org.geojson.Feature)
        {
            SamplingFeature<?> sf = new SamplingFeature<>();
            sf.setGeometry(toGmlGeometry(geojson));
            return sf;
        }
        else
            throw new IllegalArgumentException("Unsupported geometry: " + geojson.getClass().getSimpleName());
    }

    public static AbstractFeature toGmlFeature(Location location, String uid)
    {
        GenericFeature f;
        f = new GenericFeatureImpl(new QName("Location"));
        f.setUniqueIdentifier(FEATURE_UID_PREFIX + uid);
        f.setName(location.getName());
        f.setDescription(location.getDescription());

        if(GEOJSON_FORMAT.equals(location.getEncodingType()) || VND_GEOJSON_FORMAT.equals(location.getEncodingType())) {
            GeoJsonObject geojson = (GeoJsonObject) location.getLocation();

            if (geojson != null)
                f.setGeometry(toGmlGeometry(geojson));
        } else {
//            var featureProperties = new AbstractGeometryImpl();

            if(location.getLocation() instanceof ObjectNode) {
                var fields = ((ObjectNode) location.getLocation()).fields();
                while(fields.hasNext()) {
                    var field = fields.next();
                    f.setProperty(new QName(field.getKey()), fromJsonNode(field.getValue()));
                }
            }

            f.setGeometry(null);
        }

        return f;
    }

    private static Serializable fromJsonNode(JsonNode node) {
        switch (node.getNodeType()) {
            case NUMBER:
                return node.asDouble();
            case STRING:
                return node.asText();
            case BOOLEAN:
                return node.asBoolean();
        }

        return null;
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
            Polygon polygon = (Polygon) geojson;
            var p = fac.newPolygon();
            // TODO: Translate polygon
            return p;
        }
        else if (geojson instanceof org.geojson.Feature)
        {
            if (!(((Feature) geojson).getGeometry() instanceof Feature))
                return toGmlGeometry(((Feature) geojson).getGeometry());
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
