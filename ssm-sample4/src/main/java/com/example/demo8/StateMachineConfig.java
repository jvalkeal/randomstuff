package com.example.demo8;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends StateMachineConfigurerAdapter<String, String> {

	@Override
	public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
		model
			.withModel()
				.factory(modelFactory());
	}

	@Bean
	public StateMachineModelFactory<String, String> modelFactory() {
		return new UmlStateMachineModelFactory(
			"classpath:com/example/demo8/import-main/import-main.uml",
			new String[] { "classpath:com/example/demo8/import-sub/import-sub.uml" });
	}
}
