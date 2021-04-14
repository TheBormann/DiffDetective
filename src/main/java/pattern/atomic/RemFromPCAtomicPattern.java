package pattern.atomic;

import analysis.data.PatternMatch;
import diff.data.DiffNode;
import evaluation.FeatureContext;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;

public class RemFromPCAtomicPattern extends AtomicPattern{
    public static final String PATTERN_NAME = "RemFromPC";

    public RemFromPCAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch> getMatches(DiffNode codeNode) {
        List<PatternMatch> patternMatches = new ArrayList<>();

        if(codeNode.isRem() && !codeNode.getBeforeParent().isRem()){
            Node fm = codeNode.getBeforeParent().getBeforeFeatureMapping();

            PatternMatch patternMatch = new PatternMatch(this,
                    codeNode.getFromLine(), codeNode.getToLine(), fm
            );
            patternMatches.add(patternMatch);
        }
        return patternMatches;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0], true)
        };
    }
}
