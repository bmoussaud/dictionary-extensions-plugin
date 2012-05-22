package com.xebialabs.deployit.community.dictionary.contributor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.execution.Step;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.artifact.DerivedArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Predicates.compose;
import static com.google.common.collect.Iterables.*;
import static com.xebialabs.deployit.community.dictionary.contributor.Predicates2.instanceOf;

public class ApplicationConfigurationProcessor {

	static final Type APP_CONF_TYPE = Type.valueOf("udm.ApplicationConfiguration");

	static final Predicate<Delta> IS_DERIVED_ARTIFACT = compose(
			Predicates.instanceOf(DerivedArtifact.class),
			Predicates2.extractDeployed());


	@PrePlanProcessor
	public Step preProcess(DeltaSpecification specification) {
		final Set<Deployable> deployables = specification.getDeployedApplication().getVersion().getDeployables();
		if (!any(deployables, instanceOf(APP_CONF_TYPE))) {
			logger.info("no {} in the version", APP_CONF_TYPE);
			return null;
		}
		final Iterable<Map<String, String>> applicationConfigurations = transform(filter(deployables, instanceOf(APP_CONF_TYPE)), new Function<Deployable, Map<String, String>>() {
			@Override
			public Map<String, String> apply(Deployable input) {
				return input.getProperty("entries");
			}
		});

		for (Map<String, String> applicationConfiguration : applicationConfigurations) {
			if (applicationConfiguration.isEmpty())
				continue;

			for (Deployed<?, ?> deployed : gatherTargets(specification.getDeltas())) {
				final Map<String, String> placeholders = deployed.getProperty("placeholders");
				for (Map.Entry<String, String> entry : applicationConfiguration.entrySet()) {
					if (placeholders.containsKey(entry.getKey())) {
						logger.info("{}: override {} by {}, previous {}", new Object[]{deployed, entry.getKey(), entry.getValue(), placeholders.get(entry.getKey())});
						placeholders.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}

		return null;
	}

	private Iterable<Deployed<?, ?>> gatherTargets(List<Delta> operations) {
		final Iterable<Deployed<?, ?>> deployeds = transform(filter(operations, IS_DERIVED_ARTIFACT), Predicates2.extractDeployed());
		logger.info("deployeds {}", deployeds);
		return deployeds;

	}

	protected static final Logger logger = LoggerFactory.getLogger(ApplicationConfigurationProcessor.class);

}
