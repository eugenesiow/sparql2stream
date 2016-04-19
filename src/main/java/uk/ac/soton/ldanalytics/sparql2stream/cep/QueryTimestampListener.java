package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.util.Map;
import java.util.Map.Entry;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class QueryTimestampListener implements UpdateListener {

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if(newEvents.length>0) {
			long ms = System.currentTimeMillis();
			long time1 = (Long)((Map<?, ?>)newEvents[0].getUnderlying()).get("time1");
			long time2 = (Long)((Map<?, ?>)newEvents[0].getUnderlying()).get("time2");
			long inserttime = time1 > time2 ? time1 : time2;
			System.out.println(ms-inserttime);

//			for(Object map:((Map<?, ?>)newEvents[0].getUnderlying()).entrySet()) {
//				Entry<?, ?> entry = ((Entry<?, ?>)map);
//				System.out.println(entry.getKey()+":"+entry.getValue());
//			}
		}
	}

}
