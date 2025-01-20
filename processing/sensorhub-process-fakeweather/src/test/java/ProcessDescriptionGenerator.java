import com.google.gson.stream.JsonWriter;
import net.opengis.sensorml.v20.AggregateProcess;
import org.junit.Test;
import org.sensorhub.process.weather.ProcessHelper;
import org.sensorhub.process.weather.WeatherProcess;
import org.vast.data.SWEFactory;
import org.vast.process.ProcessException;
import org.vast.sensorML.AggregateProcessImpl;
import org.vast.sensorML.SMLBuilders;
import org.vast.sensorML.SMLJsonBindings;
import org.vast.xml.XMLWriterException;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ProcessDescriptionGenerator {
    SWEFactory fac = new SWEFactory();
    ProcessHelper helper = new ProcessHelper();

    public AggregateProcess generateDescription() throws ProcessException {
        WeatherProcess p1 = new WeatherProcess();
        p1.init();

        return helper.createProcessChain()
                .name("Process Chain")
                .uid("urn:osh:process:weather")
                .addDataSource("source0", "urn:osh:sensor:simweather:001")
                .addOutputList(p1.getOutputList())
                .addProcess("process0", p1)
                .addConnection("components/source0/outputs/weather",
                        "components/process0/inputs/weather")
                .addConnection("components/process0/outputs/weather",
                        "outputs/weather")
                .build();
    }

    @Test
    public void generateDescJSON() throws ProcessException, IOException {
        SMLJsonBindings jsonBindings = new SMLJsonBindings();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(System.out));
        writer.setIndent("");
        writer.beginObject();
        jsonBindings.writeAggregateProcessProperties(writer, generateDescription());
        writer.endObject();
        writer.flush();
    }

    @Test
    public void generateDescXML() throws ProcessException, XMLWriterException {

        helper.writeProcess(System.out, generateDescription(), true);
    }

}
