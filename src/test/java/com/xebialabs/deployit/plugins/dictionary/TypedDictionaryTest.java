package com.xebialabs.deployit.plugins.dictionary;

import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.test.support.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypedDictionaryTest {

	@BeforeClass
	public static void boot() {
		PluginBooter.bootWithoutGlobalContext();
	}

	@Test
	public void test1() throws Exception {
		final Dictionary typedDictionary = TestUtils.newInstance("ext.MyTypedDictionary");
		typedDictionary.setProperty("aString", "foo");
		typedDictionary.setProperty("aPassword", "tiger");
		typedDictionary.setProperty("aInteger", 128);
		typedDictionary.setProperty("aBoolean", true);
		typedDictionary.setProperty("anEnum", MyEnum.THREE);
		typedDictionary.setProperty("anHiddenProperty", "hidden");
		typedDictionary.setProperty("aSetOfStrings", newHashSet());
		typedDictionary.setProperty("aListOfStrings", newArrayList());

		final Map<String, String> entries = typedDictionary.getEntries();

		assertThat(entries.keySet().contains("aString"), is(true));
		assertThat(entries.get("aString"), is("foo"));

		assertThat(entries.keySet().contains("aPassword"), is(true));
		assertThat(entries.get("aPassword"), is("tiger"));

		assertThat(entries.keySet().contains("aInteger"), is(true));
		assertThat(entries.get("aInteger"), is("128"));

		assertThat(entries.keySet().contains("aBoolean"), is(true));
		assertThat(entries.get("aBoolean"), is("true"));

		assertThat(entries.keySet().contains("anEnum"), is(true));
		assertThat(entries.get("anEnum"), is("THREE"));

		assertThat(entries.keySet().contains("anHiddenProperty"), is(false));
		assertThat(entries.keySet().contains("aSetOfStrings"), is(false));
		assertThat(entries.keySet().contains("aListOfStrings"), is(false));
	}
}
