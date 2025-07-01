package com.botts.test.impl.driver.civiliot;

import com.botts.impl.driver.civiliot.CIoTUtils;
import com.botts.impl.driver.civiliot.ImageURLUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.common.SensorHubException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

public class UtilsTests {

    URL timestampURL = new URL("https://airtw.moenv.gov.tw/AirSitePic/20240702/001-202407021500.jpg");
    URL uuidURL = new URL("https://iapi.wra.gov.tw/v3/api/Image/3e900c38-c2dd-440c-91d2-023a0c4a8aad");
    URL testURL = new URL("https://iapi.wra.gov.tw/v3/api/Image/19e35ba3-22b0-4a03-a5ae-974947e9b459");

    public UtilsTests() throws MalformedURLException {
    }

    @Before
    public void setup() throws SensorHubException, InterruptedException {
        // For testing only pollers
    }


    @Test
    public void printImageInfo() throws IOException {
        URL url = testURL;
        BufferedImage image = ImageIO.read(url);
        System.out.println("Dimensions: " + image.getWidth() + "x" + image.getHeight());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "JPG", baos);
        byte[] imageBytes = baos.toByteArray();
        System.out.println(imageBytes.length);
    }

    @Test
    public void urlsHaveValidDimensions() throws IOException, SensorHubException, ParseException {
        ImageURLUtils.getBytes(timestampURL);
        ImageURLUtils.getBytes(uuidURL);
        ImageURLUtils.getBytes(testURL);
        ImageURLUtils.getDimensions(timestampURL);
        ImageURLUtils.getDimensions(uuidURL);
        ImageURLUtils.getDimensions(testURL);
    }

    @Test
    public void getTimestamps() throws IOException, ParseException {
        Assert.assertTrue(CIoTUtils.getImageTimestampUTC(timestampURL) > 1700000000);
        Assert.assertTrue(CIoTUtils.getImageTimestampUTC(uuidURL) > 1700000000);
    }

    @Test
    public void getLatestURL() throws IOException, ParseException {
        Assert.assertNotNull(CIoTUtils.getLatestURL(timestampURL));
        Assert.assertNotNull(CIoTUtils.getLatestURL(uuidURL));
    }


}
