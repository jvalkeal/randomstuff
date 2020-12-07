package com.example.demo8;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

@SpringBootTest
class Demo8ApplicationTests {

	@Autowired
	private StateMachine<String, String> stateMachine;

	@Test
	void contextLoads() {
		stateMachine.start();
		assertThat(stateMachine.getState().getIds()).contains("MAIN2", "CHILD2");
	}
}
