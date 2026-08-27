package org.sensorhub.process.geoloc;

import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.Quantity;
import net.opengis.swe.v20.Vector;
import org.sensorhub.algo.geoloc.Ellipsoid;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessException;
import org.vast.swe.helper.GeoPosHelper;

/**
 * Intersects two or three horizontal rays in a local tangent plane.
 *
 * <p>The local east/north approximation uses the WGS84 radii of curvature at
 * the mean sensor latitude. It is intended for sensor separations and target
 * ranges below about 10 km. With three rays, the returned point is the
 * least-squares point having the smallest sum of squared perpendicular
 * distances to the rays.</p>
 */
public class RayIntersection extends ExecutableProcessImpl {

    public static final OSHProcessInfo INFO = new OSHProcessInfo(
            "geoloc:RayIntersection",
            "Ray Intersection",
            "Compute the local 2D least-squares intersection of two or three locations with heading",
            RayIntersection.class);

    private static final double SINGULARITY_TOLERANCE = 1e-12;
    private static final double FORWARD_TOLERANCE_METERS = 1e-6;

    protected Vector llaOrigin1;
    protected Quantity azimuth1;
    protected Vector llaOrigin2;
    protected Quantity azimuth2;
    protected Vector llaOrigin3;
    protected Quantity azimuth3;

    protected Vector intersection;

    public RayIntersection() {
        super(INFO);

        var fac = new GeoPosHelper();

        inputData.add("llaOrigin1", llaOrigin1 = fac.createLocationVectorLLA().build());

        azimuth1 = fac.createQuantity()
                .definition(GeoPosHelper.DEF_AZIMUTH_ANGLE)
                .label("Azimuth Angle")
                .description("Ray azimuth from true north, measured clockwise")
                .uomCode("deg")
                .axisId("Z")
                .build();
        inputData.add("azimuth1", azimuth1);

        inputData.add("llaOrigin2", llaOrigin2 = (Vector) llaOrigin1.clone());
        azimuth2 = (Quantity) azimuth1.clone();
        inputData.add("azimuth2", azimuth2);

        inputData.add("llaOrigin3", llaOrigin3 = (Vector) llaOrigin1.clone());
        azimuth3 = (Quantity) azimuth1.clone();
        inputData.add("azimuth3", azimuth3);

        outputData.add("intersection", intersection = fac.newLocationVectorLLA("Intersection"));
    }

    @Override
    protected void initPortData(DataComponent port) {
        // ExecutableProcessImpl normally allocates every input. Leaving these
        // two unallocated lets hasData() represent an omitted optional ray.
        if (port != llaOrigin3 && port != azimuth3)
            super.initPortData(port);
    }

    @Override
    public void execute() throws ProcessException {
        // Do not expose a previous result after a failed execution.
        intersection.clearData();

        try {
            var lla1 = llaOrigin1.getData();
            var lla2 = llaOrigin2.getData();

            boolean hasLocation3 = llaOrigin3.hasData();
            boolean hasAzimuth3 = azimuth3.hasData();
            if (hasLocation3 != hasAzimuth3)
                throw new IllegalArgumentException("The third location and azimuth must be provided together");

            double[] result;
            double altitude;
            if (hasLocation3) {
                var lla3 = llaOrigin3.getData();
                result = computeIntersection(
                        new double[] {lla1.getDoubleValue(0), lla2.getDoubleValue(0), lla3.getDoubleValue(0)},
                        new double[] {lla1.getDoubleValue(1), lla2.getDoubleValue(1), lla3.getDoubleValue(1)},
                        new double[] {azimuth1.getData().getDoubleValue(),
                                azimuth2.getData().getDoubleValue(),
                                azimuth3.getData().getDoubleValue()});
                altitude = (lla1.getDoubleValue(2) + lla2.getDoubleValue(2) + lla3.getDoubleValue(2)) / 3.0;
            } else {
                result = computeIntersection(
                        lla1.getDoubleValue(0), lla1.getDoubleValue(1), azimuth1.getData().getDoubleValue(),
                        lla2.getDoubleValue(0), lla2.getDoubleValue(1), azimuth2.getData().getDoubleValue());
                altitude = (lla1.getDoubleValue(2) + lla2.getDoubleValue(2)) / 2.0;
            }

            var data = intersection.createDataBlock();
            data.setDoubleValue(0, result[0]); // latitude
            data.setDoubleValue(1, result[1]); // longitude
            data.setDoubleValue(2, altitude);
            intersection.setData(data);
            getLogger().debug("Found intersection at {} {} {}", result[0], result[1], altitude);
        } catch (IllegalArgumentException e) {
            getLogger().debug("Intersection not found: {}", e.getMessage());
        }
    }

    @Override
    protected void publishData() throws InterruptedException {
        // SensorML process chains use DataQueue, which cannot publish an empty
        // component. No intersection means no output sample for this execution.
        if (intersection.hasData())
            super.publishData();
    }

