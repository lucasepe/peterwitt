package io.github.lucasepe.eventbus;

import io.github.lucasepe.core.eventbus.PeterWitt;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.LogManager;


public class PeterWittTestCase {

  private final PeterWitt eventBus = new PeterWitt();

  @Before
  public void setup() {
    try (InputStream stream = PeterWittTestCase.class.getClassLoader().getResourceAsStream("logging.properties")) {

      LogManager logManager = LogManager.getLogManager();
      logManager.readConfiguration(stream);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void immediatePost() {
    final Receiver receiver = new Receiver();

    this.eventBus.register(receiver, GreetingEvent.class);
    this.eventBus.post(new GreetingEvent("Luca"));
  }

  @Test
  public void delayedPost() {
    final Receiver receiver = new Receiver();

    this.eventBus.register(receiver, GreetingEvent.class);
    this.eventBus.postDelayed(new GreetingEvent("Luca"), 3000L);
  }


  static class GreetingEvent {
    final String name;

    GreetingEvent(final String name) {
      this.name = name;
    }
  }

  static class Receiver implements PeterWitt.EventReceiver {
    @Override
    public void onEventReceived(Object event) {
      if (event instanceof GreetingEvent) {
        GreetingEvent greetingEvent = (GreetingEvent)event;
        System.out.println(String.format("Hello %1$s!", greetingEvent.name));
      }
    }
  }
}
