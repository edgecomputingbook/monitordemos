import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.siddhi.core.event.Event;
import io.siddhi.core.util.transport.InMemoryBroker;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OutputSubscriber implements InMemoryBroker.Subscriber {

    private String topic;
    private String streamName;
    private Gson gson;
    private Timer timer;

    public OutputSubscriber(String topic, String streamName) {
        this.topic = topic;
        this.streamName = streamName;
        gson = new Gson();
        MeterRegistry registry = new SimpleMeterRegistry();
        timer = Timer
                .builder("my.timer")
                .description("a description of what this timer does") // optional
                .register(registry);


    }

    @Override
    public void onMessage(Object msg) {

        try {
            //System.out.println("OUTPUT EVENT: " + msg);
            Map map = gson.fromJson((String)msg, Map.class);
            //System.out.println(map.get("event"));
            String sensorStr = map.get("event").toString();
            Sensor sensor = gson.fromJson(sensorStr,Sensor.class);
            long nsdiff = (System.currentTimeMillis() - sensor.timestamp);
            sensor.transactiontime = nsdiff;
            timer.record(nsdiff, TimeUnit.MILLISECONDS);
            //sensor.transactiontime = System.currentTimeMillis() - sensor.timestamp;
            System.out.println("OUTPUT EVENT: " + sensor.toString());
            System.out.println("MEAN Transaction Time (MS): " + (int)timer.mean(TimeUnit.MILLISECONDS));
        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public String getTopic() {
        return topic;
    }

}