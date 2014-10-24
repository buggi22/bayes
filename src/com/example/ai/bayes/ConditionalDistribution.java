package com.example.ai.bayes;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.Map;

@AutoValue
public abstract class ConditionalDistribution {
  public abstract String getVariableName();
  public abstract ImmutableList<String> getParentVariableNames();
  public abstract ImmutableMap<ImmutableList<String>, Double>
      getProbabilities();

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

  public static Builder forVariable(String variableName) {
    return new Builder(variableName);
  }

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
      return new AutoValue_ConditionalDistribution(
          variableName,
          parentVariableNames,
          ImmutableMap.copyOf(probabilities));
    }
  }
}
