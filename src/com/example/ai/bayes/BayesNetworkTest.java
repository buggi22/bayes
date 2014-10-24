package com.example.ai.bayes;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

public class BayesNetworkTest extends TestCase {

  private static final double DELTA = 0.001;

  /**
   * An example based on a Martin Gardner probability puzzle, which in turn is
   * based on a variant of the short story "The Lady, or the Tiger?".
   */
  private BayesNetwork getLadyOrTigerNetwork() {
    BayesNetwork.Builder builder = BayesNetwork.builder()
        .add(ConditionalDistribution.forVariable("InitialChoice")
            .setProbability(1/3d, "TT")
            .setProbability(1/3d, "TL")
            .setProbability(1/3d, "LL")
            .build());
    for (int i = 1; i <= 3; i++) {
      builder.add(ConditionalDistribution.forVariable("Reveal" + i)
          .setParents("InitialChoice")
          .setProbability(1, "T", "TT")
          .setProbability(0, "L", "TT")
          .setProbability(1/2d, "T", "TL")
          .setProbability(1/2d, "L", "TL")
          .setProbability(0, "T", "LL")
          .setProbability(1, "L", "LL")
          .build());
    }
    return builder.build();
  }

  /**
   * Tests queries that do not involve any evidence.
   */
  public void testQueryLadyOrTigerWithoutEvidence() {
    BayesNetwork network = getLadyOrTigerNetwork();

    // Compute the probability of surviving the first round.
    Event queryEvent = Event.of("Reveal1", "L");

    double probability = network.queryProbability(queryEvent);
    assertEquals(1/2d, probability, DELTA);

    // Compute the probability of surviving all 3 rounds.
    queryEvent = Event.and(ImmutableList.of(
        Event.of("Reveal1", "L"),
        Event.of("Reveal2", "L"),
        Event.of("Reveal3", "L")));

    probability = network.queryProbability(queryEvent);
    assertEquals(3/8d, probability, DELTA);

    // Compute the probability of picking the pair of doors with two tigers.
    queryEvent = Event.of("InitialChoice", "TT");

    probability = network.queryProbability(queryEvent);
    assertEquals(1/3d, probability, DELTA);

    // Compute the probability of picking a pair of doors with at least one
    // tiger.
    queryEvent = Event.or(
        Event.of("InitialChoice", "TT"), Event.of("InitialChoice", "TL"));

    probability = network.queryProbability(queryEvent);
    assertEquals(2/3d, probability, DELTA);
  }

  /**
   * Tests queries that involve evidence.
   */
  public void testQueryLadyOrTigerWithEvidence() {
    BayesNetwork network = getLadyOrTigerNetwork();

    // Compute the probability of surviving all 3 rounds, given that the
    // protagonist has survived through two rounds so far.
    Event queryEvent = Event.of("Reveal3", "L");
    Event evidence = Event.and(
        Event.of("Reveal1", "L"), Event.of("Reveal2", "L"));

    double probability = network.queryProbability(queryEvent, evidence);
    assertEquals(9/10d, probability, DELTA);

    // Should get the same result when the query event contains the evidence.
    queryEvent = Event.and(ImmutableList.of(
        Event.of("Reveal1", "L"),
        Event.of("Reveal2", "L"),
        Event.of("Reveal3", "L")));

    probability = network.queryProbability(queryEvent, evidence);
    assertEquals(9/10d, probability, DELTA);

    // Compute the probability of surviving all 3 rounds, given that the
    // protagonist has survived through one round so far.
    evidence = Event.of("Reveal1", "L");

    probability = network.queryProbability(queryEvent, evidence);
    assertEquals(3/4d, probability, DELTA);
  }
}
