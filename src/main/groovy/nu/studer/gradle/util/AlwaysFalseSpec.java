package nu.studer.gradle.util;

import org.gradle.api.Task;
import org.gradle.api.specs.Spec;

/**
 * Spec that always evaluates to false.
 */
public final class AlwaysFalseSpec implements Spec<Task> {

    public static final AlwaysFalseSpec INSTANCE = new AlwaysFalseSpec();

    private AlwaysFalseSpec() {
    }

    @Override
    public boolean isSatisfiedBy(Task element) {
        return false;
    }

}
