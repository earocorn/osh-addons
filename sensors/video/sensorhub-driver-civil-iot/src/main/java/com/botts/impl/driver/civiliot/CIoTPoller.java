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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CIoTPoller  {

    private static final Logger logger = LoggerFactory.getLogger(VideoOutput.class.getSimpleName());

    private final URL initialURL;
    private final long pollInterval;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> pollTask;
    private volatile byte[] latestBuffer;
    private final VideoOutput<?> output;
    private IFeature foi;

    public CIoTPoller(URL initialURL, long pollInterval, VideoOutput<?> output) {
        this.initialURL = initialURL;
        this.pollInterval = pollInterval;
        this.output = output;
    }

    public void setFoi(IFeature foi) {
        this.foi = foi;
    }

    public void start() {
        if (executor == null)
            throw new IllegalStateException("Poller not able to start without executor");
        pollTask = executor.scheduleAtFixedRate(() -> {
            System.out.println("[Civil IoT] Polling for " + output.getName());
            try {
                URL currentURL = CIoTUtils.getLatestURL(initialURL);
                byte[] currentBuffer = ImageURLUtils.getBytes(currentURL);
                long samplingTime = CIoTUtils.getImageTimestampUTC(currentURL);
                // TODO Uncomment so that we aren't outputting the same frame multiple times
//                if (latestBuffer == null || !Arrays.equals(latestBuffer, currentBuffer))
                    output.processBuffer(currentBuffer, samplingTime, foi != null ? foi.getUniqueIdentifier() : null);
                latestBuffer = currentBuffer;
            } catch (IOException e) {
                System.out.println("Error renewing URL or getting buffer " + e.getMessage());
            } catch (ParseException e) {
                System.out.println("Error getting timestamp from URL {}" + e.getMessage());
            }
        }, 0, pollInterval, TimeUnit.MINUTES);
    }

    public void stop() {
        // Cancel current task if needed
        if (pollTask != null && !pollTask.isCancelled())
            pollTask.cancel(true);
    }

    public void cleanup() {
        // Shutdown scheduler
        if (executor != null && !executor.isShutdown())
            executor.shutdownNow();
    }

    public void setExecutor(ScheduledExecutorService executor) {
        stop();
        cleanup();
        this.executor = executor;
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

}
