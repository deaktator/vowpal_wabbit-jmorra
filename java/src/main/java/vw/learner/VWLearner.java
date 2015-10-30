package vw.learner;

import java.io.Closeable;

/**
 * This is the super type of all different typed VW learners.  This type exists to ensure that the
 * {@link VWLearners#create(String)} method has a super type.
 */
public interface VWLearner extends Closeable {
    void close();
}
