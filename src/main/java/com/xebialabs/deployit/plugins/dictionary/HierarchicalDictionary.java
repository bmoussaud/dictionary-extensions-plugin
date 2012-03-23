package com.xebialabs.deployit.plugins.dictionary;

import com.google.common.collect.Maps;
import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static com.xebialabs.deployit.plugin.api.udm.Metadata.ConfigurationItemRoot.ENVIRONMENTS;


@Metadata(root = ENVIRONMENTS, description = "A Dictionary contains list of dictionaries that will be interrogated following the order of a list of other dictionaries.")
public class HierarchicalDictionary extends Dictionary {

	@Property(description = "The list of dictionaries, the data contained in the first dictionaries will override the data of the next dictionary, and so on...", required = false)
	private List<Dictionary> dictionaries = newArrayList();

	@Override
	public Map<String, String> getEntries() {
		return buildHierarchicalEntries();
	}

	@Override
	public String getValue(String key) {
		return buildHierarchicalEntries().get(key);
	}

	@Override
	public boolean hasValue(String key) {
		return buildHierarchicalEntries().containsKey(key);
	}

	Map<String, String> buildHierarchicalEntries() {
		final Map<String, String> hentries = Maps.newHashMap();

		if (dictionaries != null && !dictionaries.isEmpty()) {
			for (Dictionary dictionary : reverse(dictionaries)) {
				final Map<String, String> entries = dictionary.getEntries();
				if (entries != null) {
					hentries.putAll(entries);
				}
			}
		}

		Map<String, String> entries = getProperty("entries");
		if (entries != null) {
			hentries.putAll(entries);
		}
		return hentries;
	}

}
