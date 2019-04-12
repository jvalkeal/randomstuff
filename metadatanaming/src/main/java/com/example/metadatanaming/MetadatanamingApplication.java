package com.example.metadatanaming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class MetadatanamingApplication implements CommandLineRunner {

	@Autowired
	private MetadataResolver metadataResolver;

	@Override
	public void run(String... args) throws Exception {
		List<ConfigurationMetadataProperty> properties = metadataResolver.resolve();
		properties.stream().sorted(Comparator.comparing(a -> a.getId())).forEach(p -> {
			System.out.println(p.getId() + " / " + p.getName());
		});
	}

	@Component
	public class MetadataResolver implements ApplicationContextAware {

		private static final String CONFIGURATION_METADATA_PATTERN = "classpath*:/META-INF/spring-configuration-metadata.json";
		private static final String KEY_PREFIX = "spring.rabbitmq.listener.";
		private ApplicationContext applicationContext;

		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = applicationContext;
		}

		public List<ConfigurationMetadataProperty> resolve() {
			List<ConfigurationMetadataProperty> metadataProperties = new ArrayList<>();
			ConfigurationMetadataRepositoryJsonBuilder builder = ConfigurationMetadataRepositoryJsonBuilder.create();
			try {
				Resource[] resources = applicationContext.getResources(CONFIGURATION_METADATA_PATTERN);
				for (Resource resource : resources) {
					builder.withJsonResource(resource.getInputStream());
				}
			}
			catch (IOException e) {
				throw new RuntimeException("Unable to read configuration metadata", e);
			}
			ConfigurationMetadataRepository metadataRepository = builder.build();
			Map<String, ConfigurationMetadataGroup> groups = metadataRepository.getAllGroups();
			groups.values().stream()
				.filter(g -> g.getId().startsWith(KEY_PREFIX))
				.forEach(g -> {
					g.getProperties().values().stream()
						.forEach(p -> {
							metadataProperties.add(p);
						});
				});
			return metadataProperties;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(MetadatanamingApplication.class, args);
	}
}
