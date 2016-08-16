package com.example;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;

@Configuration
public class StateMachineConfig {

	@Bean
	public StateMachineFactory<String, String> stateMachineFactory(
			StateMachineModelFactory<String, String> stateMachineModelFactory, BeanFactory beanFactory) {
		return new DbStateMachineFactory(stateMachineModelFactory, beanFactory);
	}

	@Bean
	public StateMachineModelFactory<String, String> stateMachineModelFactory(StatesRepository statesRepository,
			TransitionsRepository transitionsRepository) {
		return new DbStateMachineModelFactory(statesRepository, transitionsRepository);
	}

	private static class DbStateMachineFactory implements StateMachineFactory<String, String> {

		private final StateMachineModelFactory<String, String> stateMachineModelFactory;
		private final BeanFactory beanFactory;

		public DbStateMachineFactory(StateMachineModelFactory<String, String> stateMachineModelFactory, BeanFactory beanFactory) {
			this.stateMachineModelFactory = stateMachineModelFactory;
			this.beanFactory = beanFactory;
		}

		@Override
		public StateMachine<String, String> getStateMachine() {
			return getStateMachine(null);
		}

		@Override
		public StateMachine<String, String> getStateMachine(String machineId) {
			Builder<String, String> builder = StateMachineBuilder.builder();
			try {
				builder.configureConfiguration()
					.withConfiguration()
						.beanFactory(beanFactory);
				builder.configureModel()
					.withModel()
						.factory(stateMachineModelFactory);
			} catch (Exception e) {
				throw new RuntimeException();
			}
			return builder.build();
		}
	}

	private static class DbStateMachineModelFactory implements StateMachineModelFactory<String, String> {

		private final StatesRepository statesRepository;
		private final TransitionsRepository transitionsRepository;

		public DbStateMachineModelFactory(StatesRepository statesRepository, TransitionsRepository transitionsRepository) {
			this.statesRepository = statesRepository;
			this.transitionsRepository = transitionsRepository;
		}

		@Override
		public StateMachineModel<String, String> build() {
			ConfigurationData<String, String> configurationData = new ConfigurationData<>();

			Collection<StateData<String, String>> stateData = new ArrayList<>();
			for (States s : statesRepository.findAll()) {
				stateData.add(new StateData<String, String>(s.getState(), s.isInitial()));
			}
			StatesData<String, String> statesData = new StatesData<>(stateData);

			Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
			for (Transitions t : transitionsRepository.findAll()) {
				transitionData.add(new TransitionData<String, String>(t.getSource(), t.getTarget(), t.getEvent()));
			}
			TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData);

			StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(configurationData, statesData, transitionsData);
			return stateMachineModel;
		}
	}
}
