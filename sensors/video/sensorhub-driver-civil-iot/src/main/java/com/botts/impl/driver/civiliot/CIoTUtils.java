package com.botts.impl.driver.civiliot;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CIoTUtils {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter headerDateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZoneId TAIWAN = ZoneId.of("UTC+8");
    private static final String AIR_PREFIX = "/AirSitePic";

    public static boolean isTimestampedURL(URL url) throws IOException {
        // If more image repositories are discovered, change this to something more robust
        return url.getPath().contains(AIR_PREFIX);
    }

    public static long getImageTimestampUTC(URL url) throws IOException, ParseException {
        if (isTimestampedURL(url)) {
            String path = url.getPath();
            String[] parts = path.split("/");

            if (parts.length < 4)
                throw new IllegalArgumentException("URL path does not contain expected parts.");

            String datetimeString = parts[3].substring(4).split("\\.")[0]; // Extract only digits
            LocalDateTime datetime = LocalDateTime.parse(datetimeString, dateTimeFormat);
            return datetime.toInstant(ZoneOffset.UTC).toEpochMilli();
        } else {
            String utcString = url.openConnection().getHeaderField("imageutc").split("\\+")[0].trim();
            LocalDateTime utcDatetime = LocalDateTime.parse(utcString, headerDateTimeFormat);
            return utcDatetime.toInstant(ZoneOffset.UTC).toEpochMilli();
        }
    }

    public static URL getLatestURL(URL initialURL) throws IOException {
        return getLatestURL(0, initialURL);
    }

    private static URL getLatestURL(int minuteOffset, URL initialURL) throws IOException {
        if (!isTimestampedURL(initialURL))
            return initialURL;

        ZonedDateTime now = ZonedDateTime.now(TAIWAN);

        int minute = now.getMinute();
        int roundedMinute = (minute/10) * 10;

        ZonedDateTime roundedTime = now.withMinute(roundedMinute).withSecond(0).withNano(0).plusMinutes(minuteOffset);

        String dateString = dateFormat.format(roundedTime);
        String datetimeString = dateTimeFormat.format(roundedTime);

        String baseURL = initialURL.toString();
        int prefixEnd = baseURL.indexOf(AIR_PREFIX);
        if (prefixEnd == -1)
            throw new IOException("Initial URL is not valid");

        String id = baseURL.split("/")[5].substring(0, 3);
        String prefix = baseURL.substring(0, prefixEnd);
        String newPath = String.format("%s/%s/%s-%s.jpg", AIR_PREFIX, dateString, id, datetimeString);

        URL url = new URL(prefix + newPath);
        try {
            ImageURLUtils.getBytes(url);
        } catch (IOException e) {
            return getLatestURL(minuteOffset - 10, initialURL);
        }

        return url;
    }

}
