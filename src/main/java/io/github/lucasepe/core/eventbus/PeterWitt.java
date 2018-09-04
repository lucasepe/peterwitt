package io.github.lucasepe.core.eventbus;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeterWitt {
  private static final Logger LOG = Logger.getLogger(PeterWitt.class.getName());

  public interface EventReceiver {
    void onEventReceived(Object event);
  }

  /**
   * An {@link Executor} that post events in parallel.
   */
  private static final ExecutorService BACKGROUND_EXECUTOR =
      PeterwittExecutors.background();

  /**
   * An {@link Executor} that post events in the current thread unless
   * the stack runs too deep, at which point it will delegate to {@link #BACKGROUND_EXECUTOR} in
   * order to trim the stack.
   */
  private static final Executor IMMEDIATE_EXECUTOR =
      PeterwittExecutors.immediate();

  /**
   * An {@link Executor} that post events with specific delay in milliseconds.
   */
  private static final ScheduledExecutorService SCHEDULED_EXECUTOR =
      PeterwittExecutors.scheduled();


  private final Map<Class<?>, List<EventReceiver>> receivers;

  public PeterWitt() {
    super();
    this.receivers = Collections.synchronizedMap(new LinkedHashMap<>());
  }

  public void register(PeterWitt.EventReceiver receiver, Class<?> cls) {
    List<PeterWitt.EventReceiver> list = this.receivers.computeIfAbsent(cls, k -> new ArrayList<>());
    if (list.add(receiver)) {
      LOG.log(Level.FINE, String.format("registered listener %1$s for event %2$s",
          receiver.getClass().getSimpleName(), cls.getSimpleName()));
    }
  }

  public void unregister(PeterWitt.EventReceiver receiver, Class<?> cls) {
    List<PeterWitt.EventReceiver> list = this.receivers.get(cls);
    if (list != null) {
      if (list.remove(receiver)) {
        LOG.log(Level.FINE, String.format("unregistered listener %1$s for event %2$s",
            receiver.getClass().getSimpleName(), cls.getSimpleName()));
      }
    }
  }

  public void post(Object event) {
    List<PeterWitt.EventReceiver> list = this.receivers.get(event.getClass());
    if (list != null) {
      for (PeterWitt.EventReceiver receiver : Collections.unmodifiableList(list)) {
        LOG.log(Level.FINE, String.format("posting event %1$s to receiver %2$s",
            event, receiver.getClass().getSimpleName()));
        IMMEDIATE_EXECUTOR.execute(() -> receiver.onEventReceived(event));
      }
    }
  }

  public void postInBackround(Object event) {
    List<PeterWitt.EventReceiver> list = this.receivers.get(event.getClass());
    if (list != null) {
      for (PeterWitt.EventReceiver receiver : Collections.unmodifiableList(list)) {
        LOG.log(Level.FINE, String.format("posting background event %1$s to receiver %2$s",
            event, receiver.getClass().getSimpleName()));
        BACKGROUND_EXECUTOR.submit(() -> receiver.onEventReceived(event));
      }
    }
  }

  public void postDelayed(Object event, final long delayMillis) {
    List<PeterWitt.EventReceiver> list = this.receivers.get(event.getClass());
    if (list != null) {
      for (PeterWitt.EventReceiver receiver : Collections.unmodifiableList(list)) {
        LOG.log(Level.FINE, String.format("posting event %1$s (delay: %2$d ms) to receiver %3$s",
            event, delayMillis, receiver.getClass().getSimpleName()));
        SCHEDULED_EXECUTOR.schedule(
            () -> receiver.onEventReceived(event), delayMillis, TimeUnit.MILLISECONDS);
      }
    }
  }

  public void shutdown() {
    LOG.log(Level.FINE, String.format("%1$s shutting down", getClass().getSimpleName()));
    this.receivers.clear();
    shutdown(SCHEDULED_EXECUTOR);
    shutdown(BACKGROUND_EXECUTOR);
  }

  private void shutdown(final ExecutorService executor) {
    if (executor != null) {
      try {
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
      } catch (InterruptedException ignore) {
        //LOG.warn("tasks interrupted", e);
      } finally {
        executor.shutdownNow();
      }
    }
  }
}
