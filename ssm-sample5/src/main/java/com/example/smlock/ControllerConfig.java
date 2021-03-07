package com.example.smlock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ControllerConfig {

	@Autowired
	private StateMachineService<String, String> service;

	@GetMapping(path = "/state")
	public String getState() {
		StateMachine<String, String> machine = getLockedMachine("test");
		String state = machine.getState().toString();
		service.releaseStateMachine("test");
		return state;
	}

	@PostMapping(path = "/event", params = "id")
	public void processEvent(@RequestParam("id") String event) {
		StateMachine<String, String> machine = getLockedMachine("test");
		machine.sendEvent(event);
		service.releaseStateMachine("test");
	}

	private StateMachine<String, String> getLockedMachine(String machineId) {
		try {
			return service.acquireStateMachine(machineId);
		} catch (Exception e) {
			throw new LockFailureException(String.format("Unable to get locked machine %s", machineId), e);
		}
	}

	private static class LockFailureException extends ResponseStatusException {

		private static final long serialVersionUID = -8336926125114637157L;

		LockFailureException(String reason, Throwable cause) {
			super(HttpStatus.LOCKED, reason, cause);
		}
	}
}
