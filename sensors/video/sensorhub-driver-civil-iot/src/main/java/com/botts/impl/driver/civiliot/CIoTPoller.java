package com.botts.impl.driver.civiliot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.ogc.gml.IFeature;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CIoTPoller  {

    private static final Logger logger = LoggerFactory.getLogger(VideoOutput.class.getSimpleName());

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter headerDateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZoneId TAIWAN = ZoneId.of("UTC+8");
    private static final String AIR_PREFIX = "/AirSitePic";

    private final boolean urlHasTimestamp;
    private final URL initialURL;
    private final int pollInterval;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pollTask;
    private volatile byte[] latestBuffer;
    private final VideoOutput<?> output;
    private IFeature foi;

    public CIoTPoller(URL initialURL, int pollInterval, VideoOutput<?> output) {
        this.initialURL = initialURL;
        this.pollInterval = pollInterval;
        this.urlHasTimestamp = initialURL.getPath().contains(AIR_PREFIX);
        this.output = output;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    // TODO: For testing
    public CIoTPoller(URL initialURL, int pollInterval) {
        this.initialURL = initialURL;
        this.pollInterval = pollInterval;
        this.urlHasTimestamp = initialURL.getPath().contains(AIR_PREFIX);
        this.output = null;
    }

    public void setFoi(IFeature foi) {
        this.foi = foi;
    }

    public void start() {
        pollTask = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[Civil IoT] Polling for " + output.getName());
            try {
                URL currentURL = getLatestURL();
                byte[] currentBuffer = ImageURLUtils.getBytes(currentURL);
                long samplingTime = getImageTimestampUTC(currentURL);
                // TODO Change back
//                if (latestBuffer == null || !Arrays.equals(latestBuffer, currentBuffer))
                    output.processBuffer(currentBuffer, samplingTime, foi != null ? foi.getUniqueIdentifier() : null);
                latestBuffer = currentBuffer;
            } catch (IOException e) {
                System.out.println("Error renewing URL or getting buffer " + e.getMessage());
            } catch (ParseException e) {
                System.out.println("Error getting timestamp from URL {}" + e.getMessage());
            }
            // TODO CHANGE TO MINUTES
        }, 0, pollInterval, TimeUnit.SECONDS);
    }

    public void stop() {
        // Cancel current task if needed
        if (pollTask != null && !pollTask.isCancelled())
            pollTask.cancel(true);
    }

    public void cleanup() {
        // Shutdown scheduler
        if (scheduler != null && !scheduler.isShutdown())
            scheduler.shutdownNow();
    }

    public long getImageTimestampUTC(URL url) throws IOException, ParseException {
        if (urlHasTimestamp) {
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

    public URL getLatestURL() throws IOException {
       return getLatestURL(0);
    }

    public URL getLatestURL(int minuteOffset) throws IOException {
        if (!urlHasTimestamp)
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
            return getLatestURL(minuteOffset - 10);
        }

        return url;
    }

}
