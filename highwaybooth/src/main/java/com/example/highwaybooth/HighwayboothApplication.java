/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.highwaybooth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@SpringBootApplication
public class HighwayboothApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(HighwayboothApplication.class);

	public static final String STATE_INITIAL = "INITIAL";
	public static final String STATE_CONTROL = "CONTROL";
	public static final String STATE_TRAFFICLIGHT_GREEN = "TRAFFICLIGHT_GREEN";
	public static final String STATE_TRAFFICLIGHT_RED = "TRAFFICLIGHT_RED";
	public static final String STATE_TURNSTILE_OPEN = "TURNSTILE_OPEN";
	public static final String STATE_TURNSTILE_CLOSED = "TURNSTILE_CLOSED";

	public static final String EVENT_DETECT_VEHICLE = "DETECT_VEHICLE";
	public static final String EVENT_AFTER_ENTER = "AFTER_ENTER";
	public static final String EVENT_AFTER_EXIT = "AFTER_EXIT";
	public static final String EVENT_PAYMENT_ADD = "PAYMENT_ADD";

	public static final String HEADER_PAYMENT = "PAYMENT";
	public static final String HEADER_VEHICLE = "VEHICLE";

	public static final String VARIABLE_BALANCE = "BALANCE";

	public enum VEHICLES {
		CAR(150), TRUCK(250), MOTORCYCLE(100);
		private Integer charge;

		VEHICLES(Integer charge) {
			this.charge = charge;
		}

		public Integer getCharge() {
			return charge;
		}
	}

	@EnableStateMachine
	public static class StateMachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			// autostart and add simple logger for state entrys
			config
				.withConfiguration()
					.autoStartup(true)
					.listener(new StateMachineListenerAdapter<String, String>() {
						@Override
						public void stateEntered(State<String, String> state) {
							log.info("Enter state {}", state.getId());
						}
					});
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			// define INITIAL state where you could do init actions
			// define CONTROL state which is wrapping two regions
			// as traffic light and turnstile are independent.
			states
				.withStates()
					.initial(STATE_INITIAL)
					.state(STATE_CONTROL)
					.and()
					.withStates()
						.parent(STATE_CONTROL)
						.initial(STATE_TRAFFICLIGHT_RED)
						.stateEntry(STATE_TRAFFICLIGHT_GREEN, balanceResetAction())
						.and()
					.withStates()
						.parent(STATE_CONTROL)
						.initial(STATE_TURNSTILE_CLOSED)
						.state(STATE_TURNSTILE_OPEN);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				// we just pass through to control
				.withExternal()
					.source(STATE_INITIAL)
					.target(STATE_CONTROL)
					.and()
				// we go green automatically when payment is made
				.withExternal()
					.source(STATE_TRAFFICLIGHT_RED)
					.target(STATE_TRAFFICLIGHT_GREEN)
					.guard(paymentGuard())
					.and()
				// sensor seeing car fully exiting turns light red
				.withExternal()
					.source(STATE_TRAFFICLIGHT_GREEN)
					.target(STATE_TRAFFICLIGHT_RED)
					.event(EVENT_AFTER_EXIT)
					.and()
					// we go open automatically when payment is made
				.withExternal()
					.source(STATE_TURNSTILE_CLOSED)
					.target(STATE_TURNSTILE_OPEN)
					.guard(paymentGuard())
					.and()
				// sensor seeing car starting to exit turns gate down
				.withExternal()
					.source(STATE_TURNSTILE_OPEN)
					.target(STATE_TURNSTILE_CLOSED)
					.event(EVENT_AFTER_ENTER)
					.and()
				// payment is handled as internal transition which
				// doesn't cause actual state change
				// we just want to handle event with money
				.withInternal()
					.source(STATE_CONTROL)
					.action(paymentAddedAction())
					.event(EVENT_PAYMENT_ADD)
					.and()
				// we just want to handle even from car detector
				.withInternal()
					.source(STATE_CONTROL)
					.action(vehicleDetectedAction())
					.event(EVENT_DETECT_VEHICLE);
		}

		@Bean
		public Guard<String, String> paymentGuard() {
			// guards transition not to happen if balance is missing or negative
			return context -> {
				Integer balance = context.getExtendedState().get(VARIABLE_BALANCE, Integer.class);
				return balance != null && balance >= 0;
			};
		}

		@Bean
		public Action<String, String> balanceResetAction() {
			// reset balance away and return change
			return context -> {
				Integer balance = context.getExtendedState().get(VARIABLE_BALANCE, Integer.class);
				log.info("Balance left returning {}", balance);
				context.getExtendedState().getVariables().clear();
			};
		}

		@Bean
		public Action<String, String> paymentAddedAction() {
			return context -> {
				Integer payment = context.getMessageHeaders().get(HEADER_PAYMENT, Integer.class);
				Integer balance = context.getExtendedState().get(VARIABLE_BALANCE, Integer.class);
				balance += payment;
				context.getExtendedState().getVariables().put(VARIABLE_BALANCE, balance);
				log.info("Received payment {} new balance is {}", payment, balance);
			};
		}

		@Bean
		public Action<String, String> vehicleDetectedAction() {
			// handle car detection and add negative balance from vehicly type
			return context -> {
				VEHICLES vehicle = context.getMessageHeaders().get(HEADER_VEHICLE, VEHICLES.class);
				Integer balance = -vehicle.getCharge();
				context.getExtendedState().getVariables().put(VARIABLE_BALANCE, balance);
				log.info("Vehicle {} detected, reset balance to {}", vehicle, balance);
			};
		}
	}

	@Autowired
	private StateMachine<String, String> stateMachine;

	@Override
	public void run(String... args) throws Exception {
		// simple example a car going throught and paying 5 cents too much
		Message<String> detectCarMessage = MessageBuilder
				.withPayload(EVENT_DETECT_VEHICLE)
				.setHeader(HEADER_VEHICLE, VEHICLES.CAR).build();
		Message<String> payQuarterMessage = MessageBuilder
				.withPayload(EVENT_PAYMENT_ADD)
				.setHeader(HEADER_PAYMENT, 25).build();
		Message<String> payDimeMessage = MessageBuilder
				.withPayload(EVENT_PAYMENT_ADD)
				.setHeader(HEADER_PAYMENT, 10).build();

		stateMachine.sendEvent(detectCarMessage);
		stateMachine.sendEvent(payQuarterMessage);
		stateMachine.sendEvent(payQuarterMessage);
		stateMachine.sendEvent(payQuarterMessage);
		stateMachine.sendEvent(payQuarterMessage);
		stateMachine.sendEvent(payQuarterMessage);
		stateMachine.sendEvent(payDimeMessage);
		stateMachine.sendEvent(payDimeMessage);
		stateMachine.sendEvent(payDimeMessage);
		stateMachine.sendEvent(EVENT_AFTER_ENTER);
		stateMachine.sendEvent(EVENT_AFTER_EXIT);
	}

	public static void main(String[] args) {
		SpringApplication.run(HighwayboothApplication.class, args);
	}
}
