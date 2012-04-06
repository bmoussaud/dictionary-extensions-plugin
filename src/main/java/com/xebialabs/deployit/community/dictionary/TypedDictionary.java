package com.xebialabs.deployit.community.dictionary;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.xebialabs.deployit.plugin.api.reflect.Descriptor;
import com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry;
import com.xebialabs.deployit.plugin.api.reflect.PropertyDescriptor;
import com.xebialabs.deployit.plugin.api.reflect.PropertyKind;
import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.xebialabs.deployit.plugin.api.reflect.PropertyKind.*;
import static com.xebialabs.deployit.plugin.api.udm.Metadata.ConfigurationItemRoot.ENVIRONMENTS;

@Metadata(root = ENVIRONMENTS, virtual = true, description = "An abstract Dictionary with keys defined as properties.")
public class TypedDictionary extends Dictionary {

	private static final Set<PropertyKind> FORBIDEN_KINDS = ImmutableSet.of(LIST_OF_CI, LIST_OF_STRING, SET_OF_CI, SET_OF_STRING, MAP_STRING_STRING);

	@Override
	public void setEntries(Map<String, String> dict) {

	}

	@Override
	public Map<String, String> getEntries() {
		return getEntriesAsMap();
	}

	private Map<String, String> getEntriesAsMap() {
		final Map<String, String> entries = Maps.newHashMap();
		final Descriptor descriptor = DescriptorRegistry.getDescriptor(getType());
		final Collection<PropertyDescriptor> propertyDescriptors = descriptor.getPropertyDescriptors();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if (propertyDescriptor.isHidden()) {
				logger.info("The '{}' hidden property isn't managed by the typed dictionary, skip it", propertyDescriptor.getName());
				continue;
			}

			if (FORBIDEN_KINDS.contains(propertyDescriptor.getKind())) {
				logger.info("The '{}' kind of the property '{}' is not supported by the typed dictionary, skip it", propertyDescriptor.getKind(), propertyDescriptor.getName());
				continue;
			}

			final String name = propertyDescriptor.getName();
			final Object property = getProperty(name);
			if (property != null) {
				entries.put(name, property.toString());
			}
		}
		return entries;
	}

	@Override
	public String getValue(String key) {
		return getEntriesAsMap().get(key);
	}

	@Override
	public boolean hasValue(String key) {
		return getEntriesAsMap().containsKey(key);
	}

	private static final Logger logger = LoggerFactory.getLogger(TypedDictionary.class);
}
