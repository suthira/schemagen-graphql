package com.bretpatterson.schemagen.graphql.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields annotated with this use the specified data fetcher for retrieval
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface GraphQLDataFetcher {

	/**
	 * A custom datafetcher you would like to use for this field
	 * @return
	 */
	Class dataFetcher();
}
