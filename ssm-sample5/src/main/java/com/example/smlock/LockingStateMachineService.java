package com.example.smlock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.util.Assert;

/**
 * Implementation of a {@link StateMachineService} which enhances
 * {@link DefaultStateMachineService} adding locking functionality based on
 * Spring Integrations {@link JdbcLockRegistry}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class LockingStateMachineService<S, E> extends DefaultStateMachineService<S, E> {

	private final static Logger log = LoggerFactory.getLogger(LockingStateMachineService.class);
	private final JdbcLockRegistry lockRegistry;
	private final Map<String, Lock> locks = new HashMap<>();

	public LockingStateMachineService(JdbcLockRegistry lockRegistry, StateMachineFactory<S, E> stateMachineFactory) {
		super(stateMachineFactory);
		Assert.notNull(lockRegistry, "'lockRegistry' must be set");
		this.lockRegistry = lockRegistry;
	}

	public LockingStateMachineService(JdbcLockRegistry lockRegistry, StateMachineFactory<S, E> stateMachineFactory,
			StateMachinePersist<S, E, String> stateMachinePersist) {
		super(stateMachineFactory, stateMachinePersist);
		Assert.notNull(lockRegistry, "'lockRegistry' must be set");
		this.lockRegistry = lockRegistry;
	}

	// @Override
	// public StateMachine<S, E> acquireStateMachine(String machineId) {
	// 	return super.acquireStateMachine(machineId);
	// }

	@Override
	public StateMachine<S, E> acquireStateMachine(String machineId, boolean start) {
		lock(machineId);
		return super.acquireStateMachine(machineId, start);
	}

	@Override
	public void releaseStateMachine(String machineId) {
		super.releaseStateMachine(machineId);
		unlock(machineId);
	}

	@Override
	public void releaseStateMachine(String machineId, boolean stop) {
		super.releaseStateMachine(machineId, stop);
		unlock(machineId);
	}

	private void lock(String machineId) {
		synchronized (locks) {
			Lock lock = this.locks.get(machineId);
			if (lock == null) {
				lock = this.lockRegistry.obtain(machineId);
			}
			log.info("Attempt to lock machine {}", machineId);
			boolean tryLock = lock.tryLock();
			if (!tryLock) {
				throw new RuntimeException("Unable to lock statemachine");
			}
			locks.put(machineId, lock);
			log.info("Locked machine {}", machineId);
		}
	}

	private void unlock(String machineId) {
		synchronized (locks) {
			Lock lock = this.locks.get(machineId);
			if (lock != null) {
				log.info("Unlocking machine {}", machineId);
				lock.unlock();
				this.locks.remove(machineId);
				log.info("Unlocked machine {}", machineId);
			}
		}
	}
}
