package com.example.ai.bayes;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

@AutoValue
public abstract class Event {
  public abstract ImmutableList<? extends AndClause> getAndClauses();

  @AutoValue
  public abstract static class AndClause {
    public abstract ImmutableList<? extends Case> getPositiveCases();
    public abstract ImmutableList<? extends Case> getNegativeCases();

    public Event negate() {
      List<AndClause> negatedClauses = Lists.newArrayList();
      for (Case positiveCase : getPositiveCases()) {
        negatedClauses.add(new AutoValue_Event_AndClause(
            ImmutableList.<Case>of(),
            ImmutableList.<Case>of(positiveCase)));
      }
      for (Case negativeCase : getNegativeCases()) {
        negatedClauses.add(new AutoValue_Event_AndClause(
            ImmutableList.<Case>of(negativeCase),
            ImmutableList.<Case>of()));
      }
      return new AutoValue_Event(ImmutableList.copyOf(negatedClauses));
    }
  }

  @AutoValue
  public abstract static class Case {
    public abstract String getVariable();
    public abstract String getValue();
  }

  public static Event of(String variable, String value) {
    return new AutoValue_Event(ImmutableList.of(
        new AutoValue_Event_AndClause(
            ImmutableList.<Case>of(new AutoValue_Event_Case(variable, value)),
            ImmutableList.<Case>of())));
  }

  public static Event not(Event e) {
    // Note: an event with no clauses is always false (empty OR-statement),
    // so we start with an always true event (a single empty AND-statement).
    Event result = new AutoValue_Event(ImmutableList.of(
        new AutoValue_Event_AndClause(
            ImmutableList.<Case>of(),
            ImmutableList.<Case>of())));
    for (AndClause andClause : e.getAndClauses()) {
      result = and(result, andClause.negate());
    }
    return result;
  }

  public static Event or(Event e1, Event e2) {
    ImmutableList<AndClause> newClauses =
        ImmutableList.copyOf(Iterables.concat(
            e1.getAndClauses(), e2.getAndClauses()));
    return new AutoValue_Event(newClauses);
  }

  public static Event and(Event e1, Event e2) {
    List<AndClause> newClauses = Lists.newArrayList();
    for (AndClause clause1 : e1.getAndClauses()) {
      for (AndClause clause2 : e2.getAndClauses()) {
        AndClause newAndClause = new AutoValue_Event_AndClause(
            ImmutableList.copyOf(Iterables.concat(
                clause1.getPositiveCases(),
                clause2.getPositiveCases())),
            ImmutableList.copyOf(Iterables.concat(
                clause1.getNegativeCases(),
                clause2.getNegativeCases())));
        newClauses.add(newAndClause);
      }
    }
    return new AutoValue_Event(ImmutableList.copyOf(newClauses));
  }
}
