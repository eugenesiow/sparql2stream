package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.util.Map;
import java.util.Map.Entry;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class QueryRecorderListener implements UpdateListener {

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if(newEvents.length>0) {
			long ms = System.currentTimeMillis();
			System.out.println(ms);
			for(Object map:((Map<?, ?>)newEvents[0].getUnderlying()).entrySet()) {
				Entry<?, ?> entry = ((Entry<?, ?>)map);
//				System.out.println(entry.getKey()+":"+entry.getValue());
			}
		}
	}

}
