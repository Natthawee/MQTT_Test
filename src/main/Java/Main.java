import org.fusesource.mqtt.client.*;

import java.net.URISyntaxException;

public class Main
{
    public static void main(String[] args)
    {
        MQTT mqtt = new MQTT();
        try
        {
            mqtt.setHost("natchu.ddns.net", 10601);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        mqtt.setReconnectDelay(5000);

        BlockingConnection connection = mqtt.blockingConnection();
        try
        {
            connection.connect();
            connection.subscribe(new Topic[] {
                    new Topic("/ESP/DOOR", QoS.AT_LEAST_ONCE)
            });
//            connection.publish("testTopic", "Test message".getBytes(), QoS.AT_LEAST_ONCE, false);
            int i = 1;
            while(i <= 100)
            {
                Message message = connection.receive();
                if(message != null)
                {
                    System.out.println("Received message #" + i + ": " + new String(message.getPayload()));
                    i++;
                }
            }
            connection.unsubscribe(new String[] {"testTopic"});
            connection.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
