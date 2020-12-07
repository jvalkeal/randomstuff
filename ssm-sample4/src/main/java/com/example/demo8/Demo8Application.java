package com.example.demo8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.statemachine.StateMachine;

@SpringBootApplication
public class Demo8Application implements CommandLineRunner {

	private final static Logger log = LoggerFactory.getLogger(Demo8Application.class);

	@Autowired
	private StateMachine<String, String> stateMachine;

	@Override
	public void run(String... args) throws Exception {
		stateMachine.start();
		log.info("State {}", stateMachine.getState());
	}

	public static void main(String[] args) {
		SpringApplication.run(Demo8Application.class, args);
	}
}
