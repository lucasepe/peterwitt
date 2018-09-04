package io.github.lucasepe.core.eventbus;


import java.util.concurrent.*;

/**
 * Collection of {@link Executor}s to use in conjunction with {@link PeterWitt#post(Object, ExecutorService)}.
 */
/* package */ final class PeterwittExecutors {

    private static final PeterwittExecutors INSTANCE = new PeterwittExecutors();

    private final ExecutorService background;
    private final ScheduledExecutorService scheduled;
    private final Executor immediate;

    private PeterwittExecutors() {
        this.background = Executors.newCachedThreadPool();
        this.scheduled = Executors.newSingleThreadScheduledExecutor();
        this.immediate = new ImmediateExecutor();
    }

  /**
   * An {@link Executor} that executes tasks in parallel.
   */
  /* package */ static ExecutorService background() {
    return INSTANCE.background;
  }

  /* package */ static ScheduledExecutorService scheduled() {
    return INSTANCE.scheduled;
  }

  /**
   * An {@link Executor} that executes tasks in the current thread unless
   * the stack runs too deep, at which point it will delegate to {@link PeterwittExecutors#background}
   * in order to trim the stack.
   */
  /* package */ static Executor immediate() {
    return INSTANCE.immediate;
  }

  /**
   * An {@link Executor} that runs a runnable inline (rather than scheduling it
   * on a thread pool) as long as the recursion depth is less than MAX_DEPTH. If the executor has
   * recursed too deeply, it will instead delegate to the {@link PeterWitt#BACKGROUND_EXECUTOR} in order
   * to trim the stack.
   */
  private static class ImmediateExecutor implements Executor {
    private static final int MAX_DEPTH = 15;
    private ThreadLocal<Integer> executionDepth = new ThreadLocal<>();

    /**
     * Increments the depth.
     *
     * @return the new depth value.
     */
    private int incrementDepth() {
      Integer oldDepth = executionDepth.get();
      if (oldDepth == null) {
        oldDepth = 0;
      }
      int newDepth = oldDepth + 1;
      executionDepth.set(newDepth);
      return newDepth;
    }

    /**
     * Decrements the depth.
     *
     * @return the new depth value.
     */
    @SuppressWarnings("UnusedReturnValue")
    private int decrementDepth() {
      Integer oldDepth = executionDepth.get();
      if (oldDepth == null) {
        oldDepth = 0;
      }
      int newDepth = oldDepth - 1;
      if (newDepth == 0) {
        executionDepth.remove();
      } else {
        executionDepth.set(newDepth);
      }
      return newDepth;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void execute(final Runnable command) {
      int depth = incrementDepth();
      try {
        if (depth <= MAX_DEPTH) {
          command.run();
        } else {
          PeterwittExecutors.background().execute(command);
        }
      } finally {
        decrementDepth();
      }
    }
  }
}
