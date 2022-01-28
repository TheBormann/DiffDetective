package datasets.predefined;

import datasets.ParseOptions;
import datasets.Repository;
import diff.DiffFilter;
import org.eclipse.jgit.diff.DiffEntry;

import java.nio.file.Path;

/**
 * Default repository for the snippet from the Marlin history used in the paper
 * "Concepts, Operations, and Feasibility of a Projection-Based Variation Control System",
 * Stefan Stanciulescu, Thorsten Berger, Eric Walkingshaw, Andrzej Wasowski
 * at ICSME 2016.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public class StanciulescuMarlin {
    public static final DiffFilter DIFF_FILTER = new DiffFilter.Builder()
            .allowMerge(false)
            .allowedPaths("Marlin.*")
            .blockedPaths(".*arduino.*")
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("c", "cpp", "h", "pde")
            .build();

    /**
     * Instance for the default predefined Marlin repository.
     * @return Marlin repository
     */
    public static Repository fromZipInDiffDetectiveAt(Path pathToDiffDetective) {
        final Path marlinPath = pathToDiffDetective
                .resolve(Repository.DIFFDETECTIVE_DEFAULT_REPOSITORIES_DIRECTORY)
                .resolve("Marlin_old.zip");
        return Repository
                .fromZip(marlinPath, "Marlin_old")
                .setDiffFilter(DIFF_FILTER)
                .setParseOptions(new ParseOptions(Marlin.ANNOTATION_PARSER));
    }
}
