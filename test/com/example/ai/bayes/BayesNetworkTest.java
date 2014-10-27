package com.example.ai.bayes;

import static com.example.ai.bayes.Event.and;
import static com.example.ai.bayes.Event.not;
import static com.example.ai.bayes.Event.or;
import static com.example.ai.bayes.Event.varEquals;
import junit.framework.TestCase;

public class BayesNetworkTest extends TestCase {
  private static final double DELTA = 0.000001;

  private BayesNetwork getIndependentNetwork() {
    return BayesNetwork.builder()
        .add(ConditionalDistribution.forVariable("X")
            .setProbability(1/5d,  "X1")
            .setProbability(3/10d, "X2")
            .setProbability(1/2d,  "X3")
            .build())
        .add(ConditionalDistribution.forVariable("Y")
            .setProbability(1/20d, "Y1")
            .setProbability(1/4d,  "Y2")
            .setProbability(7/10d, "Y3")
            .build())
        .build();
  }

  public void testIndependent() {
    BayesNetwork network = getIndependentNetwork();

    double probability = network.queryProbability(varEquals("X", "X1"));
    assertEquals(1/5d, probability, DELTA);

    probability = network.queryProbability(varEquals("X", "X2"));
    assertEquals(3/10d, probability, DELTA);

    probability = network.queryProbability(varEquals("X", "X3"));
    assertEquals(1/2d, probability, DELTA);

    probability = network.queryProbability(varEquals("Y", "Y1"));
    assertEquals(1/20d, probability, DELTA);

    probability = network.queryProbability(varEquals("Y", "Y2"));
    assertEquals(1/4d, probability, DELTA);

    probability = network.queryProbability(varEquals("Y", "Y3"));
    assertEquals(7/10d, probability, DELTA);
  }

  public void testIndependentNot() {
    BayesNetwork network = getIndependentNetwork();

    double probability = network.queryProbability(
        not(varEquals("X", "X1")));
    assertEquals(8/10d, probability, DELTA);
  }

  public void testIndependentAnd() {
    BayesNetwork network = getIndependentNetwork();

    double probability = network.queryProbability(
        and(varEquals("X", "X1"), varEquals("X", "X2")));
    assertEquals(0d, probability, DELTA);

    probability = network.queryProbability(
        and(varEquals("X", "X1"), varEquals("X", "X1")));
    assertEquals(1/5d, probability, DELTA);

    probability = network.queryProbability(
        and(varEquals("X", "X3"), varEquals("Y", "Y2")));
    assertEquals(1/8d, probability, DELTA);
  }

  public void testIndependentOr() {
    BayesNetwork network = getIndependentNetwork();

    double probability = network.queryProbability(
        or(varEquals("X", "X1"), varEquals("X", "X2")));
    assertEquals(1/2d, probability, DELTA);

    probability = network.queryProbability(
        or(varEquals("X", "X1"), varEquals("X", "X1")));
    assertEquals(1/5d, probability, DELTA);

    probability = network.queryProbability(
        or(varEquals("X", "X3"), varEquals("Y", "Y2")));
    assertEquals(5/8d, probability, DELTA);
  }

