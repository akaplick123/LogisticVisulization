package de.andre.chart.data.groups;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

public class TimedGroupsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetAllKeys() {
	TimedGroups<Entry> groups = newGroup();
	groups.add(new Entry().setTime(minute(15)).setId("1"));
	groups.add(new Entry().setTime(minute(10)).setId("2"));
	groups.add(new Entry().setTime(minute(15)).setId("3"));
	groups.add(new Entry().setTime(minute(20)).setId("4"));

	Stream<LocalDateTime> result = groups.getAllKeys();
	assertThat(result).containsExactly(minute(10), minute(15), minute(20));
    }

    @Test
    public void testGetAllKeysBetween() {
	TimedGroups<Entry> groups = newGroup();
	groups.add(new Entry().setTime(minute(15)).setId("1"));
	groups.add(new Entry().setTime(minute(10)).setId("2"));
	groups.add(new Entry().setTime(minute(15)).setId("3"));
	groups.add(new Entry().setTime(minute(20)).setId("4"));

	assertThat(groups.getAllKeysBetween(minute(10), minute(15))).describedAs("(10..15]")
		.containsExactly(minute(15));
	assertThat(groups.getAllKeysBetween(minute(9), minute(15))).describedAs("(9..15]").containsExactly(minute(10),
		minute(15));
	assertThat(groups.getAllKeysBetween(minute(9), minute(14))).describedAs("(9..14]").containsExactly(minute(10));
	assertThat(groups.getAllKeysBetween(minute(10), minute(14))).describedAs("(10..14]").isEmpty();
    }

    @Test
    public void testGetValues() {
	TimedGroups<Entry> groups = newGroup();
	groups.add(new Entry().setTime(minute(15)).setId("1"));
	groups.add(new Entry().setTime(minute(10)).setId("2"));
	groups.add(new Entry().setTime(minute(15)).setId("3"));
	groups.add(new Entry().setTime(minute(20)).setId("4"));

	Stream<Entry> result = groups.getValues(minute(10));
	assertThat(result).extracting("id").containsExactlyInAnyOrder("2");
    }

    private static TimedGroups<Entry> newGroup() {
	return new TimedGroups<>(Entry::getTime);
    }

    private static LocalDateTime minute(int minute) {
	return LocalDateTime.of(2017, 9, 21, 15, minute);
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    private static final class Entry {
	private LocalDateTime time;
	private String id;
    }
}
