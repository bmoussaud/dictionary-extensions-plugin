package com.xebialabs.deployit.community.dictionary.deployed;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.xebialabs.deployit.community.dictionary.contributor.ApplicationConfigurationProcessor;
import com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.test.deployment.DeployitTester;
import com.xebialabs.deployit.test.support.LoggingDeploymentExecutionContext;
import com.xebialabs.deployit.test.support.TestUtils;
import org.junit.*;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.of;
import static com.xebialabs.deployit.test.support.TestUtils.*;
import static org.junit.Assert.assertThat;


public class ApplicationConfigurationProcessorTest {


	DeployedApplication deployedApplication;
	Container container;
	Deployed deployed;
	Deployable deployableArtifact;

	Function<TemporaryFolder, Deployable> deployableArtifactFactory = new Function<TemporaryFolder, Deployable>() {
		@Override
		public Deployable apply(TemporaryFolder input) {
			try {
				final Deployable artifact = (Deployable) createArtifact("configuration.txt", "1.0", "data/configuration-file.properties", "test.File", input.newFolder());
				artifact.setProperty("placeholders", ImmutableSet.of("CODE", "MKT"));
				artifact.setProperty("targetDirectory", "/tmp");
				return artifact;
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	};

	Deployable deployableResource;
	Function<TemporaryFolder, Deployable> deployableResourceFactory = new Function<TemporaryFolder, Deployable>() {
		@Override
		public Deployable apply(TemporaryFolder input) {
			final Deployable ac = newInstance("udm.ApplicationConfiguration");
			ac.setProperty("entries", ImmutableMap.of("MKT", "ASIA"));
			return ac;
		}
	};


	@Test
	public void shouldCreateAndDestroyDeployedSimple() throws Exception {
		deployedApplication = createDeployedApplication(
				createDeploymentPackage("1.0", getDeployableArtifact()),
				createEnvironment(getContainer()));

		final Map<String, String> placeholders = Maps.newHashMap();
		placeholders.put("CODE", "MMM");
		placeholders.put("MKT", "EUROPE");
		createDeployed(placeholders);
		assertDeployedCreatedCorrectly(of("MMM", "EUROPE"));
	}

	@Test
	public void shouldCreateAndDestroyDeployedWithApplicationConfiguration() throws Exception {
		deployedApplication = createDeployedApplication(
				createDeploymentPackage("1.0", getDeployableArtifact(), getDeployableResource()),
				createEnvironment(getContainer()));

		final Map<String, String> placeholders = Maps.newHashMap();
		placeholders.put("CODE", "MMM");
		placeholders.put("MKT", "<app-conf>");
		createDeployed(placeholders);
		assertDeployedCreatedCorrectly(of("MMM", "ASIA"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldCreateAndDestroyDeployedWithMissingApplicationConfiguration() throws Exception {
		deployedApplication = createDeployedApplication(
				createDeploymentPackage("1.0", getDeployableArtifact(), getDeployableResource()),
				createEnvironment(getContainer()));

		final Map<String, String> placeholders = Maps.newHashMap();
		placeholders.put("CODE", "<app-conf>");
		placeholders.put("MKT", "<app-conf>");
		createDeployed(placeholders);
		assertDeployedCreatedCorrectly(of("MMM", "ASIA"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldCreateAndDestroyDeployedWithApplicationConfigurationButConfigured() throws Exception {
		deployedApplication = createDeployedApplication(
				createDeploymentPackage("1.0", getDeployableArtifact(), getDeployableResource()),
				createEnvironment(getContainer()));

		final Map<String, String> placeholders = Maps.newHashMap();
		placeholders.put("CODE", "MMM");
		placeholders.put("MKT", "NASDAQ");
		createDeployed(placeholders);
		assertDeployedCreatedCorrectly(of("MMM", "ASIA"));
	}

	private void createDeployed(Map<String, String> placeholders) {
		deployed = tester.generateDeployed(getDeployableArtifact(), getContainer(), Type.valueOf("test.DeployedFile"), placeholders);
		DeltaSpecification spec = new DeltaSpecificationBuilder()
				.initial(deployedApplication)
				.create(deployed).build();

		ApplicationConfigurationProcessor processor = new ApplicationConfigurationProcessor();
		processor.injectPlaceholderValues(spec);
	}

	private void assertDeployedCreatedCorrectly(List<String> expectedContent) throws IOException {
		final Map<String, String> placeholders = deployed.getProperty("placeholders");
		for (String c : expectedContent)
			assertThat(placeholders.values(), IsCollectionContaining.hasItem(c));
	}

	@After
	public void tearDown() {
		new File("/tmp/configuration.txt").delete();
	}

	public Deployable getDeployableArtifact() {
		if (deployableArtifact == null)
			deployableArtifact = deployableArtifactFactory.apply(folder);
		return deployableArtifact;
	}

	public Deployable getDeployableResource() {
		if (deployableResource == null) {
			deployableResource = deployableResourceFactory.apply(folder);
		}
		return deployableResource;
	}


	public Container getContainer() {
		if (container == null) {
			Host host = TestUtils.newInstance("overthere.LocalHost");
			host.setId(id("Infrastructure", "localHost"));
			host.setOs(com.xebialabs.overthere.OperatingSystemFamily.UNIX);
			host.setProperty("tmpDeleteOnDisconnect", "false");
			container = host;
		}
		return container;
	}

	///// Base
	static {
		PluginBooter.boot();
	}

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	protected static DeployitTester tester;

	protected static LoggingDeploymentExecutionContext context;


	@BeforeClass
	public static void boot() {
		tester = DeployitTester.build();
	}

	@BeforeClass
	public static void createContext() {
		context = new LoggingDeploymentExecutionContext(ApplicationConfigurationProcessorTest.class);
	}

	@AfterClass
	public static void destroyContext() {
		context.destroy();
	}

}
