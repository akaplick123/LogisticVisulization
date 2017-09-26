package de.andre.chart.ui.chartframe.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SubgroupAdderTest {

    private static final LocalDateTime TIME1 = LocalDateTime.of(2017, 9, 25, 15, 56);
    private static final LocalDateTime TIME2 = LocalDateTime.of(2017, 9, 25, 18, 56);
    private static final String GROUP1 = "grp1";
    private static final String GROUP2 = "grp2";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_Empty() {
	SubgroupAdder adder = new SubgroupAdder();
	assertThat(adder.getEntries()).isEmpty();
    }

    @Test
    public void test_Add() {
	SubgroupAdder adder = new SubgroupAdder();

	adder.add(TIME1, GROUP1, 10);
	assertThat(adder.getEntries()).describedAs("add 1").hasSize(1);
	assertThat(adder.getEntries()).describedAs("add 1").extracting(e -> e.getValue()).containsExactlyInAnyOrder(10);

	adder.add(TIME1, GROUP1, 5);
	assertThat(adder.getEntries()).describedAs("add 2").hasSize(1);
	assertThat(adder.getEntries()).describedAs("add 2").extracting(e -> e.getValue()).containsExactlyInAnyOrder(15);

	adder.add(TIME1, GROUP2, 7);
	assertThat(adder.getEntries()).describedAs("add 3").hasSize(2);
	assertThat(adder.getEntries()).describedAs("add 3").extracting(e -> e.getValue()).containsExactlyInAnyOrder(15,
		7);

	adder.add(TIME2, GROUP2, 13);
	assertThat(adder.getEntries()).describedAs("add 4").hasSize(3);
	assertThat(adder.getEntries()).describedAs("add 4").extracting(e -> e.getValue()).containsExactlyInAnyOrder(15,
		7, 13);

	adder.add(TIME2, GROUP2, 23);
	assertThat(adder.getEntries()).describedAs("add 5").hasSize(3);
	assertThat(adder.getEntries()).describedAs("add 5").extracting(e -> e.getValue()).containsExactlyInAnyOrder(15,
		7, 36);
    }

}
