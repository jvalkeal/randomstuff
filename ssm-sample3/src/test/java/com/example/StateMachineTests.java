/*
 * Copyright 2016 the original author or authors.
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
package com.example;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

public class StateMachineTests extends AbstractBuildTests {

	@Test
	@SuppressWarnings("unchecked")
	public void testNoErrorGoesToClosed() throws Exception {
		context.register(DemoApplication.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("CLOSED").and()
					.build();
		plan.test();
		listener.print();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testPauseAfterThreeFailedValidations() throws Exception {
		context.register(DemoApplication.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.getExtendedState().getVariables().put("error", new RuntimeException());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					// we should have 9 state changes and end to SAVED
					.step().expectStateChanged(9).expectStates("SAVED").and()
					.build();
		plan.test();
		listener.print();
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
