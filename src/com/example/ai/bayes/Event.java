package com.example.ai.bayes;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class Event {
  public abstract ImmutableList<? extends AndClause> getAndClauses();

  @AutoValue
  public abstract static class AndClause {
    public abstract ImmutableSetMultimap<String, ? extends Condition>
        getConditions();

    public Event negate() {
      List<AndClause> negatedClauses = Lists.newArrayList();
      for (Map.Entry<String, ? extends Condition> entry
          : getConditions().entries()) {
        String variable = entry.getKey();
        Condition condition = entry.getValue();
        negatedClauses.add(AndClause.of(
            ImmutableSetMultimap.of(variable, condition.negate())));
      }
      return Event.fromAndClauses(ImmutableList.copyOf(negatedClauses));
    }

    public static AndClause concat(AndClause clause1, AndClause clause2) {
      ImmutableSetMultimap<String, Condition> newConditions =
          ImmutableSetMultimap.<String, Condition>builder()
              .putAll(clause1.getConditions())
              .putAll(clause2.getConditions())
              .build();
      return AndClause.of(newConditions);
    }

    public static AndClause of(
        SetMultimap<String, ? extends Condition> conditions) {
      return new AutoValue_Event_AndClause(
          ImmutableSetMultimap.copyOf(conditions));
    }

    public static AndClause equal(String variable, String value) {
      return of(ImmutableSetMultimap.of(variable, Condition.equal(value)));
    }

    public static AndClause notEqual(String variable, String value) {
      return of(ImmutableSetMultimap.of(variable, Condition.notEqual(value)));
    }

    /**
     * An empty AND-clause represents an always-true predicate.
     */
    public static AndClause alwaysTrue() {
      return of(ImmutableSetMultimap.<String, Condition>of());
    }
  }

  public static enum ConditionType {
    EQUAL,
    NOT_EQUAL;

    public ConditionType negate() {
      switch (this) {
        case EQUAL:
          return NOT_EQUAL;
        case NOT_EQUAL:
          return EQUAL;
        default:
          throw new AssertionError("Unhandled ConditionType " + this);
      }
    }
  }

  @AutoValue
  public abstract static class Condition {
    public abstract ConditionType getType();
    public abstract String getValue();

    public static Condition equal(String value) {
      return new AutoValue_Event_Condition(ConditionType.EQUAL, value);
    }

    public static Condition notEqual(String value) {
      return new AutoValue_Event_Condition(ConditionType.NOT_EQUAL, value);
    }

    public Condition negate() {
      return new AutoValue_Event_Condition(getType().negate(), getValue());
    }
  }

  public static Event varEquals(String variable, String value) {
    return fromAndClauses(AndClause.equal(variable, value));
  }

  public static Event not(Event e) {
    // Note: an event with no clauses is always false (empty OR-statement),
    // so we start with an always true event (a single empty AND-statement).
    Event result = Event.alwaysTrue();
    for (AndClause andClause : e.getAndClauses()) {
      result = and(result, andClause.negate());
    }
    return result;
  }

  public static Event fromAndClauses(AndClause... andClause) {
    return fromAndClauses(ImmutableList.copyOf(andClause));
  }

  public static Event fromAndClauses(Iterable<? extends AndClause> andClauses) {
    return new AutoValue_Event(ImmutableList.copyOf(andClauses));
  }

  public static Event or(Iterable<? extends Event> events) {
    Event result = Event.alwaysFalse();
    for (Event event : events) {
      result = or(result, event);
    }
    return result;
  }

  public static Event or(Event e1, Event e2) {
    ImmutableList<AndClause> newClauses =
        ImmutableList.copyOf(Iterables.concat(
            e1.getAndClauses(), e2.getAndClauses()));
    return fromAndClauses(newClauses);
  }

  public static Event and(Iterable<? extends Event> events) {
    Event result = Event.alwaysTrue();
    for (Event event : events) {
      result = and(result, event);
    }
    return result;
  }

  public static Event and(Event e1, Event e2) {
    List<AndClause> newClauses = Lists.newArrayList();
    for (AndClause clause1 : e1.getAndClauses()) {
      for (AndClause clause2 : e2.getAndClauses()) {
        newClauses.add(AndClause.concat(clause1, clause2));
      }
    }
    return fromAndClauses(ImmutableList.copyOf(newClauses));
  }

  /** Returns a single empty AND-clause, which always evaluates to true. */
  public static Event alwaysTrue() {
    return fromAndClauses(AndClause.alwaysTrue());
  }

  /** Returns an empty OR-clause, which always evaluates to false. */
  public static Event alwaysFalse() {
    return fromAndClauses(ImmutableList.<AndClause>of());
  }
}
