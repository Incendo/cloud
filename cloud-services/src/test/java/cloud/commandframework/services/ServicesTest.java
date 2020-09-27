//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.services;

import com.google.common.reflect.TypeToken;
import cloud.commandframework.services.mock.*;
import cloud.commandframework.services.types.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class ServicesTest {

    @Test
    public void testPipeline() throws Exception {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build();
        Assertions.assertNotNull(servicePipeline);
        servicePipeline
                .registerServiceType(TypeToken.of(MockService.class), new DefaultMockService());
        Assertions.assertThrows(IllegalArgumentException.class, () -> servicePipeline
                .registerServiceType(TypeToken.of(MockService.class), new DefaultMockService()));
        final SecondaryMockService secondaryMockService = new SecondaryMockService();
        servicePipeline
                .registerServiceImplementation(TypeToken.of(MockService.class), secondaryMockService,
                        Collections.singleton(secondaryMockService));
        servicePipeline.registerServiceImplementation(MockService.class,
                mockContext -> new MockService.MockResult(-91),
                Collections.singleton(mockContext -> mockContext.getString().startsWith("-91")));
        Assertions.assertEquals(32,
                servicePipeline.pump(new MockService.MockContext("Hello")).through(MockService.class)
                        .getResult().getInteger());
        servicePipeline.pump(new MockService.MockContext("Hello")).through(MockService.class)
                .getResult(
                        (mockResult, throwable) -> Assertions.assertEquals(32, mockResult.getInteger()));
        Assertions.assertEquals(999,
                servicePipeline.pump(new MockService.MockContext("potato")).through(MockService.class)
                        .getResult().getInteger());
        Assertions.assertEquals(-91,
                servicePipeline.pump(new MockService.MockContext("-91")).through(MockService.class)
                        .getResult().getInteger());
        Assertions.assertNotNull(
                servicePipeline.pump(new MockService.MockContext("oi")).through(MockService.class)
                        .getResultAsynchronously().get());

    }

    @Test
    public void testSideEffectServices() {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build();
        servicePipeline.registerServiceType(TypeToken.of(MockSideEffectService.class),
                new DefaultSideEffectService());
        final MockSideEffectService.MockPlayer mockPlayer =
                new MockSideEffectService.MockPlayer(20);
        Assertions.assertEquals(20, mockPlayer.getHealth());
        Assertions.assertEquals(State.ACCEPTED,
                servicePipeline.pump(mockPlayer).through(MockSideEffectService.class).getResult());
        Assertions.assertEquals(0, mockPlayer.getHealth());
        mockPlayer.setHealth(20);
        servicePipeline.registerServiceImplementation(MockSideEffectService.class,
                new SecondaryMockSideEffectService(), Collections.emptyList());
        Assertions.assertThrows(IllegalStateException.class,
                () -> servicePipeline.pump(mockPlayer).through(MockSideEffectService.class)
                        .getResult());
    }

    @Test
    public void testForwarding() throws Exception {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build()
                .registerServiceType(TypeToken.of(MockService.class), new DefaultMockService())
                .registerServiceType(TypeToken.of(MockResultConsumer.class), new MockResultConsumer());
        Assertions.assertEquals(State.ACCEPTED,
                servicePipeline.pump(new MockService.MockContext("huh")).through(MockService.class)
                        .forward().through(MockResultConsumer.class).getResult());
        Assertions.assertEquals(State.ACCEPTED,
                servicePipeline.pump(new MockService.MockContext("Something"))
                        .through(MockService.class).forwardAsynchronously()
                        .thenApply(pump -> pump.through(MockResultConsumer.class))
                        .thenApply(ServiceSpigot::getResult).get());
    }

    @Test
    public void testSorting() {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build()
                .registerServiceType(TypeToken.of(MockService.class), new DefaultMockService());
        servicePipeline.registerServiceImplementation(MockService.class, new MockOrderedFirst(),
                Collections.emptyList());
        servicePipeline.registerServiceImplementation(MockService.class, new MockOrderedLast(),
                Collections.emptyList());
        // Test that the annotations worked
        Assertions.assertEquals(1,
                servicePipeline.pump(new MockService.MockContext("")).through(MockService.class)
                        .getResult().getInteger());
    }

    @Test
    public void testRecognisedTypes() {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build()
                .registerServiceType(TypeToken.of(MockService.class), new DefaultMockService());
        Assertions.assertEquals(1, servicePipeline.getRecognizedTypes().size());
    }

    @Test
    public void testImplementationGetters() {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build()
                .registerServiceType(TypeToken.of(MockService.class), new DefaultMockService());
        servicePipeline.registerServiceImplementation(MockService.class, new MockOrderedFirst(),
                Collections.emptyList());
        servicePipeline.registerServiceImplementation(MockService.class, new MockOrderedLast(),
                Collections.emptyList());
        final TypeToken<? extends Service<?, ?>> first = TypeToken.of(MockOrderedFirst.class),
                last = TypeToken.of(MockOrderedLast.class);
        final TypeToken<MockService> mockServiceType = TypeToken.of(MockService.class);
        for (TypeToken<?> typeToken : servicePipeline.getRecognizedTypes()) {
            Assertions.assertEquals(mockServiceType, typeToken);
        }
        final Collection<? extends TypeToken<? extends Service<MockService.MockContext, MockService.MockResult>>>
                impls = servicePipeline.getImplementations(mockServiceType);
        Assertions.assertEquals(3, impls.size());
        final Iterator<? extends TypeToken<? extends Service<MockService.MockContext, MockService.MockResult>>>
                iterator = impls.iterator();
        Assertions.assertEquals(first, iterator.next());
        Assertions.assertEquals(last, iterator.next());
        Assertions.assertEquals(DefaultMockService.class, iterator.next().getRawType());
    }

    @Test
    public void testAnnotatedMethods() throws Exception {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build()
                .registerServiceType(TypeToken.of(MockService.class), new DefaultMockService())
                .registerMethods(new AnnotatedMethodTest());
        final String testString = UUID.randomUUID().toString();
        Assertions.assertEquals(testString.length(),
                servicePipeline.pump(new MockService.MockContext(testString)).through(MockService.class)
                        .getResult().getInteger());
    }

    @Test
    public void testConsumerServices() {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build()
                .registerServiceType(TypeToken.of(MockConsumerService.class),
                        new StateSettingConsumerService())
                .registerServiceImplementation(MockConsumerService.class,
                        new InterruptingMockConsumer(), Collections.emptyList());
        final MockService.MockContext context = new MockService.MockContext("");
        servicePipeline.pump(context).through(MockConsumerService.class).getResult();
        Assertions.assertEquals("", context.getState());
    }

    @Test
    public void testPartialResultServices() {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build()
                .registerServiceType(TypeToken.of(MockPartialResultService.class),
                        new DefaultPartialRequestService())
                .registerServiceImplementation(MockPartialResultService.class,
                        new CompletingPartialResultService(), Collections.emptyList());
        final MockChunkedRequest.Animal cow = new MockChunkedRequest.Animal("cow");
        final MockChunkedRequest.Animal dog = new MockChunkedRequest.Animal("dog");
        final MockChunkedRequest.Animal cat = new MockChunkedRequest.Animal("cat");
        final Map<MockChunkedRequest.Animal, MockChunkedRequest.Sound> sounds =
                servicePipeline.pump(new MockChunkedRequest(Arrays.asList(cow, dog, cat)))
                        .through(MockPartialResultService.class).getResult();
        Assertions.assertEquals("moo", sounds.get(cow).getSound());
        Assertions.assertEquals("woof", sounds.get(dog).getSound());
        Assertions.assertEquals("unknown", sounds.get(cat).getSound());
    }

    @Test
    public void testExceptions() {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build();
        Assertions.assertNotNull(servicePipeline);
        servicePipeline
                .registerServiceType(TypeToken.of(MockService.class), new DefaultMockService());
        final PipelineException pipelineException = Assertions.assertThrows(PipelineException.class,
                () -> servicePipeline.pump(new MockService.MockContext("pls throw exception"))
                        .through(MockService.class).getResult());
        Assertions.assertEquals(DefaultMockService.TotallyIntentionalException.class,
                pipelineException.getCause().getClass());
        servicePipeline.pump(new MockService.MockContext("pls throw exception"))
                .through(MockService.class).getResult((result, throwable) -> {
            Assertions.assertNotNull(throwable);
            Assertions.assertEquals(DefaultMockService.TotallyIntentionalException.class,
                    throwable.getClass());
            Assertions.assertNull(result);
        });
    }

}
