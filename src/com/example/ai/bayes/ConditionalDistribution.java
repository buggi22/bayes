package com.example.ai.bayes;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class ConditionalDistribution {
  public abstract String getVariableName();
  public abstract ImmutableList<String> getParentVariableNames();
  public abstract ImmutableMap<ImmutableList<String>, Double>
      getProbabilities();

  public static Builder forVariable(String variableName) {
    return new Builder(variableName);
  }

  public static class Builder {
    private final String variableName;
    private final List<String> parentVariableNames = Lists.newArrayList();
    private final Map<ImmutableList<String>, Double> probabilities =
        Maps.newLinkedHashMap();

    private Builder(String variableName) {
      this.variableName = variableName;
    }

    public Builder setProbability(
        double probability, String firstValue, String... otherValues) {
      ImmutableList<String> values = ImmutableList.<String>builder()
          .add(firstValue)
          .addAll(ImmutableList.copyOf(otherValues))
          .build();
      probabilities.put(values, probability);
      return this;
    }

    public Builder setParents(String... parents) {
      parentVariableNames.clear();
      parentVariableNames.addAll(ImmutableList.copyOf(parents));
      return this;
    }

    public ConditionalDistribution build() {
      return new AutoValue_ConditionalDistribution(
          variableName,
          ImmutableList.copyOf(parentVariableNames),
          ImmutableMap.copyOf(probabilities));
    }
  }
}
