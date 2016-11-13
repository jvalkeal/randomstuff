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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	@Autowired
	private StateMachine<String, String> stateMachine;

	@Override
	public void run(String... args) throws Exception {
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();
		listener.print();
	}

	@Configuration
	@EnableStateMachine
	public static class Config extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:model.uml");
		}

		@Bean
		public Guard<String, String> hasErrorGuard() {
			return context -> {
				return context.getExtendedState().getVariables().containsKey("error");
			};
		}

		@Bean
		public Guard<String, String> shouldPauseGuard() {
			return context -> {
				Integer validations = context.getExtendedState().get("validations", Integer.class);
				return validations != null && validations >= 3;
			};
		}

		@Bean
		public Guard<String, String> isPausedGuard() {
			return context -> {
				Integer validations = context.getExtendedState().get("validations", Integer.class);
				if (validations == null) {
					return true;
				} else {
					return validations < 3;
				}
			};
		}

		@Bean
		public Action<String, String> validateAction() {
			return context -> {
				Integer validations = context.getExtendedState().get("validations", Integer.class);
				if (validations == null) {
					validations = 1;
				} else {
					validations++;
				}
				context.getExtendedState().getVariables().put("validations", validations);
			};
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
