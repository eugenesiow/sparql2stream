package uk.ac.soton.ldanalytics.sparql2stream.cep;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class QueryListener implements UpdateListener {

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		// TODO Auto-generated method stub
		if(newEvents.length>0) {
			System.out.println(newEvents[0].get("value")+","+newEvents[0].get("sensor"));
		}
	}

}
