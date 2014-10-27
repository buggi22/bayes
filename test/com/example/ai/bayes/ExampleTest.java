package com.example.ai.bayes;

import static com.example.ai.bayes.Event.and;
import static com.example.ai.bayes.Event.or;
import static com.example.ai.bayes.Event.varEquals;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

public class ExampleTest extends TestCase {

  private static final double DELTA = 0.000001;

  /**
   * An example based on a Martin Gardner probability puzzle, which in turn is
   * based on a variant of the short story "The Lady, or the Tiger?".
   */
  private BayesNetwork getLadyOrTigerNetwork() {
    BayesNetwork.Builder builder = BayesNetwork.builder()
        .add(ConditionalDistribution
            .forVariable("InitialChoice")
            .setProbability(1/3d, "TT")
            .setProbability(1/3d, "TL")
            .setProbability(1/3d, "LL")
            .build());
    for (int i = 1; i <= 3; i++) {
      builder.add(ConditionalDistribution
          .forVariable("Reveal" + i)
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
    Event queryEvent = varEquals("Reveal1", "L");

    double probability = network.queryProbability(queryEvent);
    assertEquals(1/2d, probability, DELTA);

    // Compute the probability of surviving all 3 rounds.
    queryEvent = and(ImmutableList.of(
        varEquals("Reveal1", "L"),
        varEquals("Reveal2", "L"),
        varEquals("Reveal3", "L")));

    probability = network.queryProbability(queryEvent);
    assertEquals(3/8d, probability, DELTA);

    // Compute the probability of picking the pair of doors with two tigers.
    queryEvent = varEquals("InitialChoice", "TT");

    probability = network.queryProbability(queryEvent);
    assertEquals(1/3d, probability, DELTA);

    // Compute the probability of picking a pair of doors with at least one
    // tiger.
    queryEvent = or(
        varEquals("InitialChoice", "TT"), varEquals("InitialChoice", "TL"));

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
    Event queryEvent = varEquals("Reveal3", "L");
    Event evidence = and(
        varEquals("Reveal1", "L"), varEquals("Reveal2", "L"));

    double probability = network.queryProbabilityWithEvidence(queryEvent, evidence);
    assertEquals(9/10d, probability, DELTA);

    // Should get the same result when the query event contains the evidence.
    queryEvent = and(ImmutableList.of(
        varEquals("Reveal1", "L"),
        varEquals("Reveal2", "L"),
        varEquals("Reveal3", "L")));

    probability = network.queryProbabilityWithEvidence(queryEvent, evidence);
    assertEquals(9/10d, probability, DELTA);

    // Compute the probability of surviving all 3 rounds, given that the
    // protagonist has survived through one round so far.
    evidence = varEquals("Reveal1", "L");

    probability = network.queryProbabilityWithEvidence(queryEvent, evidence);
    assertEquals(3/4d, probability, DELTA);
  }

  /**
   * Constructs a network based on the following exercise:
   * (PDF) https://cw.felk.cvut.cz/wiki/_media/courses/ae4m33rzn/bn_solved.pdf
   */
  private BayesNetwork getTramNetwork() {
    return BayesNetwork.builder()
        .add(ConditionalDistribution
            .forVariable("Time")
            .setProbability(1/3d, "Evening")
            .setProbability(2/3d, "Daytime")
            .build())
        .add(ConditionalDistribution
            .forVariable("Line")
            .setProbability(5/10d, "Line22")
            .setProbability(3/10d, "Line24")
            .setProbability(2/10d, "Line6")
            .build())
        .add(ConditionalDistribution
            .forVariable("TramLength")
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
        .add(ConditionalDistribution
            .forVariable("Direction")
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
    Event queryEvent = varEquals("Direction", "Albertov");
    Event evidence = and(
        varEquals("Time", "Evening"),
        varEquals("TramLength", "Short"));

    double probability = network.queryProbabilityWithEvidence(queryEvent, evidence);
    assertEquals(79/310d, probability, DELTA);

    // Compute the probability that a tram is long, given that it belongs
    // to Line 22.
    queryEvent = varEquals("TramLength", "Long");
    evidence = varEquals("Line", "Line22");

    probability = network.queryProbabilityWithEvidence(queryEvent, evidence);
    assertEquals(29/30d, probability, DELTA);
  }

  /**
   * Tests a simple network corresponding to the following example:
   * http://en.wikipedia.org/wiki/Bayesian_network#Example
   */
  public void testGrassNetwork() {
    BayesNetwork network = BayesNetwork.builder()
        .add(ConditionalDistribution
            .forVariable("Rain")
            .setProbability(1/5d, "True")
            .setProbability(4/5d, "False")
            .build())
        .add(ConditionalDistribution
            .forVariable("Sprinkler")
            .setParents("Rain")
            .setProbability(2/5d, "True", "False")
            .setProbability(3/5d, "False", "False")
            .setProbability(1/100d, "True", "True")
            .setProbability(99/100d, "False", "True")
            .build())
        .add(ConditionalDistribution
            .forVariable("GrassWet")
            .setParents("Sprinkler", "Rain")
            .setProbability(0d, "True", "False", "False")
            .setProbability(1d, "False", "False", "False")
            .setProbability(4/5d, "True", "False", "True")
            .setProbability(1/5d, "False", "False", "True")
            .setProbability(9/10d, "True", "True", "False")
            .setProbability(1/10d, "False", "True", "False")
            .setProbability(99/100d, "True", "True", "True")
            .setProbability(1/100d, "False", "True", "True")
            .build())
        .build();

    Event queryEvent = varEquals("Rain", "True");
    Event evidence = varEquals("GrassWet", "True");

    double probability =
        network.queryProbabilityWithEvidence(queryEvent, evidence);
    assertEquals(891/2491d, probability, DELTA);
  }
}
