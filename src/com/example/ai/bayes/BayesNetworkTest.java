package com.example.ai.bayes;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

public class BayesNetworkTest extends TestCase {

  private static final double DELTA = 0.000001;

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

  /**
   * Constructs a network based on the following exercise:
   * (PDF) https://cw.felk.cvut.cz/wiki/_media/courses/ae4m33rzn/bn_solved.pdf
   */
  private BayesNetwork getTramNetwork() {
    return BayesNetwork.builder()
        .add(ConditionalDistribution.forVariable("Time")
            .setProbability(1/3d, "Evening")
            .setProbability(2/3d, "Daytime")
            .build())
        .add(ConditionalDistribution.forVariable("Line")
            .setProbability(5/10d, "Line22")
            .setProbability(3/10d, "Line24")
            .setProbability(2/10d, "Line6")
            .build())
        .add(ConditionalDistribution.forVariable("TramLength")
            .setParents("Time", "Line")
            .setProbability(1/10d, "Short", "Evening", "Line22")
            .setProbability(9/10d, "Long", "Evening", "Line22")
            .setProbability(0d, "Short", "Daytime", "Line22")
            .setProbability(1d, "Long", "Daytime", "Line22")
            .setProbability(2/10d, "Short", "Evening", "Line24")
            .setProbability(8/10d, "Long", "Evening", "Line24")
            .setProbability(2/10d, "Short", "Daytime", "Line24")
            .setProbability(8/10d, "Long", "Daytime", "Line24")
            .setProbability(1d, "Short", "Evening", "Line6")
            .setProbability(0d, "Long", "Evening", "Line6")
            .setProbability(9/10d, "Short", "Daytime", "Line6")
            .setProbability(1/10d, "Long", "Daytime", "Line6")
            .build())
        .add(ConditionalDistribution.forVariable("Direction")
            .setParents("Line")
            .setProbability(1/10d, "Albertov", "Line22")
            .setProbability(9/10d, "Pavlova", "Line22")
            .setProbability(9/10d, "Albertov", "Line24")
            .setProbability(1/10d, "Pavlova", "Line24")
            .setProbability(1/10d, "Albertov", "Line6")
            .setProbability(9/10d, "Pavlova", "Line6")
            .build())
        .build();
  }

  public void testQueryTram() {
    BayesNetwork network = getTramNetwork();

    // Compute the probability that a tram is going to Albertov, given
    // that it is evening and the tram is short.
    Event queryEvent = Event.of("Direction", "Albertov");
    Event evidence = Event.and(
        Event.of("Time", "Evening"), Event.of("TramLength", "Short"));

    double probability = network.queryProbability(queryEvent, evidence);
    assertEquals(79/310d, probability, DELTA);

    // Compute the probability that a tram is long, given that it belongs
    // to Line 22.
    queryEvent = Event.of("TramLength", "Long");
    evidence = Event.of("Line", "Line22");

    probability = network.queryProbability(queryEvent, evidence);
    assertEquals(29/30d, probability, DELTA);
  }
}
