package de.andre.chart.data;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.springframework.stereotype.Component;

@Component
public class LocalDateTimeLookUp {
    private HashMap<String, Integer> strToId = new HashMap<>();
    private HashMap<Integer, LocalDateTime> idToLocalDate = new HashMap<>();
    private HashMap<LocalDateTime, Integer> localDateToId = new HashMap<>();

    private int nextAvailableId = 1;

    /**
     * @param part
     * @return
     */
    public Integer findId(String part) {
	return strToId.get(part);
    }

    public Integer findId(LocalDateTime time) {
	return localDateToId.get(time);
    }

    /**
     * 
     * @param part
     * @param time
     * @return
     */
    public int add(String part, LocalDateTime time) {
	Integer id = localDateToId.get(time);
	if (id == null) {
	    id = nextAvailableId;
	    nextAvailableId++;
	}
	strToId.put(part, id);
	idToLocalDate.put(id, time);
	localDateToId.put(time, id);

	return id;
    }

    public LocalDateTime getTimeById(int timestampId) {
	return idToLocalDate.get(timestampId);
    }
}
