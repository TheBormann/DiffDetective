package pattern.semantic;

import analysis.data.PatternMatch;
import diff.data.DiffNode;
import evaluation.FeatureContext;

import java.util.ArrayList;
import java.util.List;

public class MoveElseSemanticPattern extends SemanticPattern{

    public static final String PATTERN_NAME = "MoveElseSEM";

    public MoveElseSemanticPattern() {
        this.name = PATTERN_NAME;
    }

    /*
    DETECTION:
        added else node
        the parent of this node has a removed else child
        the parent has a child that is either also a child of the added or the removed else node
     */
    @Override
    public List<PatternMatch> getMatches(DiffNode annotationNode) {
        List<PatternMatch> patternMatches = new ArrayList<>();

        if(annotationNode.isAdd() && annotationNode.isElse()){

            DiffNode removedElse = null;
            for(DiffNode parentsChild : annotationNode.getAfterParent().getChildren()){
                if(parentsChild.isElse() && parentsChild.isRem()){
                    removedElse = parentsChild;
                    break;
                }
            }

            if(removedElse == null){
                return patternMatches;
            }

            List<DiffNode> commonAddElse = annotationNode.getChildren();
            commonAddElse.retainAll(annotationNode.getAfterParent().getChildren());

            List<DiffNode> commonRemElse = removedElse.getChildren();
            commonRemElse.retainAll(annotationNode.getAfterParent().getChildren());

            if(commonAddElse.isEmpty() && commonRemElse.isEmpty()){
                return patternMatches;
            }

            PatternMatch patternMatch = new PatternMatch(this,
                    annotationNode.getFromLine(), removedElse.getToLine()
            );
            patternMatches.add(patternMatch);
        }
        return patternMatches;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
