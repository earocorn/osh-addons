package com.botts.impl.driver.civiliot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class ImageURLUtils {

    public static int[] getDimensions(URL url) throws IOException {
        BufferedImage image = ImageIO.read(url);
        int width = image.getWidth();
        int height = image.getHeight();
        return new int[]{width, height};
    }

    public static byte[] getBytes(URL url) throws IOException {
        BufferedImage image = ImageIO.read(url);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "JPG", baos);
        return baos.toByteArray();
    }



}
