package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.monitoring.TaskCompletionMonitor;
import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.mining.MiningTask;
import org.variantsync.diffdetective.parallel.ScheduledTasksIterator;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.Diagnostics;
import org.variantsync.diffdetective.util.InvocationCounter;
import org.variantsync.functjonal.iteration.ClusteredIterator;
import org.variantsync.functjonal.iteration.MappedIterator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public record HistoryAnalysis(
        List<Repository> repositoriesToAnalyze,
        Path outputDir,
        int commitsToProcessPerThread,
        CommitHistoryAnalysisTaskFactory whatToDo,
        Consumer<Path> postProcessingOnRepositoryOutputDir
) {
    public static final String TOTAL_RESULTS_FILE_NAME = "totalresult" + AnalysisResult.EXTENSION;
    public static final int COMMITS_TO_PROCESS_PER_THREAD_DEFAULT = 1000;

    @Deprecated
    public static void mine(
            final Repository repo,
            final Path outputDir,
            final DiffTreeLineGraphExportOptions exportOptions,
            final AnalysisStrategy strategy)
    {
        AnalysisResult totalResult;
        final GitDiffer differ = new GitDiffer(repo);
        final Clock clock = new Clock();

        // prepare tasks
        Logger.info(">>> Scheduling synchronous mining");
        clock.start();
        List<RevCommit> commitsToProcess = differ.yieldRevCommits().toList();
        final CommitHistoryAnalysisTask task = new MiningTask(new CommitHistoryAnalysisTask.Options(
                repo,
                differ,
                outputDir.resolve(repo.getRepositoryName() + ".lg"),
                exportOptions,
                strategy,
                commitsToProcess
        ));
        Logger.info("Scheduled " + commitsToProcess.size() + " commits.");
        commitsToProcess = null; // free reference to enable garbage collection
        Logger.info("<<< done after " + clock.printPassedSeconds());

        Logger.info(">>> Run mining");
        clock.start();
        try {
            totalResult = task.call();
        } catch (Exception e) {
            Logger.error(e);
            Logger.info("<<< aborted after " + clock.printPassedSeconds());
            return;
        }
        Logger.info("<<< done after " + clock.printPassedSeconds());

        exportMetadata(outputDir, totalResult);
    }

    public static void mineAsync(
            final Repository repo,
            final Path outputDir,
            final CommitHistoryAnalysisTaskFactory taskFactory,
            int commitsToProcessPerThread)
    {
        final AnalysisResult totalResult = new AnalysisResult(repo.getRepositoryName());
        final GitDiffer differ = new GitDiffer(repo);
        final Clock clock = new Clock();

        // prepare tasks
        final int nThreads = Diagnostics.INSTANCE.run().getNumberOfAvailableProcessors();
        Logger.info(">>> Scheduling asynchronous mining on " + nThreads + " threads.");
        clock.start();
        final InvocationCounter<RevCommit, RevCommit> numberOfTotalCommits = InvocationCounter.justCount();
        final Iterator<CommitHistoryAnalysisTask> tasks = new MappedIterator<>(
                /// 1.) Retrieve COMMITS_TO_PROCESS_PER_THREAD commits from the differ and cluster them into one list.
                new ClusteredIterator<>(differ.yieldRevCommitsAfter(numberOfTotalCommits), commitsToProcessPerThread),
                /// 2.) Create a MiningTask for the list of commits. This task will then be processed by one
                ///     particular thread.
                commitList -> taskFactory.create(
                        repo,
                        differ,
                        outputDir.resolve(commitList.get(0).getId().getName() + ".lg"),
                        commitList)
        );
        Logger.info("<<< done in " + clock.printPassedSeconds());

        final TaskCompletionMonitor commitSpeedMonitor = new TaskCompletionMonitor(0, TaskCompletionMonitor.LogProgress("commits"));
        Logger.info(">>> Run mining");
        clock.start();
        commitSpeedMonitor.start();
        try (final ScheduledTasksIterator<AnalysisResult> threads = new ScheduledTasksIterator<>(tasks, nThreads)) {
            while (threads.hasNext()) {
                final AnalysisResult threadsResult = threads.next();
                totalResult.append(threadsResult);
                commitSpeedMonitor.addFinishedTasks(threadsResult.exportedCommits);
            }
        } catch (Exception e) {
            Logger.error("Failed to run all mining task!");
            Logger.error(e);
            System.exit(0);
        }

        final double runtime = clock.getPassedSeconds();
        Logger.info("<<< done in " + Clock.printPassedSeconds(runtime));

        totalResult.runtimeWithMultithreadingInSeconds = runtime;
        totalResult.totalCommits = numberOfTotalCommits.invocationCount().get();

        exportMetadata(outputDir, totalResult);
    }

    public static <T> void exportMetadata(final Path outputDir, final Metadata<T> totalResult) {
        exportMetadataToFile(outputDir.resolve(TOTAL_RESULTS_FILE_NAME), totalResult);
    }

    public static <T> void exportMetadataToFile(final Path outputFile, final Metadata<T> totalResult) {
        final String prettyMetadata = totalResult.exportTo(outputFile);
        Logger.info("Metadata:\n" + prettyMetadata);
    }

    public void runAsync() {
        for (final Repository repo : repositoriesToAnalyze) {
            Logger.info(" === Begin Processing " + repo.getRepositoryName() + " ===");
            final Clock clock = new Clock();
            clock.start();

            final Path repoOutputDir = outputDir.resolve(repo.getRepositoryName());
            /// Don't repeat work we already did:
            if (!Files.exists(repoOutputDir.resolve(TOTAL_RESULTS_FILE_NAME))) {
                mineAsync(repo, repoOutputDir, whatToDo, commitsToProcessPerThread);
//                mine(repo, repoOutputDir, ExportOptions(repo), MiningStrategy());

                postProcessingOnRepositoryOutputDir.accept(repoOutputDir);
            } else {
                Logger.info("  Skipping repository " + repo.getRepositoryName() + " because it has already been processed.");
            }

            Logger.info(" === End Processing " + repo.getRepositoryName() + " after " + clock.printPassedSeconds() + " ===");
        }
    }
}