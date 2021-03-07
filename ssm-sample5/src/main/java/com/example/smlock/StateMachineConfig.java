package com.example.smlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.StateMachineService;

@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<String, String> {

	private final static Logger log = LoggerFactory.getLogger(StateMachineConfig.class);

	@Bean
	public StateMachineService<String, String> stateMachineService(JdbcLockRegistry lockRegistry,
			StateMachineFactory<String, String> factory, StateMachinePersist<String, String, String> persist) {
		return new LockingStateMachineService<>(lockRegistry, factory, persist);
	}

	@Bean
	public StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister(
			JpaStateMachineRepository jpaStateMachineRepository) {
		return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
	}

	@Configuration
	@EnableStateMachineFactory
	public class Config extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister;

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withPersistence()
					.runtimePersister(stateMachineRuntimePersister);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.action(context -> {
						log.info("Sleeping 10s in S1-S2 transition");
						try {
							Thread.sleep(10000);
						} catch (Exception e) {
						}
					})
					.and()
				.withExternal()
					.source("S2")
					.target("S1")
					.event("E2");
		}
	}
}
