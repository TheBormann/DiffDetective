package diff.difftree.serialize;

import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.difftree.*;
import util.Assert;
import util.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Import patches from line graphs.
 */
public class LineGraphImport {
    public static List<DiffTree> fromFile(final Path path, final DiffTreeLineGraphImportOptions options) {
        Assert.assertTrue(Files.isRegularFile(path));
        Assert.assertTrue(FileUtils.isLineGraph(path));
        return fromLineGraph(FileUtils.readUTF8(path), options);
    }
	
	/**
	 * Transforms a line graph into a list of {@link DiffTree DiffTrees}.
	 * 
	 * @return All {@link DiffTree DiffTrees} contained in the line graph
	 */
	public static List<DiffTree> fromLineGraph(final String lineGraph, final DiffTreeLineGraphImportOptions options) {
		java.util.Scanner input = new java.util.Scanner(lineGraph);
		
		// All DiffTrees read from the line graph
		List<DiffTree> diffTreeList = new ArrayList<>();
		
		// All DiffNodes of one DiffTree for determining the root node
		List<DiffNode> diffNodeList = new ArrayList<>();
		
		// A hash map of DiffNodes
		// <id of DiffNode, DiffNode>
		HashMap<Integer, DiffNode> diffNodes = new HashMap<>();

		// The currently read DiffTree with all its DiffNodes and edges
		DiffTree curDiffTree = null;
		
		// The previously read DiffTree
		String previousDiffTreeLine = "";
		
		// Read the entire line graph 
		while (input.hasNext()) {
			String ln = input.nextLine();
			if (ln.startsWith(LineGraphConstants.LG_TREE_HEADER)) {
				// the line represents a DiffTree
				
				if (!diffNodeList.isEmpty()) {
					curDiffTree = parseDiffTree(previousDiffTreeLine, diffNodeList, options); // parse to DiffTree
					diffTreeList.add(curDiffTree); // add newly computed DiffTree to the list of all DiffTrees
					
					// Remove all DiffNodes from list
					diffNodeList.clear();
					diffNodes.clear();	
				} 
				previousDiffTreeLine = ln;
			} else if (ln.startsWith(LineGraphConstants.LG_NODE)) {
				// the line represents a DiffNode
				
				// parse node from input line
				final Pair<Integer, DiffNode> idAndNode = options.nodeFormat().fromLineGraphLine(ln);
			
				// add DiffNode to lists of current DiffTree
				diffNodeList.add(idAndNode.getValue());
				diffNodes.put(idAndNode.getKey(), idAndNode.getValue());
				
			} else if (ln.startsWith(LineGraphConstants.LG_EDGE)) {
				// the line represent a connection with two DiffNodes
				
				String[] edge = ln.split(" ");
				String fromNodeId = edge[1]; // the id of the child DiffNode
				String toNodeId = edge[2]; // the id of the parent DiffNode
				String name = edge[3];
				
				// Both child and parent DiffNode should exist since all DiffNodes have been read in before. Otherwise, the line graph input is faulty
				DiffNode childNode = diffNodes.get(Integer.parseInt(fromNodeId));
				DiffNode parentNode = diffNodes.get(Integer.parseInt(toNodeId));

				if (childNode == null) {
					input.close();
					throw new IllegalArgumentException(fromNodeId + " does not exits. Faulty line graph.");
				}
				if (parentNode == null) {
					input.close();
					throw new IllegalArgumentException(toNodeId + " does not exits. Faulty line graph.");
				}

                switch (name) {
                    // Nothing has been changed. The child-parent relationship remains the same
                    case LineGraphConstants.BEFORE_AND_AFTER_PARENT -> {
                        parentNode.addAfterChild(childNode);
                        parentNode.addBeforeChild(childNode);
                    }
                    // The child DiffNode lost its parent DiffNode (an orphan DiffNode)
                    case LineGraphConstants.BEFORE_PARENT -> parentNode.addBeforeChild(childNode);

                    // The parent DiffNode has a new child DiffNode
                    case LineGraphConstants.AFTER_PARENT -> parentNode.addAfterChild(childNode);

                    // A syntax error has occurred.
                    default -> throw new RuntimeException("Syntax error. Invalid name in edge: " + ln);
                }
			} else {
				// ignore blank spaces
				if (!ln.trim().equals("")) {
					input.close();
					String errorMessage = String.format(
							"Line graph syntax error. Expects: \"%s\" (DiffTree), \"%s\" (DiffNode), \"%s\" (edge) or a blank space (delimiter). Faulty input: \"%s\".", 
							LineGraphConstants.LG_TREE_HEADER, 
							LineGraphConstants.LG_NODE, 
							LineGraphConstants.LG_EDGE, 
							ln);
					throw new IllegalArgumentException(errorMessage);
				}
			}
		}
		input.close();

		if (!diffNodeList.isEmpty()) {
			curDiffTree = parseDiffTree(previousDiffTreeLine, diffNodeList, options); // parse to DiffTree
			diffTreeList.add(curDiffTree); // add newly computed DiffTree to the list of all DiffTrees
		}
		
		// return all computed DiffTrees.
		return diffTreeList;
	}
	
	/**
	 * Generates a {@link DiffTree} from given parameters.
	 * 
	 * @param lineGraph The line graph line to be parsed
	 * @param diffNodeList The list of {@link DiffNode DiffNodes}
	 * @param options {@link DiffTreeLineGraphImportOptions}
	 * @return {@link DiffTree}
	 */
	private static DiffTree parseDiffTree(final String lineGraph, final List<DiffNode> diffNodeList, final DiffTreeLineGraphImportOptions options) {
		final DiffTreeSource diffTreeSource = options.treeFormat().fromLineGraphLine(lineGraph);
		// Handle trees and graphs differently
		if (options.graphFormat() == GraphFormat.DIFFGRAPH) {
			// If you should interpret the input data as DiffTrees, always expect a root to be present. Parse all nodes (v) to a list of nodes. Search for the root. Assert that there is exactly one root.
			Assert.assertTrue(diffNodeList.stream().noneMatch(DiffNode::isRoot)); // test if it’s not a tree
			return DiffGraph.fromNodes(diffNodeList, diffTreeSource);
		} else if (options.graphFormat() == GraphFormat.DIFFTREE) {
			// If you should interpret the input data as DiffTrees, always expect a root to be present. Parse all nodes (v) to a list of nodes. Search for the root. Assert that there is exactly one root.
			int rootCount = 0;
			DiffNode root = null;
			for (DiffNode v : diffNodeList) { 
				if (v.isRoot()) {
					rootCount++; 
					root = v;
				}
			}
			Assert.assertTrue(rootCount == 1);// test if it’s a tree
			return new DiffTree(root, diffTreeSource);
		} else {
			throw new RuntimeException("Unsupported GraphFormat");
		}
	}
}