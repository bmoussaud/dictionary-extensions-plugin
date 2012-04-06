package com.xebialabs.deployit.plugins.dictionary;

import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;

import static com.xebialabs.deployit.plugin.api.udm.Metadata.ConfigurationItemRoot.ENVIRONMENTS;

@Metadata(root = ENVIRONMENTS, virtual = true, description = "a versioned dictionary.")
public class VersionedDictionary extends Dictionary {

	@Property
	private int  version = 0;

	@Property
	private int  defaultVersion = 0;

}
