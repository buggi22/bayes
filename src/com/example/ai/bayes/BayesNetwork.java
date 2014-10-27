package com.example.ai.bayes;

import com.example.ai.bayes.Event.AndClause;
import com.example.ai.bayes.Event.Condition;
import com.google.auto.value.AutoValue;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@AutoValue
public abstract class BayesNetwork {
  public abstract ImmutableList<ConditionalDistribution>
      getConditionalDistributions();

  /**
   * Computes the probability of an {@link Event}, conditioned on evidence
   * from another event.
   */
  public double queryProbability(Event queryEvent, Event evidence) {
    return queryProbability(Event.and(queryEvent, evidence))
        / queryProbability(evidence);
  }

  /**
   * Computes the a-priori probability of a given {@link Event}
   */
  public double queryProbability(Event queryEvent) {
    if (queryEvent.getAndClauses().isEmpty()) {
      return 0d;
    }
    if (queryEvent.getAndClauses().size() == 1) {
      return getProbabilityForSingleAndClause(
          queryEvent.getAndClauses().get(0));
    }

    AndClause firstClause = queryEvent.getAndClauses().get(0);
    ImmutableList<? extends AndClause> remainingClauses =
        queryEvent.getAndClauses().subList(
            1, queryEvent.getAndClauses().size());

    Event firstClauseEvent = Event.fromAndClauses(firstClause);
    Event remainingOrEvent = Event.fromAndClauses(remainingClauses);

    return queryProbability(firstClauseEvent)
        + queryProbability(remainingOrEvent)
        - queryProbability(Event.and(firstClauseEvent, remainingOrEvent));
  }

  private double getProbabilityForSingleAndClause(AndClause andClause) {
    ImmutableMap.Builder<String, String> partialAssignment =
        ImmutableMap.builder();
    ImmutableMultimap.Builder<String, String> remainingCombinations =
        ImmutableMultimap.builder();
    for (String variable : andClause.getConditions().keySet()) {
      Collection<? extends Condition> conditions =
          andClause.getConditions().get(variable);
      Set<String> allowedValues = getAllowedValues(variable, conditions);
      if (allowedValues.isEmpty()) {
        // There's no way to satisfy the given conditions, so the probability
        // is zero.
        return 0;
      } else if (allowedValues.size() == 1) {
        partialAssignment.put(
            variable, Iterables.getOnlyElement(allowedValues));
      } else {
        remainingCombinations.putAll(variable, allowedValues);
      }
    }

    // Handle all nuisance variables (the variables that don't appear directly
    // in this clause).
    for (String nuisanceVar : getNuisanceVariables(andClause)) {
      remainingCombinations.putAll(nuisanceVar, getValues(nuisanceVar));
    }

    return getProbabilityRecursive(
        partialAssignment.build(), remainingCombinations.build());
  }

  private ImmutableSet<String> getAllowedValues(String variable,
      Collection<? extends Condition> conditions) {
    Set<String> result = getValues(variable);
    for (Condition condition : conditions) {
      switch (condition.getType()) {
        case EQUAL:
          result = Sets.filter(
              result, Predicates.equalTo(condition.getValue()));
          break;
        case NOT_EQUAL:
          result = Sets.filter(
              result, Predicates.not(Predicates.equalTo(condition.getValue())));
          break;
        default:
          throw new AssertionError(
              "Unhandled condition type " + condition.getType());
      }
    }
    return ImmutableSet.copyOf(result);
  }

  private double getProbabilityRecursive(
      ImmutableMap<String, String> partialAssignment,
      ImmutableMultimap<String, String> remainingCombinations) {
    if (remainingCombinations.isEmpty()) {
      return getJointProbability(partialAssignment);
    }

    String firstVar = remainingCombinations.keySet().iterator().next();
    ImmutableMultimap.Builder<String, String> newRemainingCombinations =
        ImmutableMultimap.builder();
    for (String var : remainingCombinations.keySet()) {
      if (!var.equals(firstVar)) {
        newRemainingCombinations.putAll(var, remainingCombinations.get(var));
      }
    }

    double result = 0;
    for (String valueForFirstVar : remainingCombinations.get(firstVar)) {
      ImmutableMap<String, String> newPartialAssignment =
          ImmutableMap.<String, String>builder()
              .putAll(partialAssignment)
              .put(firstVar, valueForFirstVar)
              .build();
      result += getProbabilityRecursive(
          newPartialAssignment, newRemainingCombinations.build());
    }
    return result;
  }

  private double getJointProbability(ImmutableMap<String, String> assignment) {
    double result = 1.0;
    for (String var : getVariables()) {
      ImmutableList.Builder<String> key = ImmutableList.<String>builder()
          .add(assignment.get(var));
      ImmutableList<String> parentVariables =
          getDistribution(var).getParentVariableNames();
      for (String parentVar : parentVariables) {
        key.add(assignment.get(parentVar));
      }
      double conditionalProbability =
          getDistribution(var).getProbabilities().get(key.build());
      result *= conditionalProbability;
    }
    return result;
  }

  private ConditionalDistribution getDistribution(String variableName) {
    for (ConditionalDistribution distribution : getConditionalDistributions()) {
      if (distribution.getVariableName().equals(variableName)) {
        return distribution;
      }
    }
    throw new IllegalArgumentException(
        "Could not find distribution for variable named " + variableName);
  }

  private ImmutableSet<String> getValues(String variableName) {
    return getDistribution(variableName).getValues();
  }

  public ImmutableList<String> getVariables() {
    ImmutableList.Builder<String> result = ImmutableList.builder();
    for (ConditionalDistribution distribution : getConditionalDistributions()) {
      result.add(distribution.getVariableName());
    }
    return result.build();
  }

  /**
   * Gets all variables that are included in this {@link BayesNetwork} but
   * not in the given {@link AndClause}.
   */
  private ImmutableSet<String> getNuisanceVariables(AndClause andClause) {
    return ImmutableSet.copyOf(Sets.difference(
        ImmutableSet.copyOf(getVariables()),
        andClause.getConditions().keySet()));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final List<ConditionalDistribution> distributions =
        Lists.newArrayList();

    public Builder add(ConditionalDistribution distribution) {
      distributions.add(distribution);
      return this;
    }

    public BayesNetwork build() {
      return new AutoValue_BayesNetwork(ImmutableList.copyOf(distributions));
    }
  }
}
