import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;
import org.sensorhub.api.common.SensorHubException;

public class MqttTests {

    @Test
    public void test() throws SensorHubException, MqttException {
        MqttClient client = new MqttClient("tcp://broker.hivemq.com:1883", "OSHTest");
        client.connect();
        client.subscribe("osh-test", new IMqttMessageListener() {
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                System.out.println(new String(mqttMessage.getPayload()));
                System.out.println(s);
            }
        });
        try {
            Thread.sleep(36000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // client config: protocol, host, port, client id
        // subscription config: topic id, ?
    }

}
