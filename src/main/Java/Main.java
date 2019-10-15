import org.bson.BsonDocument;
import org.fusesource.mqtt.client.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

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
            ArrayList<String> deviceList = new ArrayList<>();
            deviceList.add("DC_5XPmDjAu");
            deviceList.add("DC_RqVz6AP4");
            deviceList.add("DC_xxxxxxxx");
            deviceList.add("DC_yyyyyyyy");
            String[] msgPayloads = new String[deviceList.size()];
            String[][] parsedJson = new String[deviceList.size()][2];
            int i = 0;
            while(i < deviceList.size())
            {
                Message message = connection.receive();
                if(message != null)
                {
                    String msgPayload = new String(message.getPayload());
                    msgPayloads[i] = msgPayload;
                    i++;
                }
            }
            Arrays.sort(msgPayloads);
            msgPayloads = new ArrayList<>(Arrays.asList(msgPayloads))
                    .stream()
                    .distinct().toArray(String[]::new);
            System.out.println(Arrays.toString(msgPayloads));

            i = 0;
            while(i < deviceList.size())
            {
                parsedJson[i][0] = deviceList.get(i);
                i++;
            }

            i = 0;
            while(i < msgPayloads.length)
            {
                BsonDocument bsonDoc = BsonDocument.parse(msgPayloads[i]);
                if(!bsonDoc.isEmpty())
                {
                    if(bsonDoc.getString("deviceName", null).getValue().equals(parsedJson[i][0]))
                    {
                        if(bsonDoc.getString("publishMessage", null).getValue().equals("STATUS DOOR LOCK"))
                        {
                            parsedJson[i][1] = "LOCK";
                        }
                        else if(bsonDoc.getString("publishMessage", null).getValue().equals("STATUS DOOR UNLOCK"))
                        {
                            parsedJson[i][1] = "UNLOCK";
                        }
                    }
                    else
                    {
                        parsedJson[i][1] = null;
                    }
                }
                i++;
            }

            for(String[] ss : parsedJson)
            {
                System.out.println(ss[0] + ", " + ss[1]);
            }

            connection.unsubscribe(new String[] {"/ESP/DOOR"});
            connection.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
