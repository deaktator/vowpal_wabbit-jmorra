package vw.learner;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.IOException;

/**
 * An example of why we might like <code>VWFactory.getVWLearner(String command)</code> to be public.
 * Created by deak on 10/29/15.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class VWFactoryGetLearnerTest {

    @Test
    public void testJson() throws IOException {
        final VWLearner vwLearner = VWFactory.getVWLearner("--cb 4");
        try {
            final JsonWriter jsonWriter = JsonWriters.getJsonWriter(vwLearner);
            final String example = "a b c";
            final String response = jsonWriter.jsonValue(example);
            final String expected = "{\"input\":\"a b c\",\"response\":1}";
            final String actual = "{\"input\":\"" + example + "\",\"response\": " + response + "}";
            assertEquals(expected, actual);
        }
        finally {
            vwLearner.close();
        }
    }

    private static class JsonWriters {
        public static JsonWriter getJsonWriter(final VWLearner learner) {
            if (learner instanceof VWFloatArrayLearner)
                return new FloatArrayJsonWriter((VWFloatArrayLearner) learner);
            else if (learner instanceof VWIntLearner)
                return new IntJsonWriter((VWIntLearner) learner);
            else
                throw new IllegalArgumentException(
                        "Only Support VWFloatArrayLearner, VWIntLearner.  Given " +
                        learner.getClass().getCanonicalName());
        }
    }

    private interface JsonWriter {
        String jsonValue(String example);
    }

    private static class IntJsonWriter implements JsonWriter {
        private final VWIntLearner learner;
        private IntJsonWriter(final VWIntLearner learner) {
            this.learner = learner;
        }
        public String jsonValue(final String example) {
            return String.valueOf(learner.predict(example));
        }
    }

    private static class FloatArrayJsonWriter implements JsonWriter {
        private final VWFloatArrayLearner learner;
        private FloatArrayJsonWriter(final VWFloatArrayLearner learner) {
            this.learner = learner;
        }
        public String jsonValue(final String example) {
            final float[] prediction = learner.predict(example);
            final StringBuilder b = new StringBuilder().append("[");
            if (2 < prediction.length) {
                for (float p: prediction) {
                    b.append(p);
                }
            }
            else {
                b.append(prediction[0]);
                for (int i = 1; i < prediction.length; ++i) {
                    b.append(",").append(prediction[i]);
                }
            }

            return b.append("]").toString();
        }
    }
}
