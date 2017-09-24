package de.andre.chart.data.groups;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

public class TimeToGroupTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
	TimeToGroup<Entry> sut = new TimeToGroup<>(Entry::getTime);

	assertThat(sut.getGroupOf(entryOfMinute(0))).describedAs("0").isEqualTo(minute(0));
	assertThat(sut.getGroupOf(entryOfMinute(11))).describedAs("11").isEqualTo(minute(10));
	assertThat(sut.getGroupOf(entryOfMinute(12))).describedAs("12").isEqualTo(minute(10));
	assertThat(sut.getGroupOf(entryOfMinute(13))).describedAs("13").isEqualTo(minute(10));
	assertThat(sut.getGroupOf(entryOfMinute(14))).describedAs("14").isEqualTo(minute(10));
	assertThat(sut.getGroupOf(entryOfMinute(15))).describedAs("15").isEqualTo(minute(15));
	assertThat(sut.getGroupOf(entryOfMinute(16))).describedAs("16").isEqualTo(minute(15));
	assertThat(sut.getGroupOf(entryOfMinute(17))).describedAs("17").isEqualTo(minute(15));
	assertThat(sut.getGroupOf(entryOfMinute(18))).describedAs("18").isEqualTo(minute(15));
	assertThat(sut.getGroupOf(entryOfMinute(19))).describedAs("19").isEqualTo(minute(15));
    }

    private static Entry entryOfMinute(int minute) {
	return new Entry().setId("" + minute).setTime(minute(minute));
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
