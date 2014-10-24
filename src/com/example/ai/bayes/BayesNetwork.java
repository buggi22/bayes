package com.example.ai.bayes;

import com.example.ai.bayes.Event.AndClause;
import com.example.ai.bayes.Event.Case;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

@AutoValue
public abstract class BayesNetwork {
  public abstract ImmutableList<ConditionalDistribution>
      getConditionalDistributions();

  private ConditionalDistribution getDistribution(String variableName) {
    for (ConditionalDistribution distribution : getConditionalDistributions()) {
      if (distribution.getVariableName().equals(variableName)) {
        return distribution;
      }
    }
    return null;
  }

  private Set<String> getValues(String variableName) {
    ConditionalDistribution distribution = getDistribution(variableName);
    ImmutableSet.Builder<String> values = ImmutableSet.builder();
    for (ImmutableList<String> key : distribution.getProbabilities().keySet()) {
      // The first value of the key corresponds to the value of the variable.
      // (The remaining values in the key correspond to values of the parent
      // variables, which can be ignored here.)
      values.add(key.get(0));
    }
    return values.build();
  }

  public ImmutableList<String> getVariables() {
    ImmutableList.Builder<String> result = ImmutableList.builder();
    for (ConditionalDistribution distribution : getConditionalDistributions()) {
      result.add(distribution.getVariableName());
    }
    return result.build();
  }

  public double queryProbability(Event queryEvent) {
    if (queryEvent.getAndClauses().isEmpty()) {
      return 0d;
    }
    if (queryEvent.getAndClauses().size() > 1) {
      // TODO: support this case
      throw new UnsupportedOperationException(
          "Querying probabilities for OR-expressions with more than one "
          + "AND-clause is not yet unsupported");
    }

    AndClause andClause = queryEvent.getAndClauses().get(0);
    ImmutableMap.Builder<String, String> partialAssignment =
        ImmutableMap.builder();
    for (Case positiveCase : andClause.getPositiveCases()) {
      partialAssignment.put(
          positiveCase.getVariable(), positiveCase.getValue());
    }

    ImmutableMultimap.Builder<String, String> remainingCombinations =
        ImmutableMultimap.builder();
    for (String nuisanceVar : getNuisanceVariables(andClause)) {
      remainingCombinations.putAll(nuisanceVar, getValues(nuisanceVar));
    }
    for (Case negativeCase : andClause.getNegativeCases()) {
      remainingCombinations.putAll(negativeCase.getVariable(),
          Sets.difference(
              getValues(negativeCase.getVariable()),
              Sets.newHashSet(negativeCase.getValue())));
    }
    return getProbabilityRecursive(
        partialAssignment.build(), remainingCombinations.build());
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

  /**
   * Gets all variables that are included in this {@link BayesNetwork} but
   * not in the given {@link AndClause}.
   */
  private List<String> getNuisanceVariables(AndClause andClause) {
    Set<String> result = Sets.newHashSet(getVariables());
    for (Case c : andClause.getPositiveCases()) {
      result.remove(c.getVariable());
    }
    for (Case c : andClause.getNegativeCases()) {
      result.remove(c.getVariable());
    }
    return ImmutableList.copyOf(result);
  }

  public Double queryProbability(Event queryEvent, Event evidence) {
    return queryProbability(Event.and(queryEvent, evidence))
        / queryProbability(evidence);
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
