package com.example.ai.bayes;

import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

public class ConditionalDistributionTest extends TestCase {
  public void testGetValues() {
    ConditionalDistribution dist = ConditionalDistribution.forVariable("X")
        .setParents("Y", "Z")
        .setProbability(0.1d, "X1", "Y1", "Z1")
        .setProbability(0.1d, "X2", "Y1", "Z1")
        .setProbability(0.1d, "X1", "Y2", "Z1")
        .setProbability(0.1d, "X2", "Y2", "Z1")
        .setProbability(0.1d, "X1", "Y1", "Z2")
        .setProbability(0.1d, "X2", "Y1", "Z2")
        .setProbability(0.1d, "X1", "Y2", "Z2")
        .setProbability(0.3d, "X2", "Y2", "Z2")
        .build();
    assertEquals(ImmutableSet.of("X1", "X2"), dist.getValues());
  }
}
