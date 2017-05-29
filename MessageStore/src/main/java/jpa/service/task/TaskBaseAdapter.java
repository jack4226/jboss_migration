package jpa.service.task;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class TaskBaseAdapter implements TaskBaseBo, java.io.Serializable {
	private static final long serialVersionUID = -8159784802650360342L;

	public List<String> convertArgumensTotList(String taskArguments) {
		ArrayList<String> list = new ArrayList<String>();
		if (taskArguments != null) {
			StringTokenizer st = new StringTokenizer(taskArguments, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token != null && token.trim().length() > 0)
					list.add(token);
			}
		}
		return list;
	}
}
