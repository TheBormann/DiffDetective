package preliminary.pattern;

import pattern.EditPattern;
import preliminary.analysis.data.PatternMatch;
import preliminary.evaluation.FeatureContext;

import java.util.Optional;

@Deprecated
public interface FeatureContextReverseEngineering<E> {
    EditPattern<E> getPattern();
    /**
     * Creates a PatternMatch object for the given element.
     * Assumes {@code matches(codeNode) == true}.
     * @param e An element that was matched to this pattern.
     * @return A PatternMatch object containing metadata when matching this pattern to the given node.
     */
    PatternMatch<E> createMatch(E e);
    FeatureContext[] getFeatureContexts(PatternMatch<E> patternMatch);

    static <E> Optional<PatternMatch<E>> match(FeatureContextReverseEngineering<E> reverseEngineering, E x) {
        if (reverseEngineering.getPattern().matches(x)) {
            return Optional.of(reverseEngineering.createMatch(x));
        }

        return Optional.empty();
    }
}
