package com.globant.automation.trainings.tests.concurrency.threadSafety;

import com.globant.automation.trainings.tests.concurrency.TimedIterator;
import com.globant.automation.trainings.tests.concurrency.threadSafety.interfaces.IIntGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Java concurrency - Example 2
 * <p>
 * The intention is to show how concurrency issues happen when not taking into account
 * Thread Safety in our code, despite using Thread Safe collection types for storage.
 * <p>
 * Notice how every test involving {@link UnsafeIntGenerator} fails, no matter the Thread Safe collection used.
 *
 * @author Juan Krzemien
 */
@RunWith(Parameterized.class)
public class ThreadSafetyIssues extends HideNonRelatedStuff {
    private final Runnable taskRetrieveUniqueNumbers;

    public ThreadSafetyIssues(IIntGenerator generator, Collection<Integer> forMain, Collection<Integer> forThreads) {
        super(generator, forMain, forThreads);
        this.taskRetrieveUniqueNumbers = () -> range(0, MAX_NUMBERS_TO_RETRIEVE).forEach(i -> storageForThreads.add(generator.getNextInt()));
    }

    /**
     * Loop for MAX_RUN_TIME_SECONDS seconds doing:
     * - Clear structures
     * - Spawn MAX_THREADS threads -> Each thread retrieves MAX_THREADS numbers from numberGenerator
     * - Main thread retrieves MAX_THREADS numbers from numberGenerator too
     * - Asserts retrieved numbers: no duplicated/missing
     */
    @Test
    public void threadSafetyIssues() {
        new TimedIterator(MAX_RUN_TIME_SECONDS).forEachRemaining(
                l -> {
                    storageForMainThread.clear();
                    storageForThreads.clear();
                    range(0, MAX_THREADS).parallel().forEach(i -> taskRetrieveUniqueNumbers.run());
                    // No parallel() here...
                    range(0, MAX_NUMBERS_TO_RETRIEVE).forEach(i -> storageForMainThread.add(numberGenerator.getNextInt()));
                    printStats(l);
                }
        );
    }

    private void printStats(Long loopNumber) {
        out.println(format("%s/%s - Attempt #%s", generatorName, collectionName, loopNumber));
        out.println(format("Integers retrieved by Main: %s", storageForMainThread));
        out.println(format("Integers retrieved by Threads: %s", storageForThreads));
        try {
            assertEquals(format("[%s Thread Safety issue] There should be no elements repeated", generatorName), storageForThreads.size(), storageForThreads.parallelStream().distinct().count());
            assertTrue(format("[%s Thread Safety issue] Storages should not share numbers", generatorName), storageForMainThread.parallelStream().allMatch(i -> !storageForThreads.contains(i)));
        } catch (AssertionError ae) {
            out.println(format("Awww snap! I started to behave erratically on attempt #%s...Good times I'm not in a production environment!", loopNumber));
            throw ae;
        }
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // Safe collections
                {new UnsafeIntGenerator(), new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>()}, // Unsafe generator
                {new SafeIntGenerator(), new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>()},
                {new AnotherSafeIntGenerator(), new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>()},
                {new UnsafeIntGenerator(), new ConcurrentSkipListSet<>(), new ConcurrentSkipListSet<>()}, // Unsafe generator
                {new SafeIntGenerator(), new ConcurrentSkipListSet<>(), new ConcurrentSkipListSet<>()},
                {new AnotherSafeIntGenerator(), new ConcurrentSkipListSet<>(), new ConcurrentSkipListSet<>()},
                {new UnsafeIntGenerator(), new Vector<>(), new Vector<>()}, // Unsafe generator
                {new SafeIntGenerator(), new Vector<>(), new Vector<>()},
                {new AnotherSafeIntGenerator(), new Vector<>(), new Vector<>()}
        });
    }
}
