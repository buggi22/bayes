# bayes

Compute probabilities using Bayesian networks.

This is intended to be a "toy" implementation for educational purposes.  The code is not designed for efficiency and is particularly susceptible to numerical rounding errors.

## Usage

Consider the example outlined [here](http://en.wikipedia.org/wiki/Bayesian_network#Example).  The following code illustrates how to calculate the probability that it is raining, given that the grass is wet.

```
  private static final double DELTA = 0.000001;

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
```

## Unit tests

Unit test can be found under the [`test`](/test/com/example/ai/bayes) directory.

In particular, examples corresponding to a handful of probability exercises can be found in [`ExampleTest.java`](/test/com/example/ai/bayes/ExampleTest.java).

## Dependencies

This project depends on the following libraries:
- JUnit 3
- Google Guava
- Google AutoValue

Google AutoValue, in particular, requires annotation processing to be enabled during compilation.

