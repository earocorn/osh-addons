/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2025 Botts Innovative Research, Inc. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.process.geoloc;

import net.opengis.swe.v20.DataBlock;
import org.junit.Test;
import org.sensorhub.algo.vecmath.Vect3d;
import org.vast.process.DataQueue;
import org.vast.process.ProcessException;

import static org.junit.Assert.*;

public class TestRayIntersectionProcess {

    private static final double TARGET_LAT = 34.7000;
    private static final double TARGET_LON = -86.7500;

    private RayIntersection createProcess() throws ProcessException {
        RayIntersection process = new RayIntersection();
        process.init();
        return process;
    }

    private DataBlock execProcess(RayIntersection process,
            Vect3d lla1, double az1, Vect3d lla2, double az2, Vect3d lla3, Double az3)
            throws ProcessException {
        setVector(process, "llaOrigin1", lla1);
        setQuantity(process, "azimuth1", az1);
        setVector(process, "llaOrigin2", lla2);
        setQuantity(process, "azimuth2", az2);

        if (lla3 != null && az3 != null) {
            setVector(process, "llaOrigin3", lla3);
            setQuantity(process, "azimuth3", az3);
        } else {
            process.getInputList().getComponent("llaOrigin3").clearData();
            process.getInputList().getComponent("azimuth3").clearData();
        }

        process.execute();
        return process.getOutputList().getComponent("intersection").hasData()
                ? process.getOutputList().getComponent("intersection").getData() : null;
    }

    private void setVector(RayIntersection process, String name, Vect3d lla) {
        var component = process.getInputList().getComponent(name);
        var data = component.createDataBlock();
        data.setDoubleValue(0, lla.y); // latitude
        data.setDoubleValue(1, lla.x); // longitude
        data.setDoubleValue(2, lla.z);
        component.setData(data);
    }

    private void setQuantity(RayIntersection process, String name, double value) {
        var component = process.getInputList().getComponent(name);
        var data = component.createDataBlock();
        data.setDoubleValue(value);
        component.setData(data);
    }

    @Test
    public void testTwoRayProcessReturnsCorrectCoordinateOrderAndAverageAltitude() throws Exception {
        RayIntersection process = createProcess();
        Vect3d south = new Vect3d(TARGET_LON, TARGET_LAT - 0.01, 100.0);
        Vect3d west = new Vect3d(TARGET_LON - 0.01, TARGET_LAT, 200.0);

        DataBlock result = execProcess(process, south, 0.0, west, 90.0, null, null);

        assertNotNull(result);
        assertEquals(TARGET_LAT, result.getDoubleValue(0), 1e-9);
        assertEquals(TARGET_LON, result.getDoubleValue(1), 1e-9);
        assertEquals(150.0, result.getDoubleValue(2), 1e-9);
    }

    @Test
    public void testThreeRayLeastSquaresIntersectionWithNoisyHeading() throws Exception {
        RayIntersection process = createProcess();
        Vect3d south = new Vect3d(TARGET_LON, TARGET_LAT - 0.01, 100.0);
        Vect3d west = new Vect3d(TARGET_LON - 0.01, TARGET_LAT, 200.0);
        Vect3d northeast = new Vect3d(TARGET_LON + 0.007, TARGET_LAT + 0.007, 300.0);
        double noisyHeading = bearingTo(northeast.y, northeast.x, TARGET_LAT, TARGET_LON) + 0.2;

        DataBlock result = execProcess(process, south, 0.0, west, 90.0, northeast, noisyHeading);

        assertNotNull(result);
        assertEquals(TARGET_LAT, result.getDoubleValue(0), 2e-5);
        assertEquals(TARGET_LON, result.getDoubleValue(1), 2e-5);
        assertEquals(200.0, result.getDoubleValue(2), 1e-9);
    }

