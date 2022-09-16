package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.diffdetective.diff.difftree.serialize.LinegraphFormat;
import org.variantsync.functjonal.Pair;

/**
 * Reads and writes {@link DiffNode DiffNodes} from and to line graph.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public interface DiffNodeLabelFormat extends LinegraphFormat {
	/**
	 * Converts a label of line graph into a {@link DiffNode}.
	 * 
	 * @param lineGraphNodeLabel A string containing the label of the {@link DiffNode}
	 * @param nodeId The id of the {@link DiffNode}
	 * @return The corresponding {@link DiffNode}
	 */
	default DiffNode fromLabelAndId(final String lineGraphNodeLabel, final int nodeId) {
	    return DiffNode.fromID(nodeId, lineGraphNodeLabel);
	}
	
	/**
	 * Converts a {@link DiffNode} into a {@link DiffNode} label of line graph.
	 * 
	 * @param node The {@link DiffNode} to be converted
	 * @return The corresponding line graph line
	 */
	String toLabel(final DiffNode node);

    /**
     * Converts a line describing a graph (starting with "t # ") in line graph format into a {@link DiffTreeSource}.
     *
     * @param lineGraphLine A line from a line graph file starting with "t #"
     * @return A pair with the first element being the id of the node specified in the given lineGrapLine.
     *         The second entry is the parsed {@link DiffNode} described by the label of this line.
     */
    default Pair<Integer, DiffNode> fromLineGraphLine(final String lineGraphLine) {
        if (!lineGraphLine.startsWith(LineGraphConstants.LG_NODE)) throw new RuntimeException("Failed to parse DiffNode: Expected \"v ...\" but got \"" + lineGraphLine + "\"!"); // check if encoded DiffNode

        final int idBegin = LineGraphConstants.LG_NODE.length() + 1;
        final int idEnd = lineGraphLine.indexOf(' ', idBegin);
        final String nodeIdStr = lineGraphLine.substring(idBegin, idEnd); // extract the string between the overhead in front of the node id and the delimiter right after the node id
        final int nodeId;
        try {
            nodeId = Integer.parseInt(nodeIdStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Given node id is not an integer: ‘" + nodeIdStr + "’");
        }

        final String label = lineGraphLine.substring(idEnd + 1);
        return new Pair<>(nodeId, fromLabelAndId(label, nodeId));
    }

    /**
     * Serializes the given node to a line in linegraph format.
     *
     * @param node The {@link DiffNode} to be converted
     * @return The entire line graph line of a {@link DiffNode}.
     */
    default String toLineGraphLine(final DiffNode node) {
        return LineGraphConstants.LG_NODE + " " + node.getID() + " " + toLabel(node);
    }
}