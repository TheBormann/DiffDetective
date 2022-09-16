package org.variantsync.diffdetective.diff.difftree.filter;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import static org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns.*;

/**
 * A filter on DiffTrees that is equipped with some metadata T (e.g., for debugging or logging).
 * The condition determines whether a DiffTree should be considered for computation or not.
 * Iff the condition returns true, the DiffTree should be considered.
 * @author Paul Bittner
 */
public final class DiffTreeFilter {
    /**
     * Returns a tagged predicate that always returns true and is tagged with the given metadata.
     */
    public static <T> TaggedPredicate<T, DiffTree> Any(final T metadata) {
        return TaggedPredicate.Any(metadata);
    }

    /**
     * Returns a tagged predicate that always returns true and is tagged with the String {@code "any"}.
     */
    public static TaggedPredicate<String, DiffTree> Any() {
        return Any("any");
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the DiffTree has more than one artifact node ({@link DiffNode#isCode()}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static TaggedPredicate<String, DiffTree> moreThanOneCodeNode() {
        return new TaggedPredicate<>(
                "has more than one elementary pattern",
                tree -> tree.count(DiffNode::isCode) > 1
        );
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the DiffTree is not empty ({@link DiffTree#isEmpty()}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static TaggedPredicate<String, DiffTree> notEmpty() {
        return new TaggedPredicate<>(
            "is not empty",
                tree -> !tree.isEmpty()
        );
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the DiffTree is {@link DiffTree#isConsistent() consistent}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static TaggedPredicate<String, DiffTree> consistent() {
        return new TaggedPredicate<>(
                "is consistent",
                tree -> tree.isConsistent().isSuccess()
        );
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the DiffTree has at least one artifact node ({@link DiffNode#isCode()})
     * that does not match any pattern of
     * {@link org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns#AddToPC},
     * {@link org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns#RemFromPC},
     * {@link org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns#Untouched}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static TaggedPredicate<String, DiffTree> hasAtLeastOneEditToVariability() {
        return new TaggedPredicate<>(
                "has edits to variability",
                tree -> tree.anyMatch(n ->
                        n.isCode() && !AddToPC.matches(n) && !RemFromPC.matches(n) && !Untouched.matches(n)
                )
        );
    }
}