    @Test
    public void testParallelRaysClearPreviousOutput() throws Exception {
        RayIntersection process = createProcess();
        Vect3d south = new Vect3d(TARGET_LON, TARGET_LAT - 0.01, 100.0);
        Vect3d west = new Vect3d(TARGET_LON - 0.01, TARGET_LAT, 200.0);

        assertNotNull(execProcess(process, south, 0.0, west, 90.0, null, null));
        assertNull(execProcess(process, south, 0.0, west, 0.0, null, null));
    }

    @Test
    public void testEmptyIntersectionIsNotPublishedToProcessQueue() throws Exception {
        RayIntersection process = new RayIntersection();
        DataQueue outputQueue = new DataQueue();
        process.connect(process.getOutputList().getComponent("intersection"), outputQueue);
        process.init();

        Vect3d south = new Vect3d(TARGET_LON, TARGET_LAT - 0.01, 100.0);
        Vect3d west = new Vect3d(TARGET_LON - 0.01, TARGET_LAT, 200.0);
        setVector(process, "llaOrigin1", south);
        setQuantity(process, "azimuth1", 0.0);
        setVector(process, "llaOrigin2", west);
        setQuantity(process, "azimuth2", 0.0);

        process.run();

        assertEquals(0, outputQueue.getQueueSize());
    }

    @Test
    public void testValidIntersectionIsPublishedToProcessQueue() throws Exception {
        RayIntersection process = new RayIntersection();
        DataQueue outputQueue = new DataQueue();
        process.connect(process.getOutputList().getComponent("intersection"), outputQueue);
        process.init();

        Vect3d south = new Vect3d(TARGET_LON, TARGET_LAT - 0.01, 100.0);
        Vect3d west = new Vect3d(TARGET_LON - 0.01, TARGET_LAT, 200.0);
        setVector(process, "llaOrigin1", south);
        setQuantity(process, "azimuth1", 0.0);
        setVector(process, "llaOrigin2", west);
        setQuantity(process, "azimuth2", 90.0);

        process.run();

        assertEquals(1, outputQueue.getQueueSize());
    }

    @Test
    public void testIntersectionBehindRayIsRejected() {
        try {
            RayIntersection.computeIntersection(
                    TARGET_LAT - 0.01, TARGET_LON, 180.0,
                    TARGET_LAT, TARGET_LON - 0.01, 90.0);
            fail("Expected an intersection behind the first ray to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("behind ray"));
        }
    }

    @Test
    public void testParallelStaticIntersectionIsRejected() {
        try {
            RayIntersection.computeIntersection(
                    TARGET_LAT - 0.01, TARGET_LON, 0.0,
                    TARGET_LAT, TARGET_LON - 0.01, 0.0);
            fail("Expected parallel rays to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("parallel"));
        }
    }

    @Test
    public void testSmallAreaAcrossAntimeridian() {
        double[] result = RayIntersection.computeIntersection(
                -0.01, -179.999, 0.0,
                0.0, 179.991, 90.0);

        assertEquals(0.0, result[0], 1e-9);
        assertEquals(-179.999, result[1], 1e-9);
    }

    @Test
    public void testThirdRayInputsMustBeProvidedTogether() throws Exception {
        RayIntersection process = createProcess();
        Vect3d south = new Vect3d(TARGET_LON, TARGET_LAT - 0.01, 100.0);
        Vect3d west = new Vect3d(TARGET_LON - 0.01, TARGET_LAT, 200.0);
        setVector(process, "llaOrigin1", south);
        setQuantity(process, "azimuth1", 0.0);
        setVector(process, "llaOrigin2", west);
        setQuantity(process, "azimuth2", 90.0);
        setVector(process, "llaOrigin3", new Vect3d(TARGET_LON + 0.01, TARGET_LAT, 300.0));

        process.execute();

        assertFalse(process.getOutputList().getComponent("intersection").hasData());
    }

    private double bearingTo(double fromLat, double fromLon, double toLat, double toLon) {
        double north = toLat - fromLat;
        double east = (toLon - fromLon) * Math.cos(Math.toRadians((fromLat + toLat) / 2.0));
        return Math.toDegrees(Math.atan2(east, north));
    }
}
