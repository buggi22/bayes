package com.example.ai.bayes;

import com.example.ai.bayes.Event.AndClause;
import com.example.ai.bayes.Event.Condition;
import com.google.common.collect.ImmutableSetMultimap;

import junit.framework.TestCase;

public class EventTest extends TestCase {
  public void testNegateAlwaysTrue() {
    assertEquals(Event.alwaysFalse(), Event.not(Event.alwaysTrue()));
  }

  public void testNegateAlwaysFalse() {
    assertEquals(Event.alwaysTrue(), Event.not(Event.alwaysFalse()));
  }

  public void testVarEquals() {
    Event expected = Event.fromAndClauses(
        AndClause.of(ImmutableSetMultimap.<String, Condition>builder()
            .put("X", Condition.equal("A"))
            .build()));
    assertEquals(expected, Event.varEquals("X", "A"));
  }

  public void testNegateVarEquals() {
    Event expected = Event.fromAndClauses(
        AndClause.of(ImmutableSetMultimap.<String, Condition>builder()
            .put("X", Condition.notEqual("A"))
            .build()));
    assertEquals(expected, Event.not(Event.varEquals("X", "A")));
  }

  public void testAnd() {
    Event expected = Event.fromAndClauses(
        AndClause.of(ImmutableSetMultimap.<String, Condition>builder()
            .put("X", Condition.equal("A"))
            .put("Y", Condition.equal("B"))
            .build()));
    assertEquals(expected,
        Event.and(Event.varEquals("X", "A"), Event.varEquals("Y", "B")));
  }

  public void testOr() {
    Event expected = Event.fromAndClauses(
        AndClause.of(ImmutableSetMultimap.<String, Condition>builder()
            .put("X", Condition.equal("A"))
            .build()),
        AndClause.of(ImmutableSetMultimap.<String, Condition>builder()
            .put("Y", Condition.equal("B"))
            .build()));
    assertEquals(expected,
        Event.or(Event.varEquals("X", "A"), Event.varEquals("Y", "B")));
  }

  public void testNegateAnd() {
    Event expected = Event.or(
        Event.not(Event.varEquals("X", "A")),
        Event.not(Event.varEquals("Y", "B")));
    Event actual = Event.not(Event.and(
        Event.varEquals("X", "A"), Event.varEquals("Y", "B")));
    assertEquals(expected, actual);
  }

  public void testNegateOr() {
    Event expected = Event.and(
        Event.not(Event.varEquals("X", "A")),
        Event.not(Event.varEquals("Y", "B")));
    Event actual = Event.not(Event.or(
        Event.varEquals("X", "A"), Event.varEquals("Y", "B")));
    assertEquals(expected, actual);
  }

  public void testDistributeAndOverOr() {
    Event compact = Event.and(
        Event.or(Event.varEquals("X", "A"), Event.varEquals("Y", "B")),
        Event.varEquals("Z", "C"));
    Event expanded = Event.or(
        Event.and(Event.varEquals("X", "A"), Event.varEquals("Z", "C")),
        Event.and(Event.varEquals("Y", "B"), Event.varEquals("Z", "C")));
    assertEquals(expanded, compact);
  }
}
