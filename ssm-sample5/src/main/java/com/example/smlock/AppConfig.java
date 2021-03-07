package com.example.smlock;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;

@Configuration
public class AppConfig {

	@Value("${spring.application.name}")
	private String clientId;

	@Bean
	DefaultLockRepository defaultLockRepository(DataSource dataSource) {
		DefaultLockRepository repository = new DefaultLockRepository(dataSource, clientId);
		repository.setPrefix("STATEMACHINE_");
		repository.setTimeToLive(60000);
		return repository;
	}

	@Bean
	JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository) {
		return new JdbcLockRegistry(lockRepository);
	}
}
