package com.example.ai.bayes;

import junit.framework.TestCase;

public class BayesNetworkTest extends TestCase {
  /**
   * An example based on a Martin Gardner probability puzzle, which in turn is
   * based on a variant of the short story "The Lady and the Tiger".
   */
  public void testQuery() {
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
    BayesNetwork network = builder.build();

    Event queryEvent = Event.of("Reveal3", "L");
    Event evidence = Event.and(
        Event.of("Reveal1", "L"), Event.of("Reveal2", "L"));

    Double probability = network.queryProbability(queryEvent, evidence);

    assertEquals(9/10d, probability, 0.001 /* delta */);
  }
}
