package org.variantsync.diffdetective.pattern.elementary.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;

/**
 * Our AddToPC pattern from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class AddToPC extends ElementaryPattern {
    AddToPC() {
        super("AddToPC", DiffType.ADD);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode node) {
        return !node.getAfterParent().isAdd();
    }
}