    /**
     * Computes the forward intersection of two rays.
     *
     * @return latitude and longitude in degrees, in that order
     * @throws IllegalArgumentException if the geometry is singular or the
     *         fitted intersection is behind either ray
     */
    public static double[] computeIntersection(
            double lat1, double lon1, double az1,
            double lat2, double lon2, double az2) {
        return computeIntersection(
                new double[] {lat1, lat2},
                new double[] {lon1, lon2},
                new double[] {az1, az2});
    }

    /**
     * Computes the least-squares forward intersection of two or three rays.
     * All coordinates and headings are in degrees.
     *
     * @return latitude and longitude in degrees, in that order
     */
    public static double[] computeIntersection(double[] latitudes, double[] longitudes, double[] azimuths) {
        if (latitudes == null || longitudes == null || azimuths == null ||
                latitudes.length != longitudes.length || latitudes.length != azimuths.length ||
                (latitudes.length != 2 && latitudes.length != 3)) {
            throw new IllegalArgumentException("Exactly two or three complete rays are required");
        }

        int rayCount = latitudes.length;
        double referenceLatDeg = 0.0;
        double referenceLonDeg = 0.0;
        double[] unwrappedLongitudes = new double[rayCount];
        double baseLongitude = normalizeLongitude(longitudes[0]);
        for (int i = 0; i < rayCount; i++) {
            if (!Double.isFinite(latitudes[i]) || !Double.isFinite(longitudes[i]) ||
                    !Double.isFinite(azimuths[i])) {
                throw new IllegalArgumentException("Ray coordinates and azimuths must be finite");
            }
            if (latitudes[i] < -90.0 || latitudes[i] > 90.0)
                throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
            // Keep nearby sensors nearby even if the area crosses the antimeridian.
            unwrappedLongitudes[i] = baseLongitude + normalizeLongitude(longitudes[i] - baseLongitude);
            referenceLatDeg += latitudes[i];
            referenceLonDeg += unwrappedLongitudes[i];
        }
        referenceLatDeg /= rayCount;
        referenceLonDeg /= rayCount;

        double referenceLat = Math.toRadians(referenceLatDeg);
        double sinLat = Math.sin(referenceLat);
        double e2 = Ellipsoid.WGS84.getE2();
        double a = Ellipsoid.WGS84.getEquatorRadius();
        double w = Math.sqrt(1.0 - e2 * sinLat * sinLat);
        double meridionalRadius = a * (1.0 - e2) / (w * w * w);
        double primeVerticalRadius = a / w;
        double metersPerLatRadian = meridionalRadius;
        double metersPerLonRadian = primeVerticalRadius * Math.cos(referenceLat);
        if (Math.abs(metersPerLonRadian) < 1e-9)
            throw new IllegalArgumentException("Ray intersection is undefined at the poles");

        double[] east = new double[rayCount];
        double[] north = new double[rayCount];
        double[] dirEast = new double[rayCount];
        double[] dirNorth = new double[rayCount];

        // Normal equations for the perpendicular distance from x to every line.
        double a00 = 0.0;
        double a01 = 0.0;
        double a11 = 0.0;
        double b0 = 0.0;
        double b1 = 0.0;
        for (int i = 0; i < rayCount; i++) {
            east[i] = Math.toRadians(unwrappedLongitudes[i] - referenceLonDeg) * metersPerLonRadian;
            north[i] = Math.toRadians(latitudes[i] - referenceLatDeg) * metersPerLatRadian;

            double azimuth = Math.toRadians(azimuths[i]);
            dirEast[i] = Math.sin(azimuth);
            dirNorth[i] = Math.cos(azimuth);

            double m00 = dirNorth[i] * dirNorth[i];
            double m01 = -dirEast[i] * dirNorth[i];
            double m11 = dirEast[i] * dirEast[i];
            a00 += m00;
            a01 += m01;
            a11 += m11;
            b0 += m00 * east[i] + m01 * north[i];
            b1 += m01 * east[i] + m11 * north[i];
        }

        double determinant = a00 * a11 - a01 * a01;
        double trace = a00 + a11;
        if (determinant <= SINGULARITY_TOLERANCE * trace * trace)
            throw new IllegalArgumentException("Rays are parallel or nearly parallel");

        double intersectionEast = (a11 * b0 - a01 * b1) / determinant;
        double intersectionNorth = (a00 * b1 - a01 * b0) / determinant;

        for (int i = 0; i < rayCount; i++) {
            double alongRay = (intersectionEast - east[i]) * dirEast[i] +
                    (intersectionNorth - north[i]) * dirNorth[i];
            if (alongRay < -FORWARD_TOLERANCE_METERS)
                throw new IllegalArgumentException("Intersection lies behind ray " + (i + 1));
        }

        double latitude = referenceLatDeg + Math.toDegrees(intersectionNorth / metersPerLatRadian);
        double longitude = normalizeLongitude(
                referenceLonDeg + Math.toDegrees(intersectionEast / metersPerLonRadian));
        return new double[] {latitude, longitude};
    }

    private static double normalizeLongitude(double longitude) {
        longitude %= 360.0;
        if (longitude >= 180.0)
            longitude -= 360.0;
        else if (longitude < -180.0)
            longitude += 360.0;
        return longitude;
    }
}
