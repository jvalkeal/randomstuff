package com.example.reactorretrywhenissue;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;
import reactor.test.StepVerifier;
import reactor.util.concurrent.Queues;
import reactor.util.retry.Retry;

class ReactorRetrywhenIssueApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(ReactorRetrywhenIssueApplicationTests.class);

	private Many<String> triggerSink  = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	public void test() throws Exception {
		triggerSink.asFlux().subscribe();

		int count = 10;
		List<Thread> threads = new ArrayList<>();
		List<AtomicReference<Throwable>> errors = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			AtomicReference<Throwable> error = new AtomicReference<>();
			threads.add(threadJob("T" + i, error));
			errors.add(error);
		}
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();
		}
		for (AtomicReference<Throwable> error : errors) {
			assertThat(error.get()).isNull();
		}
	}

	private Thread threadJob(String id, AtomicReference<Throwable> error) {
		AtomicInteger i = new AtomicInteger();
		Thread t = new Thread(() -> {
			log.info(id + " start");
			while(i.incrementAndGet() < 200) {
				log.info(id + ": " + i.get());
				try {
					consume(process());
				} catch (Throwable e) {
					log.error(id + ": error " + e);
					error.set(e);
					break;
				}
			}
			log.info(id + ": end");
		});
		return t;
	}

	private Flux<String> messages() {
		return Flux.just(Long.toString(System.currentTimeMillis()), Long.toString(System.currentTimeMillis()));
	}

	private Mono<Void> process() {
		return messages()
			.log("X1")
			.<Void>flatMap(m -> Mono.fromRunnable(() -> {
				triggerSink.emitNext(m, EmitFailureHandler.FAIL_FAST);
			}))
			.log("X2")
			.retryWhen(Retry.fixedDelay(10, Duration.ofNanos(1)))
			.log("X3")
			.then()
			;
	}

	public static void consume(Mono<Void> aVoid) {
		StepVerifier.create(aVoid)
			.thenConsumeWhile(x -> true)
			.expectComplete()
			.verify(Duration.ofSeconds(2));
	}
}
