package org.sensorhub.impl.sensor.ffmpeg;

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.command.CommandException;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.common.BigId;
import org.sensorhub.impl.sensor.AbstractSensorControl;
import org.sensorhub.impl.sensor.ffmpeg.config.FFMPEGConfig;
import org.sensorhub.impl.sensor.videocam.VideoCamHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class PTZControl extends AbstractSensorControl<FFMPEGSensorBase> {

    DataRecord commandData;

    // define and set default values
    double minPan = -180.0;
    double maxPan = 180.0;
    double minTilt = -180.0;
    double maxTilt = 0.0;
    double minZoom = 1.0;
    double maxZoom = 9999;

    protected PTZControl(String name, FFMPEGSensorBase parentSensor) {
        super(name, parentSensor);
    }

    public void init() {
        VideoCamHelper videoCamHelper = new VideoCamHelper();
        commandData = videoCamHelper.newPtzOutput("ptzControl", minPan, maxPan, minTilt, maxTilt, minZoom, maxZoom);
        commandData.removeComponent("time");
    }

    @Override
    protected boolean execCommand(DataBlock cmdData) throws CommandException {
        DataRecord commandMsg = commandData.copy();
        commandMsg.setData(cmdData);

        double pan = commandMsg.getComponent("pan").getData().getDoubleValue();
        double tilt = commandMsg.getComponent("tilt").getData().getDoubleValue();
        double zoom = commandMsg.getComponent("zoomFactor").getData().getDoubleValue();

        try {
            var config = (FFMPEGConfig) parentSensor.getConfiguration();
            URL url = new URL(config.ptzCommandUrl);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            String payload = "{ \"pan\": " + pan + ", \"tilt\": " + tilt + ", \"zoom\": " + zoom + " }";
            System.out.println(payload);
            byte[] out = payload.getBytes(StandardCharsets.UTF_8);
            OutputStream stream = con.getOutputStream();
            stream.write(out);
            System.out.println(con.getResponseCode() + " " + con.getResponseMessage());
            con.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // TODO: Send post request to python server from configuration
        return true;
    }

    @Override
    public DataComponent getCommandDescription() {
        return commandData;
    }

    @Override
    public DataEncoding getCommandEncoding() {
        return super.getCommandEncoding();
    }

    @Override
    public CompletableFuture<Void> updateCommand(ICommandData command) {
        return super.updateCommand(command);
    }

    @Override
    public CompletableFuture<Void> cancelCommand(BigId cmdID) {
        return super.cancelCommand(cmdID);
    }
}
