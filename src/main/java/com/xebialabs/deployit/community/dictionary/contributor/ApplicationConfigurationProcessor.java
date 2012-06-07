package com.xebialabs.deployit.community.dictionary.contributor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugin.api.udm.artifact.DerivedArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Predicates.compose;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Sets.newHashSet;
import static com.xebialabs.deployit.community.dictionary.contributor.Predicates2.equalToAny;
import static com.xebialabs.deployit.community.dictionary.contributor.Predicates2.instanceOf;

public class ApplicationConfigurationProcessor {

	static final Type APP_CONF_TYPE = Type.valueOf("udm.ApplicationConfiguration");

	// workaround for http://tech.xebialabs.com/jira/browse/DEPLOYITPB-3414
	static final Set<String> APP_CONFIGURATION_PLACEHOLDER_TOKENS = ImmutableSet.of("<app-conf>", "&lt;app-conf&gt;");

	static final Predicate<Delta> IS_DERIVED_ARTIFACT = compose(
			Predicates.instanceOf(DerivedArtifact.class),
			Predicates2.extractDeployed());

	static final List<DeploymentStep> NO_STEPS = ImmutableList.of();

	@PrePlanProcessor
	public List<DeploymentStep> injectPlaceholderValues(DeltaSpecification specification) {
		final Version version = specification.getDeployedApplication().getVersion();

		if (!any(version.getDeployables(), instanceOf(APP_CONF_TYPE))) {
			logger.info("no {} in the version {} ", APP_CONF_TYPE, version);
			return null;
		}

		final Iterable<Map<String, String>> applicationConfigurations = transform(filter(version.getDeployables(), instanceOf(APP_CONF_TYPE)),
				new Function<Deployable, Map<String, String>>() {
					@Override
					public Map<String, String> apply(Deployable input) {
						return input.getProperty("entries");
					}
				});

		final Set<String> validationErrors = newHashSet();
		for (Map<String, String> applicationConfiguration : applicationConfigurations) {
			if (applicationConfiguration.isEmpty())
				continue;

			for (Deployed<?, ?> deployed : getDerivedArtifacts(specification.getDeltas())) {
				final Map<String, String> placeholders = deployed.getProperty("placeholders");
				for (Map.Entry<String, String> entry : applicationConfiguration.entrySet()) {
					if (placeholders.containsKey(entry.getKey())) {
						if (APP_CONFIGURATION_PLACEHOLDER_TOKENS.contains(placeholders.get(entry.getKey()))) {
							logger.info("{}: override {} by {}, previous {}", new Object[]{deployed, entry.getKey(), entry.getValue(), placeholders.get(entry.getKey())});
							placeholders.put(entry.getKey(), entry.getValue());
						} else {
							validationErrors.add(candidateEntryButNotHavingTheExpectedPlaceholder(deployed, entry));
						}
					}
				}
				if (any(placeholders.values(), equalToAny(APP_CONFIGURATION_PLACEHOLDER_TOKENS))) {
					validationErrors.add(missingApplicationConfiguration(deployed, placeholders));
				}
			}
		}

		//Throw exception if we have messages and it is not undeployement.
		if (!validationErrors.isEmpty() && !specification.getOperation().equals(Operation.DESTROY)) {
			throw new IllegalArgumentException(buildErrorMessage(
					specification.getDeployedApplication(), validationErrors));
		}
		return NO_STEPS;
	}

	private String missingApplicationConfiguration(Deployed<?, ?> deployed, Map<String, String> placeholders) {
		final String message = String.format("%s: still has 'application configurations' placeholders '%s' without having provided values'",
				deployed,
				Maps.filterValues(placeholders, equalToAny(APP_CONFIGURATION_PLACEHOLDER_TOKENS)).keySet());
		logger.warn(message);
		return message;
	}

	private String candidateEntryButNotHavingTheExpectedPlaceholder(Deployed<?, ?> deployed, Map.Entry<String, String> entry) {
		final String message = String.format("%s: has a candidate placeholder for application configuration '%s' but the value '%s' is not in '%s'",
				deployed, entry.getKey(), entry.getValue(), "<app-config>");
		logger.error(message);
		return message;
	}

	private Iterable<Deployed<?, ?>> getDerivedArtifacts(List<Delta> operations) {
		final Iterable<Deployed<?, ?>> deployeds = transform(filter(operations, IS_DERIVED_ARTIFACT), Predicates2.extractDeployed());
		logger.info("deployeds with derived artifacts {}", deployeds);
		return deployeds;

	}

	private static String buildErrorMessage(DeployedApplication deployedApplication,
											Set<String> validationErrors) {
		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append("Cannot deploy '").append(deployedApplication.getName())
				.append("' (version ").append(deployedApplication.getVersion().getVersion())
				.append(") to '").append(deployedApplication.getEnvironment().getName())
				.append("' due to the following errors:");
		for (String validationError : validationErrors) {
			errorMessage.append("\n- ").append(validationError);
		}
		return errorMessage.toString();
	}

	protected static final Logger logger = LoggerFactory.getLogger(ApplicationConfigurationProcessor.class);

}
