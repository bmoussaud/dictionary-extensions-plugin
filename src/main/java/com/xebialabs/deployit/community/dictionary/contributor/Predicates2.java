package com.xebialabs.deployit.community.dictionary.contributor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Deployed;

import java.util.Collection;

import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.DESTROY;
import static com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry.getSubtypes;

public class Predicates2 {

	public static Predicate<Type> subtypeOf(Type type) {
		return new IsSubtypeOf(type);
	}

	public static Predicate<ConfigurationItem> instanceOf(Type type) {
		return com.google.common.base.Predicates.compose(subtypeOf(type),
				new Function<ConfigurationItem, Type>() {
					@Override
					public Type apply(ConfigurationItem input) {
						return input.getType();
					}
				});
	}

	public static Predicate<Delta> deltaOf(Type type) {
		return com.google.common.base.Predicates.compose(instanceOf(type),
				extractDeployed());
	}

	public static Function<Delta, Deployed<?, ?>> extractDeployed() {
		return new ExtractDeployed();
	}

	public static Predicate<Delta> operationIs(Operation operationToMatch) {
		return new OperationEquals(operationToMatch);
	}

	public static Predicate<Object> equalToAny(Collection<?> items) {
		return new EqualToAny(items);
	}

	private static class OperationEquals implements Predicate<Delta> {
		private final Operation operationToMatch;

		protected OperationEquals(Operation operationToMatch) {
			this.operationToMatch = operationToMatch;
		}

		@Override
		public boolean apply(Delta input) {
			return input.getOperation().equals(operationToMatch);
		}
	}

	private static class IsSubtypeOf implements Predicate<Type> {
		private final Collection<Type> subtypes;

		public IsSubtypeOf(Type typeToMatch) {
			subtypes = getSubtypes(typeToMatch);
			subtypes.add(typeToMatch);
		}

		@Override
		public boolean apply(Type input) {
			return subtypes.contains(input);
		}
	}

	private static class ExtractDeployed implements Function<Delta, Deployed<?, ?>> {
		@Override
		public Deployed<?, ?> apply(Delta input) {
			return (input.getOperation().equals(DESTROY)
					? input.getPrevious()
					: input.getDeployed());
		}
	}

	private static class EqualToAny implements Predicate<Object> {
		private final Collection<?> items;

		private EqualToAny(Collection<?> items) {
			this.items = items;
		}

		@Override
		public boolean apply(Object input) {
			return items.contains(input);
		}

	}


}
