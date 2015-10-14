package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class CepEngine {
	public static void main(String[] args) {
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		Map<String, Object> definition = new LinkedHashMap<String, Object>();
        definition.put("sensor", String.class);
        definition.put("temperature", double.class);
        epService.getEPAdministrator().getConfiguration().addEventType("SensorEvent", definition);
        String stmt = "select * from SensorEvent.win:keepall()";
//        String stmt = "select * from SensorEvent.win:time_batch(1 sec)";
        EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
        statement.addListener(new QueryListener());
        Random random = new Random();
        for (int i = 0; i < 1000; i++)
        {
            double temperature = random.nextDouble() * 10 + 80;
            String sensor = "s"+random.nextInt(10);

            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("temperature", temperature);
            data.put("sensor", sensor);

            epService.getEPRuntime().sendEvent(data, "SensorEvent");
            
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
}
