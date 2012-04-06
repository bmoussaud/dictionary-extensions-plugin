package com.xebialabs.deployit.community.dictionary;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableList.of;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class StackedDictionaryTest {

	private Dictionary d1;
	private Dictionary d2;
	private Dictionary d3;
	private Dictionary d4;
	private Dictionary hd;

	@BeforeClass
	public static void boot() {
		PluginBooter.bootWithoutGlobalContext();
	}

	@Before
	public void setup() throws Exception {
		d1 = newInstance("udm.Dictionary");
		d1.setProperty("entries", ImmutableMap.of("X", "1", "Y", "1"));

		d2 = newInstance("udm.Dictionary");
		d2.setProperty("entries", ImmutableMap.of("X", "3", "Y", "3", "A", "8"));

		d3 = newInstance("udm.Dictionary");
		d3.setProperty("entries", ImmutableMap.of("X", "2"));

		d4 = newInstance("udm.Dictionary");
		d4.setProperty("entries", ImmutableMap.of("Y", "2"));


		hd = newInstance("dict.StackedDictionary");

	}

	@Test
	public void testEntriesOnly() {
		hd.setProperty("entries", ImmutableMap.of("foo","bar"));
		final Map<String, String> entries = hd.getEntries();
		assertThat(entries.size(),is(1));
		assertTrue(entries.containsKey("foo"));
		assertThat(entries.get("foo"), is("bar"));
	}

	@Test
	public void testEmpty() {
		final Map<String, String> entries = hd.getEntries();
		assertTrue(entries.isEmpty());
	}

	@Test
	public void testD1D2() {
		hd.setProperty("dictionaries", of(d1, d2));
		final Map<String, String> entries = hd.getEntries();
		assertThat(entries.size(),is(3));
		assertTrue(entries.containsKey("X"));
		assertTrue(entries.containsKey("Y"));
		assertThat(entries.get("X"), is("1"));
		assertThat(entries.get("Y"), is("1"));
		assertThat(entries.get("A"), is("8"));
	}

	@Test
	public void testD1D2Entries() {
		hd.setProperty("dictionaries", of(d1, d2));
		hd.setProperty("entries", ImmutableMap.of("X", "4", "Y", "4", "A", "9"));
		final Map<String, String> entries = hd.getEntries();
		assertThat(entries.size(),is(3));
		assertTrue(entries.containsKey("X"));
		assertTrue(entries.containsKey("Y"));
		assertThat(entries.get("X"), is("4"));
		assertThat(entries.get("Y"), is("4"));
		assertThat(entries.get("A"), is("9"));
	}

	@Test
	public void testD2D1() {
		hd.setProperty("dictionaries", of(d2, d1));
		final Map<String, String> entries = hd.getEntries();
		assertThat(entries.size(),is(3));
		assertTrue(entries.containsKey("X"));
		assertTrue(entries.containsKey("Y"));
		assertThat(entries.get("X"), is("3"));
		assertThat(entries.get("Y"), is("3"));
	}

	@Test
	public void testD3D1() {
		hd.setProperty("dictionaries", of(d3, d1));
		final Map<String, String> entries = hd.getEntries();
		assertThat(entries.size(),is(2));
		assertTrue(entries.containsKey("X"));
		assertTrue(entries.containsKey("Y"));
		assertThat(entries.get("X"), is("2"));
		assertThat(entries.get("Y"), is("1"));
	}

	@Test
	public void testD4D1() {
		hd.setProperty("dictionaries", of(d4, d1));
		final Map<String, String> entries = hd.getEntries();
		assertThat(entries.size(),is(2));
		assertTrue(entries.containsKey("X"));
		assertTrue(entries.containsKey("Y"));
		assertThat(entries.get("X"), is("1"));
		assertThat(entries.get("Y"), is("2"));
	}

	@Test
	public void testD1D4() {
		hd.setProperty("dictionaries", of(d1, d4));
		final Map<String, String> entries = hd.getEntries();
		assertThat(entries.size(),is(2));
		assertTrue(entries.containsKey("X"));
		assertTrue(entries.containsKey("Y"));
		assertThat(entries.get("X"), is("1"));
		assertThat(entries.get("Y"), is("1"));
	}
}
