# Rörledning

[![CodeFactor](https://www.codefactor.io/repository/github/sauilitired/rorledning/badge/master)](https://www.codefactor.io/repository/github/sauilitired/rorledning/overview/master)

This is a library that allows you to create services, that can have several different implementations.
A service in this case, is anything that takes in a context, and spits out some sort of result, achieving
some pre-determined task.

Examples of services would be generators and caches.

## Links

- Discord: https://discord.gg/KxkjDVg
- JavaDoc: https://plotsquared.com/docs/rörledning/

## Maven

Rörledning is available on Maven Central:

```xml
<dependency>
    <groupId>cloud.commandframework</groupId>
    <artifactId>cloud-services</artifactId>
    <version>1.4.0</version>
</dependency>
```

## Usage

### ServicePipeline

All requests start in the `ServicePipeline`. To get an instance of the `ServicePipeline`, simply use
the service pipeline builder.

**Example:**

```java
final ServicePipeline servicePipeline = ServicePipeline.builder().build();
```

### Service

To implement a service, simply create an interface that extends `Service<Context, Result>`.
The context is the type that gets pumped into the service (i.e, the value you provide), and the result
is the type that gets produced by the service.

The pipeline will attempt to generate a result from each service, until a service produces a non-null result.
Thus, if a service cannot (or shouldn't) produce a result for a given context, it can simply return null.

However, there's a catch to this. At least one service must always provide a result for every input.
To ensure that this is the case, a default implementation of the service must be registered together
with the service type. This implementation is not allowed to return null.

**Examples:**

Example Service:

```java
public interface MockService extends Service<MockService.MockContext, MockService.MockResult> {

    class MockContext {

        private final String string;

        public MockContext(@Nonnull final String string) {
            this.string = string;
        }

        @Nonnull public String getString() {
            return this.string;
        }

    }

    class MockResult {

        private final int integer;

        public MockResult(final int integer) {
            this.integer = integer;
        }

        public int getInteger() {
            return this.integer;
        }

    }

}
```

Example Implementation:

```java
public class DefaultMockService implements MockService {

    @Nullable @Override public MockResult handle(@Nonnull final MockContext mockContext) {
        return new MockResult(32);
    }

}
```

Example Registration:

```java
servicePipeline.registerServiceType(TypeToken.get(MockService.class), new DefaultMockService());
```

Example Usage:

```java
final int result = servicePipeline.pump(new MockService.MockContext("Hello"))
                                  .through(MockService.class)
                                  .getResult()
                                  .getInteger();
```

### SideEffectService

Some services may just alter the state of the incoming context, without generating any (useful) result.
These services should extend `SideEffectService`.

SideEffectService returns a State instead of a result. The service may either accept a context, in
which case the execution chain is interrupted. It can also reject the context, in which case the
other services in the execution chain will get a chance to consume it.

**Example:**

```java
public interface MockSideEffectService extends SideEffectService<MockSideEffectService.MockPlayer> {

    class MockPlayer {

        private int health;

        public MockPlayer(final int health) {
            this.health = health;
        }

        public int getHealth() {
            return this.health;
        }

        public void setHealth(final int health) {
            this.health = health;
        }

    }

}

public class DefaultSideEffectService implements MockSideEffectService {

    @Nonnull @Override public State handle(@Nonnull final MockPlayer mockPlayer) {
        mockPlayer.setHealth(0);
        return State.ACCEPTED;
    }

}
```

### Asynchronous Execution

The pipeline results can be evaluated asynchronously. Simple use `getResultAsynchronously()`
instead of `getResult()`. By default, a single threaded executor is used. A different executor
can be supplied to the pipeline builder.

### Filters

Sometimes you may not want your service to respond to certain contexts. Instead of always
returning null in those cases, filters can be used. These are simply predicates that take in your
context type, and should be registered together with your implementation.

**Example:**

Example Filter:
```java
public class FilteredMockService implements MockService, Predicate<MockService.MockContext> {

    @Nullable @Override public MockResult handle(@Nonnull final MockContext mockContext) {
        return new MockResult(999);
    }

    @Override public boolean test(final MockContext mockContext) {
        return mockContext.getString().equalsIgnoreCase("potato");
    }

}
```

Example Registration:

```java
final FilteredMockService service = new FilteredMockService();
final List<Predicate<MockService.MockContext>> predicates = Collections.singletonList(service);
servicePipeline.registerServiceImplementation(MockService.class, service, predicates);
```

### Forwarding

Sometimes it may be useful to use the result produced by a service as the context for another service.
To make this easier, the concept of forwarding was introduced. When using `getResult()`, one can instead
use `forward()`, to pump the result back into the pipeline.

**Examples:**

```java
servicePipeline.pump(new MockService.MockContext("huh"))
               .through(MockService.class)
               .forward()
               .through(MockResultConsumer.class)
               .getResult();
```

This can also be done asynchronously:

```java
servicePipeline.pump(new MockService.MockContext("Something"))
               .through(MockService.class)
               .forwardAsynchronously()
               .thenApply(pump -> pump.through(MockResultConsumer.class))
               .thenApply(ServiceSpigot::getResult)
               .get();
```

### Priority/Ordering

By default, all service implementations will be executed in first-in-last-out order. That is,
the earlier the implementation was registered, the lower the priority it gets in the execution chain.

This may not always be ideal, and it is therefore possibly to override the natural ordering
of the implementations by using the &#64;Optional annotation.

**Example:**

```java
@Order(ExecutionOrder.FIRST)
public class MockOrderedFirst implements MockService {

    @Nullable @Override public MockResult handle(@Nonnull final MockContext mockContext) {
        return new MockResult(1);
    }

}

@Order(ExecutionOrder.LAST)
public class MockOrderedLast implements MockService {

    @Nullable @Override public MockResult handle(@Nonnull final MockContext mockContext) {
        return new MockResult(2);
    }

}
```

No matter in which order MockOrderedFirst and MockOrderedLast are added, MockOrderedFirst will be
handled before MockOrderedLast.

The default order for all services is `SOON`.

### Annotated Methods

You can also implement services by using instance methods, like such:

```java
@ServiceImplementation(MockService.class)
public MockService.MockResult handle(@Nonnull final MockService.MockContext context) {
    return new MockService.MockResult(context.getString().length());
}
```

The methods can also be annotated with the order annotation. Is is very important
that the method return type and parameter type match up wit the service context and
result types, or you will get runtime exceptions when using the pipeline. 

These methods are registered in ServicePipeline, using `registerMethods(yourClassInstance);`

### ConsumerService

Consumer services effectively turns the service pipeline into an event bus. Each implementation
will get a chance to consume the incoming context, unless an implementation forcefully interrupts
the execution, by calling `ConsumerService.interrupt()`

**Examples:**

```java
public interface MockConsumerService extends ConsumerService<MockService.MockContext> {
}

public class InterruptingMockConsumer implements MockConsumerService {

    @Override public void accept(@Nonnull final MockService.MockContext mockContext) {
        ConsumerService.interrupt();
    }

}

public class StateSettingConsumerService implements MockConsumerService {

    @Override public void accept(@Nonnull final MockService.MockContext mockContext) {
        mockContext.setState("");
    }

}
```

### Partial Result Services

Sometimes you may need to get results for multiple contexts, but there is no guarantee
that a single service will be able to generate all the needed results. It is then possible
to make use of `PartialResultService`.

The partial result service interface uses the `ChunkedRequestContext` class as the input, and
outputs a map of request-response pairs.

**Example:**

Example Request Type:

```java
public class MockChunkedRequest extends ChunkedRequestContext<MockChunkedRequest.Animal, MockChunkedRequest.Sound> {

    public MockChunkedRequest(@Nonnull final Collection<Animal> requests) {
        super(requests);
    }


    public static class Animal {

        private final String name;

        public Animal(@Nonnull final String name) {
            this.name = name;
        }

        @Nonnull public String getName() {
            return this.name;
        }
    }

    public static class Sound {

        private final String sound;

        public Sound(@Nonnull final String sound) {
            this.sound = sound;
        }

        @Nonnull public String getSound() {
            return this.sound;
        }
    }
}
```

Example Service:
```java
public interface MockPartialResultService extends
    PartialResultService<MockChunkedRequest.Animal, MockChunkedRequest.Sound, MockChunkedRequest> {
}
```

Example Implementations:
```java
public class DefaultPartialRequestService implements MockPartialResultService {

    @Nonnull @Override
    public Map<MockChunkedRequest.Animal, MockChunkedRequest.Sound> handleRequests(
        @Nonnull final List<MockChunkedRequest.Animal> requests) {
        final Map<MockChunkedRequest.Animal, MockChunkedRequest.Sound> map = new HashMap<>(requests.size());
        for (final MockChunkedRequest.Animal animal : requests) {
            map.put(animal, new MockChunkedRequest.Sound("unknown"));
        }
        return map;
    }

}

public class CompletingPartialResultService implements MockPartialResultService {

    @Nonnull @Override public Map<MockChunkedRequest.Animal, MockChunkedRequest.Sound> handleRequests(
        @Nonnull List<MockChunkedRequest.Animal> requests) {
        final Map<MockChunkedRequest.Animal, MockChunkedRequest.Sound> map = new HashMap<>();
        for (final MockChunkedRequest.Animal animal : requests) {
            if (animal.getName().equals("cow")) {
                map.put(animal, new MockChunkedRequest.Sound("moo"));
            } else if (animal.getName().equals("dog")) {
                map.put(animal, new MockChunkedRequest.Sound("woof"));
            }
        }
        return map;
    }

}
```

### Exception Handling

Exceptions thrown during result retrieval and implementation filtering will be wrapped by
`PipelineException`. You can use `PipelineException#getCause` to get the exception that was wrapped.

**Example:**

```java
try {
    final Result result = pipeline.pump(yourContext).through(YourService.class).getResult();
} catch (final PipelineException exception) {
    final Exception cause = exception.getCause();
}
```

You may also make use of `ServicePipeline#getException(BiConsumer<Result, Throwable>)`. This method
will unwrap any pipeline exceptions before passing them to the consumer.

**Example:**

```java
pipeline.getResult((result, exception) -> {
    if (exception != null) {
        exception.printStackTrace();
    } else {
        // consume result
    }
});
```
