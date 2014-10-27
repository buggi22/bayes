package com.example.ai.bayes;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Represents a conditional probability table, to be used in a
 * {@link BayesNetwork}.
 */
@AutoValue
public abstract class ConditionalDistribution {
  public abstract String getVariableName();
  public abstract ImmutableList<String> getParentVariableNames();
  public abstract ImmutableMap<ImmutableList<String>, Double>
      getProbabilities();

  /**
   * Gets the values that the current variable can take on.
   */
  public ImmutableSet<String> getValues() {
    ImmutableSet.Builder<String> values = ImmutableSet.builder();
    for (ImmutableList<String> key : getProbabilities().keySet()) {
      // The first value of the key corresponds to the value of the variable.
      // (The remaining values in the key correspond to values of the parent
      // variables, which can be ignored here.)
      values.add(key.get(0));
    }
    return values.build();
  }

  /**
   * Creates a builder for a conditional distribution with the given name.
   */
  public static Builder forVariable(String variableName) {
    return new Builder(variableName);
  }

  /**
   * A mutable builder for constructing instances of
   * {@link ConditionalDistribution}.
   */
  public static class Builder {
    private final String variableName;
    private ImmutableList<String> parentVariableNames = ImmutableList.of();
    private final Map<ImmutableList<String>, Double> probabilities =
        Maps.newLinkedHashMap();

    private Builder(String variableName) {
      this.variableName = variableName;
    }

    public Builder setProbability(
        double probability, String firstValue, String... otherValues) {
      Preconditions.checkState(
          otherValues.length == parentVariableNames.size());
      Preconditions.checkState(!Double.isNaN(probability));
      Preconditions.checkState(!Double.isInfinite(probability));
      Preconditions.checkState(probability >= 0d);
      Preconditions.checkState(probability <= 1d);
      ImmutableList<String> values = ImmutableList.<String>builder()
          .add(firstValue)
          .addAll(ImmutableList.copyOf(otherValues))
          .build();
      probabilities.put(values, probability);
      return this;
    }

    public Builder setParents(String... parents) {
      parentVariableNames = ImmutableList.copyOf(parents);
      return this;
    }

    public ConditionalDistribution build() {
      // TODO: check that the probabilities sum correctly to 1 with the right
      // combinations of parent variables.
      return new AutoValue_ConditionalDistribution(
          variableName,
          parentVariableNames,
          ImmutableMap.copyOf(probabilities));
    }
  }
}
