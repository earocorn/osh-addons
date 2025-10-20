package org.sensorhub.process.geoloc;

import net.opengis.swe.v20.DataBlock;
import org.junit.Test;
import org.sensorhub.algo.vecmath.Vect3d;
import org.sensorhub.process.vecmath.VecMathHelper;
import org.vast.process.ProcessException;
import org.vast.sensorML.SMLUtils;
import org.vast.sensorML.SimpleProcessImpl;
import org.vast.xml.XMLWriterException;

import static org.junit.Assert.*;

public class TestRayIntersectionProcess {

    private RayIntersection createProcess() throws ProcessException, XMLWriterException {
        RayIntersection p = new RayIntersection();

        SimpleProcessImpl wp = new SimpleProcessImpl();
        wp.setExecutableImpl(p);
        new SMLUtils(SMLUtils.V2_0).writeProcess(System.out, wp, true);

        return p;
    }

    private DataBlock execProcess(RayIntersection p, Vect3d lla1, double az1, Vect3d lla2, double az2) throws ProcessException {
        var llaOrigin1 = p.getInputList().getComponent("llaOrigin1");
        var llaData1 = llaOrigin1.createDataBlock();
        llaOrigin1.setData(llaData1);
        var azimuth1 = p.getInputList().getComponent("azimuth1");
        var azData1 = azimuth1.createDataBlock();
        azimuth1.setData(azData1);
        var llaOrigin2 = p.getInputList().getComponent("llaOrigin2");
        var llaData2 = llaOrigin2.createDataBlock();
        llaOrigin2.setData(llaData2);
        var azimuth2 = p.getInputList().getComponent("azimuth2");
        var azData2 = azimuth2.createDataBlock();
        azimuth2.setData(azData2);

        llaData1.setDoubleValue(0, lla1.y);
        llaData1.setDoubleValue(1, lla1.x);
        llaData1.setDoubleValue(2, lla1.z);
        azData1.setDoubleValue(az1);

        llaData2.setDoubleValue(0, lla2.y);
        llaData2.setDoubleValue(1, lla2.x);
        llaData2.setDoubleValue(2, lla2.z);
        azData2.setDoubleValue(az2);

        p.execute();


        return p.getOutputList().getComponent(0).hasData() ? p.getOutputList().getComponent(0).getData() : null;
    }



    @Test
    public void testIntersect() throws Exception {
        RayIntersection p = createProcess();
        DataBlock intersect;

        double az1 = 0.0;
        double az2 = 0.0;
        Vect3d lla1 = new Vect3d(-86.74447699999999, 34.68304916843915, 190);
        Vect3d lla2 = new Vect3d(-86.78415774416187, 34.73930122239332, 190);

        intersect = execProcess(p, lla1, az1, lla2, az2);

        assertNull(intersect);

        az1 = 0.0;
        az2 = 90.0;

        intersect = execProcess(p, lla1, az1, lla2, az2);

        assertNotNull(intersect);
        var lat = intersect.getDoubleValue(0);
        var lon = intersect.getDoubleValue(1);
        var alt = intersect.getDoubleValue(2);
        System.out.printf("Lat: %s, Lon: %s, Alt: %s", lat, lon, alt);
    }

}