  private BayesNetwork getConditionalNetwork() {
    return BayesNetwork.builder()
        .add(ConditionalDistribution.forVariable("X")
            .setProbability(1/5d, "X1")
            .setProbability(3/10d, "X2")
            .setProbability(1/2d, "X3")
            .build())
        .add(ConditionalDistribution.forVariable("Y")
            .setParents("X")
            .setProbability(1/20d, "Y1", "X1")
            .setProbability(1/4d, "Y2", "X1")
            .setProbability(7/10d, "Y3", "X1")
            .setProbability(3/10d, "Y1", "X2")
            .setProbability(3/10d, "Y2", "X2")
            .setProbability(4/10d, "Y3", "X2")
            .setProbability(1/10d, "Y1", "X3")
            .setProbability(8/10d, "Y2", "X3")
            .setProbability(1/10d, "Y3", "X3")
            .build())
        .add(ConditionalDistribution.forVariable("Z")
            .setParents("X")
            .setProbability(1/5d, "Z1", "X1")
            .setProbability(4/5d, "Z2", "X1")
            .setProbability(1/4d, "Z1", "X2")
            .setProbability(3/4d, "Z2", "X2")
            .setProbability(1/10d, "Z1", "X3")
            .setProbability(9/10d, "Z2", "X3")
            .build())
        .add(ConditionalDistribution.forVariable("W")
            .setParents("X", "Y")
            .setProbability(1/10d, "W1", "X1", "Y1")
            .setProbability(9/10d, "W2", "X1", "Y1")
            .setProbability(1/5d,  "W1", "X2", "Y1")
            .setProbability(4/5d,  "W2", "X2", "Y1")
            .setProbability(3/10d, "W1", "X3", "Y1")
            .setProbability(7/10d, "W2", "X3", "Y1")
            .setProbability(2/5d,  "W1", "X1", "Y2")
            .setProbability(3/5d,  "W2", "X1", "Y2")
            .setProbability(1/2d,  "W1", "X2", "Y2")
            .setProbability(1/2d,  "W2", "X2", "Y2")
            .setProbability(3/5d,  "W1", "X3", "Y2")
            .setProbability(2/5d,  "W2", "X3", "Y2")
            .setProbability(7/10d, "W1", "X1", "Y3")
            .setProbability(3/10d, "W2", "X1", "Y3")
            .setProbability(4/5d,  "W1", "X2", "Y3")
            .setProbability(1/5d,  "W2", "X2", "Y3")
            .setProbability(9/10d, "W1", "X3", "Y3")
            .setProbability(1/10d, "W2", "X3", "Y3")
            .build())
        .build();
  }

  public void testConditional() {
    BayesNetwork network = getConditionalNetwork();

    double probability = network.queryProbability(varEquals("X", "X1"));
    assertEquals(1/5d, probability, DELTA);

    probability = network.queryProbability(varEquals("X", "X2"));
    assertEquals(3/10d, probability, DELTA);

    probability = network.queryProbability(varEquals("X", "X3"));
    assertEquals(1/2d, probability, DELTA);

    probability = network.queryProbability(varEquals("Y", "Y1"));
    assertEquals(3/20d, probability, DELTA);

    probability = network.queryProbability(varEquals("Y", "Y2"));
    assertEquals(27/50d, probability, DELTA);

    probability = network.queryProbability(varEquals("Y", "Y3"));
    assertEquals(31/100d, probability, DELTA);
  }

  public void testConditionalWithEvidence() {
    BayesNetwork network = getConditionalNetwork();

    double probability = network.queryProbability(
        varEquals("X", "X1"),
        varEquals("Y", "Y1"));
    assertEquals(1/15d, probability, DELTA);

    probability = network.queryProbability(
        varEquals("X", "X2"),
        varEquals("Y", "Y1"));
    assertEquals(3/5d, probability, DELTA);
  }

  public void testConditionalNot() {
    BayesNetwork network = getConditionalNetwork();

    double probability = network.queryProbability(
        not(varEquals("X", "X1")),
        not(varEquals("Y", "Y1")));
    assertEquals(66/85d, probability, DELTA);
  }

  public void testConditionalAnd() {
    BayesNetwork network = getConditionalNetwork();

    double probability = network.queryProbability(
        and(varEquals("X", "X1"), varEquals("Y", "Y1")),
        and(varEquals("Z", "Z1"), varEquals("W", "W1")));
    assertEquals(4/1871d, probability, DELTA);
  }

  public void testConditionalOr() {
    BayesNetwork network = getConditionalNetwork();

    double probability = network.queryProbability(
        or(varEquals("X", "X2"), varEquals("X", "X3")),
        or(varEquals("Y", "Y2"), varEquals("Y", "Y3")));
    // NOTE: Same result as P(X != X1 | Y != Y1)
    assertEquals(66/85d, probability, DELTA);

    probability = network.queryProbability(
        or(varEquals("X", "X1"), varEquals("Y", "Y1")),
        or(varEquals("Z", "Z1"), varEquals("W", "W1")));
    assertEquals(3794/12989d, probability, DELTA);
  }
}
