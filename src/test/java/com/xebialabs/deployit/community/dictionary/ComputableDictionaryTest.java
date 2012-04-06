package com.xebialabs.deployit.community.dictionary;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.test.support.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ComputableDictionaryTest {

	static final String TYPE = "dict.ComputableDictionary";

	@BeforeClass
	public static void boot() {
		PluginBooter.bootWithoutGlobalContext();
	}

	@Test
	public void testOneComputedEntry() throws Exception {
		final Dictionary replaceableDictionary = TestUtils.newInstance(TYPE);
		replaceableDictionary.setProperty("entries", ImmutableMap.of("PORT", "1521", "URL", "jdbc:oracle@localhost:{{PORT}}"));
		final Map<String, String> entries = replaceableDictionary.getEntries();
		assertTrue(entries.containsKey("PORT"));
		assertTrue(entries.containsKey("URL"));
		assertThat(entries.get("PORT"), is("1521"));
		assertThat(entries.get("URL"), is("jdbc:oracle@localhost:1521"));
	}

	@Test
	public void testTwoComputedEntry() throws Exception {
		final Dictionary replaceableDictionary = TestUtils.newInstance(TYPE);
		replaceableDictionary.setProperty("entries", ImmutableMap.of("PORT", "1521", "SERVER", "xebialabs.com", "URL", "jdbc:oracle@{{SERVER}}:{{PORT}}"));
		final Map<String, String> entries = replaceableDictionary.getEntries();
		assertThat(entries.get("PORT"), is("1521"));
		assertThat(entries.get("SERVER"), is("xebialabs.com"));
		assertThat(entries.get("URL"), is("jdbc:oracle@xebialabs.com:1521"));
	}

	@Test
	public void testDependantKeys() throws Exception {
		final Dictionary replaceableDictionary = TestUtils.newInstance(TYPE);
		replaceableDictionary.setProperty("entries", ImmutableMap.of("FOO", "1521", "BAR", "{{FOO}}", "FOOBAR", "x-{{BAR}}"));
		final Map<String, String> entries = replaceableDictionary.getEntries();
		assertThat(entries.get("FOO"), is("1521"));
		assertThat(entries.get("BAR"), is("1521"));
		assertThat(entries.get("FOOBAR"), is("x-1521"));
	}

	@Test
	public void testMissingKeys() throws Exception {
		final Dictionary replaceableDictionary = TestUtils.newInstance(TYPE);
		replaceableDictionary.setProperty("entries", ImmutableMap.of("FOO", "1521", "BAR", "{{FOO}}", "FOOBAR", "x-{{XBAR}}"));
		final Map<String, String> entries = replaceableDictionary.getEntries();
		assertThat(entries.get("FOO"), is("1521"));
		assertThat(entries.get("BAR"), is("1521"));
		assertThat(entries.get("FOOBAR"), is("x-{{XBAR}}"));
	}
}
