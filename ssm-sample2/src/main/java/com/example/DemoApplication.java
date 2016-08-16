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

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	@Autowired
	private StatesRepository statesRepository;

	@Autowired
	private TransitionsRepository transitionsRepository;

	@Override
	public void run(String... args) throws Exception {
		statesRepository.save(new States("S1", true));
		statesRepository.save(new States("S2"));
		statesRepository.save(new States("S3"));
		transitionsRepository.save(new Transitions("S1", "S2", "E1"));
		transitionsRepository.save(new Transitions("S2", "S3", "E2"));
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
