# PeterWitt

<!-- 
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lucasepe/peterwitt.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.lucasepe%22%20AND%20a:%22peterwitt%22) ![Jar Size](https://img.shields.io/badge/jar%20size-%E2%89%8512kB-1a1aff.svg) ![No External Dependencies](https://img.shields.io/badge/external%20dependencies-none-008000.svg) [![GitHub license](https://img.shields.io/github/license/lucasepe/breezy.svg)](https://github.com/lucasepe/breezy/blob/master/LICENSE) 

--> 

![PeterWitt](./peterwitt96.png)

> PeterWitt is a very simple Java EventBus.


## Usage

PeterWitt library is available from [Maven Central](https://search.maven.org/search?q=g:%22io.github.lucasepe%22%20AND%20a:%22peterwitt%22).

<!--
```xml
<dependency>
  <groupId>io.github.lucasepe</groupId>
  <artifactId>peterwitt</artifactId>
  <version>1.0.0</version>
</dependency>
```
-->
## Create an event

```java
class GreetingEvent {
  final String name;

  GreetingEvent(final String name) {
    this.name = name;
  }
}
```

## Create a receiver

```java
class Receiver implements PeterWitt.EventReceiver {
  @Override
  public void onEventReceived(Object event) {
    if (event instanceof GreetingEvent) {
      GreetingEvent greetingEvent = (GreetingEvent)event;
      System.out.println(String.format("Hello %1$s!", greetingEvent.name)); 
    } 
  }
}

Receiver receiver = new Receiver();
```

## Create a PeterWitt instance

```java
final PeterWitt peterwitt = new PeterWitt();
```

## Register the EventReceiver

In order to handle the specific event

```java
peterwitt.register(GreetingEvent.class, receiver);
```

## Post a new event

> in the current thread unless the stack runs too deep, at which point it will delegate to a background executor in order to trim the stack.

```java
peterwitt.post(new GreetingEvent("Luca"));
```

> in a background thread.

```java
peterwitt.postInBackground(new GreetingEvent("Luca"));
```

> with a specific delay in milliseconds.

```java
peterwitt.postDelayed(new GreetingEvent("Luca"), 3000L);
```

## Unregistering the EventReceiver

```java
peterwitt.unregister(receiver);
```

