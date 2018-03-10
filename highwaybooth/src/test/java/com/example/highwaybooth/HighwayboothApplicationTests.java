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

import static com.example.highwaybooth.HighwayboothApplication.EVENT_AFTER_ENTER;
import static com.example.highwaybooth.HighwayboothApplication.EVENT_AFTER_EXIT;
import static com.example.highwaybooth.HighwayboothApplication.EVENT_DETECT_VEHICLE;
import static com.example.highwaybooth.HighwayboothApplication.EVENT_PAYMENT_ADD;
import static com.example.highwaybooth.HighwayboothApplication.HEADER_PAYMENT;
import static com.example.highwaybooth.HighwayboothApplication.HEADER_VEHICLE;
import static com.example.highwaybooth.HighwayboothApplication.STATE_CONTROL;
import static com.example.highwaybooth.HighwayboothApplication.STATE_TRAFFICLIGHT_GREEN;
import static com.example.highwaybooth.HighwayboothApplication.STATE_TRAFFICLIGHT_RED;
import static com.example.highwaybooth.HighwayboothApplication.STATE_TURNSTILE_CLOSED;
import static com.example.highwaybooth.HighwayboothApplication.STATE_TURNSTILE_OPEN;
import static com.example.highwaybooth.HighwayboothApplication.VARIABLE_BALANCE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.highwaybooth.HighwayboothApplication.VEHICLES;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class HighwayboothApplicationTests {

	@Autowired
	private StateMachine<String, String> stateMachine;

	@Test
	public void testInitial() {
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_RED, STATE_TURNSTILE_CLOSED);
	}

	@Test
	public void testCarReturnChange() {
		detect(VEHICLES.CAR).pay(25).pay(25);
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_RED, STATE_TURNSTILE_CLOSED);
		assertBalance(-100);
		pay(200);
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_GREEN, STATE_TURNSTILE_OPEN);
		assertBalance(null);
		exit();
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_RED, STATE_TURNSTILE_CLOSED);
	}

	@Test
	public void testTruckExactAmount() {
		detect(VEHICLES.TRUCK).pay(25).pay(25);
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_RED, STATE_TURNSTILE_CLOSED);
		assertBalance(-200);
		pay(200);
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_GREEN, STATE_TURNSTILE_OPEN);
		assertBalance(null);
		exit();
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_RED, STATE_TURNSTILE_CLOSED);
	}

	@Test
	public void testMotorcycle() {
		detect(VEHICLES.MOTORCYCLE).pay(50);
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_RED, STATE_TURNSTILE_CLOSED);
		assertBalance(-50);
		pay(50);
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_GREEN, STATE_TURNSTILE_OPEN);
		assertBalance(null);
		exit();
		assertStates(STATE_CONTROL, STATE_TRAFFICLIGHT_RED, STATE_TURNSTILE_CLOSED);
	}

	private void assertStates(String... states) {
		assertThat(stateMachine.getState().getIds()).containsExactlyInAnyOrder(states);
	}

	private void assertBalance(Integer balance) {
		assertThat(stateMachine.getExtendedState().get(VARIABLE_BALANCE, Integer.class)).isEqualTo(balance);
	}

	private HighwayboothApplicationTests detect(VEHICLES vehicle) {
		stateMachine.sendEvent(MessageBuilder.withPayload(EVENT_DETECT_VEHICLE).setHeader(HEADER_VEHICLE, vehicle).build());
		return this;
	}

	private HighwayboothApplicationTests pay(Integer cents) {
		stateMachine.sendEvent(MessageBuilder.withPayload(EVENT_PAYMENT_ADD).setHeader(HEADER_PAYMENT, cents).build());
		return this;
	}

	private HighwayboothApplicationTests exit() {
		stateMachine.sendEvent(EVENT_AFTER_ENTER);
		stateMachine.sendEvent(EVENT_AFTER_EXIT);
		return this;
	}
}
