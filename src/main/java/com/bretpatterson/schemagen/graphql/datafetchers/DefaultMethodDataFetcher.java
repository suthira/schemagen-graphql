package com.bretpatterson.schemagen.graphql.datafetchers;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of a IMethodDataFetcher that will invoke a method call with the provided GraphQL arguments. If null and a default value is
 * provided for the argument then the default value will be used, otherwise null will be sent to the method. Parameter objects are converted
 * from <i>GraphQL</i> Deserialized types to the arguments declared type using the regisered {@link ITypeFactory}.
 */
public class DefaultMethodDataFetcher implements IMethodDataFetcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMethodDataFetcher.class);
	private ITypeFactory typeFactory;

	private Method method;
	private String fieldName;
	private Object targetObject;
	private LinkedHashMap<String, Type> argumentTypeMap = new LinkedHashMap<>();
	private Map<String, Object> parameterDefaultValue = Maps.newHashMap();

	@Override
	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}

	@Override
	public void addParam(String name, Type type, Optional<Object> defaultValue) {
		argumentTypeMap.put(name, type);
		if (defaultValue.isPresent()) {
			parameterDefaultValue.put(name, defaultValue.get());
		}
	}

	Object getDefaultValue(DataFetchingEnvironment environment, String name, Type argumentType) {
		if (parameterDefaultValue.containsKey(name)) {
			return typeFactory.convertToType(argumentTypeMap.get(name), parameterDefaultValue.get(name));
		} else {
			return null;
		}
	}

	Object convertToType(Type argumentType, Object value) {
		if (value == null)
			return value;

		return typeFactory.convertToType(argumentType, value);
	}

	Object getParamValue(DataFetchingEnvironment environment, String argumentName, Type argumentType) {
		Object value =environment.getArgument(argumentName);
		if (value == null) {
			value = getDefaultValue(environment, argumentName, argumentType);
		}
		return convertToType(argumentType, value);
	}

	@Override
	public Object get(DataFetchingEnvironment environment) {

		try {
			for (Field field : environment.getFields()) {
				if (field.getName().equals(fieldName)) {
					Object[] arguments = new Object[argumentTypeMap.size()];
					int index = 0;
					for (String argumentName : argumentTypeMap.keySet()) {
						arguments[index] = getParamValue(environment, argumentName, argumentTypeMap.get(argumentName));
						index++;
					}
					return method.invoke(targetObject, (Object[]) arguments);
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Unexpected error.", ex);
			Throwables.propagate(ex);
		}
		return null;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public void setTypeFactory(ITypeFactory typeFactory) {
		this.typeFactory = typeFactory;
	}
}