package com.xebialabs.deployit.community.dictionary.deployable;

import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.api.udm.base.BaseDeployable;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;


@SuppressWarnings("serial")
@Metadata(description = "the values provided by this resource will overide the values set in the dictionaries.")
public class ApplicationConfiguration extends BaseDeployable {

	@Property(description = "The dictionary entries", required = false)
	private Map<String, String> entries = newHashMap();

	public Map<String, String> getEntries() {
		return entries;
	}

	public void setEntries(Map<String, String> entries) {
		this.entries = entries;
	}
}
