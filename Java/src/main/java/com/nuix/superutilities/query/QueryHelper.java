package com.nuix.superutilities.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import nuix.MarkupSet;

public class QueryHelper {
	/***
	 * Builds an item-date Nuix range query for items with a date occurring within the specified year.
	 * @param year The year the range query should constrain to.  Value should be a valid 4 digit year (2019 not 19).
	 * @return A Nuix range query for item dates that occurred within the given year, i.e. item-date:[20190101 TO 20191231]
	 */
	public static String yearRangeQuery(int year) {
		// Unlikely someone would intentionally provide year in this range, this is here
		// mostly to catch accidentally odd values
		if (year < 1000 || year > 9999) {
			throw new IllegalArgumentException(String.format("Year %s is a non-sensical value",year));
		}
		
		return String.format("item-date:[%s0101 TO %s1231]", year, year);
	}
	
	/***
	 * Builds an item-date range query for items with a date occurring within the specified year and month.  For example
	 * a year input of 2019 and month input of 6 should yield the query: item-date:[20190601 TO 20190630]
	 * @param year The year the range query should constrain to.  Value should be a valid 4 digit year (2019 not 19).
	 * @param month The month of the year, valid values are 1 through 12
	 * @return A Nuix range query for items with an item date in the specified year and month
	 */
	public static String yearMonthRangeQuery(int year, int month) {
		// Unlikely someone would intentionally provide year in this range, this is here
		// mostly to catch accidentally odd values
		if (year < 1000 || year > 9999) {
			throw new IllegalArgumentException(String.format("Year %s is a non-sensical value",year));
		}
		
		// Make sure a valid month number was provided
		if(month < 1 || month > 12) {
			throw new IllegalArgumentException(String.format("Month %s does fall in valid range of 1-12",month));
		}
		
		// We need to get a date time which exists within the given month and year
		DateTime instant = new DateTime(year,month,1,1,1);
		// Determine the last day of the given month on the given year
		int lastDayOfMonth = instant.dayOfMonth().getMaximumValue();
		// Build a query for item dates within the same year/month
		return String.format("item-date:[%d%02d01 TO %d%02d%02d]", year,month,year,month,lastDayOfMonth);
	}
	
	/***
	 * Joins a series of expressions by " AND ", so expressions "cat", "dog" would become "cat AND dog".  Nil/null values and values containing only
	 * whitespace characters in the provided expressions collection are ignored.
	 * @param expressions Collection of expressions to join together.
	 * @return The expressions AND'ed together.
	 */
	public static String joinByAnd(Collection<String> expressions) {
		List<String> updatedExpressionsCollection = expressions	.stream()
				.filter(e -> e != null && e.trim().isEmpty() == false)
				.collect(Collectors.toList()); 
		return String.join(" AND ", updatedExpressionsCollection);
	}
	
	/***
	 * Joins a series of expressions by " AND " after wrapping each expression in parentheses.
	 * So expressions "cat", "dog" would become "(cat) AND (dog)".  Nil/null values and values containing only
	 * whitespace characters in the provided expressions collection are ignored.
	 * @param expressions Collection of expressions to join together.
	 * @return The expressions surrounded by parentheses and AND'ed together.
	 */
	public static String parenThenJoinByAnd(Collection<String> expressions) {
		List<String> updatedExpressionsCollection = expressions.stream()
				.filter(e -> e != null && e.trim().isEmpty() == false)
				.map(e -> String.format("(%s)", e))
				.collect(Collectors.toList()); 
		return String.join(" AND ", updatedExpressionsCollection);
	}
	
	/***
	 * Joins a series of expressions by " OR ", so expressions "cat", "dog" would become "cat OR dog".  Nil/null values and values containing only
	 * whitespace characters in the provided expressions collection are ignored.
	 * @param expressions Collection of expressions to join together.
	 * @return The expressions OR'ed together.
	 */
	public static String joinByOr(Collection<String> expressions) {
		List<String> updatedExpressionsCollection = expressions	.stream()
				.filter(e -> e != null && e.trim().isEmpty() == false)
				.collect(Collectors.toList()); 
		return String.join(" OR ", updatedExpressionsCollection);
	}
	
	/***
	 * Joins a series of expressions by " OR " after wrapping each expression in parentheses.
	 * So expressions "cat", "dog" would become "(cat) OR (dog)".  Nil/null values and values containing only
	 * whitespace characters in the provided expressions collection are ignored.
	 * @param expressions Collection of expressions to join together.
	 * @return The expressions surrounded by parentheses and OR'ed together.
	 */
	public static String parenThenJoinByOr(Collection<String> expressions) {
		List<String> updatedExpressionsCollection = expressions.stream()
				.filter(e -> e != null && e.trim().isEmpty() == false)
				.map(e -> String.format("(%s)", e))
				.collect(Collectors.toList()); 
		return String.join(" OR ", updatedExpressionsCollection);
	}
	
	/***
	 * Returns a query in the form: <code>NOT (a OR b OR c)</code> with a,b and c being expressions provided.
	 * @param expressions Expressions to be OR'ed and then NOT'ed.
	 * @return A query in the form: <code>NOT (a OR b OR c)</code>
	 */
	public static String notJoinByOr(Collection<String> expressions) {
		return String.format("NOT (%s)", joinByOr(expressions));
	}
	
	/***
	 * Generates a query to find items which have a match for any of the provided named entities.
	 * @param entityNames List of entity names to search for, must be values recognized by Nuix
	 * @return A query for items having matches for any of the specified named entities such as: named-entities:( company;* OR country;* OR credit-card-num;*)
	 */
	public static String namedEntityQuery(Collection<String> entityNames) {
		Set<String> entityNameFragments = entityNames.stream().map(ne -> String.format("%s;*",ne)).collect(Collectors.toSet());
		String query = String.format("named-entities:(%s)", String.join(" OR ",entityNameFragments));
		return query;
	}
	
	public static String markupSetQuery(MarkupSet markupSet) {
		return String.format("markup-set:\"%s\"", markupSet.getName());
	}
	
	public static String escapeForSearch(String value) {
		String result = value.replaceAll("\\\\", "\\\\\\\\");
		result = result.replaceAll("\\?", "\\\\?");
		result = result.replaceAll("\\*", "\\\\*");
		result = result.replaceAll("\"", "\\\"");
		result = result.replaceAll("\u201C", "\\\u201C");
		result = result.replaceAll("\u201D", "\\\u201D");
		result = result.replaceAll("'", "\\'");
		result = result.replaceAll("\\{", "\\\\{");
		result = result.replaceAll("\\}", "\\\\}");
		return result;
	}
	
	public static String orTagQuery(Collection<String> tags) {
		List<String> escapedTags = tags.stream()
				.map(tag -> escapeForSearch(tag))
				.map(tag -> "\""+tag+"\"")
				.collect(Collectors.toList());
		return String.format("tag:(%s)", String.join(" OR ", escapedTags));
	}
	
	public static String orTagQuery(String... tags) {
		List<String> tagsList = new ArrayList<String>();
		for (int i = 0; i < tags.length; i++) {
			tagsList.add(tags[i]);
		}
		return orTagQuery(tagsList);
	}
}
