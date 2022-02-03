package diff.difftree.serialize;

import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.filter.ExplainedFilter;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.render.PatchDiffRenderer;
import diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import diff.difftree.serialize.treeformat.DiffTreeLabelFormat;
import diff.difftree.transform.DiffTreeTransformer;
import org.tinylog.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Options necessary for exporting a line graph.
 * This records contains information for exporting a {@link DiffTree} into a line graph, such as the graph format and tree and node layouts.
 */
public record DiffTreeLineGraphExportOptions(
        GraphFormat graphFormat,
		DiffTreeLabelFormat treeFormat,
		DiffNodeLabelFormat nodeFormat,
        EdgeLabelFormat edgeFormat,
		ExplainedFilter<DiffTree> treeFilter,
        List<DiffTreeTransformer> treePreProcessing,
        BiConsumer<PatchDiff, Exception> onError) {
	
    public DiffTreeLineGraphExportOptions(GraphFormat graphFormat, DiffTreeLabelFormat treeFormat, DiffNodeLabelFormat nodeFormat, EdgeLabelFormat edgeFormat) {
        this(graphFormat, treeFormat, nodeFormat, edgeFormat, ExplainedFilter.Any(), new ArrayList<>(), LogError());
    }

    public DiffTreeLineGraphExportOptions(final DiffTreeLineGraphImportOptions importOptions) {
        this(
                importOptions.graphFormat(),
                importOptions.treeFormat(),
                importOptions.nodeFormat(),
                importOptions.edgeFormat()
        );
    }

    public static BiConsumer<PatchDiff, Exception> LogError() {
        return (p, e) -> Logger.error(e);
    }

    public static BiConsumer<PatchDiff, Exception> RenderError() {
        final PatchDiffRenderer errorRenderer = PatchDiffRenderer.ErrorRendering(DiffTreeRenderer.WithinDiffDetective());
        return (p, e) -> {
            Logger.error(e);
            Logger.error("Rendering patch");
            errorRenderer.render(p, Path.of("error"));
        };
    }

    public static BiConsumer<PatchDiff, Exception> SysExitOnError() {
        return (p, e) -> System.exit(0);
    }
    
}