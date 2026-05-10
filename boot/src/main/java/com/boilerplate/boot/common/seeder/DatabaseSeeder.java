package com.boilerplate.boot.common.seeder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class DatabaseSeeder implements ApplicationRunner {
    private final List<Seeder> seeders;
    private final ObjectMapper objectMapper;

    @Override
    @SuppressWarnings("unchecked")
    public void run(ApplicationArguments args) throws Exception {
        log.info("starting database seeding...");

        for (var seeder : seeders) {
            log.info("seeding: {}", seeder.getClass().getSimpleName());

            var resource = new ClassPathResource(seeder.resourcePath());
            var type = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, seeder.recordType());
            List<?> data = objectMapper.readValue(resource.getInputStream(), type);
            seeder.seed((List) data);
        }

        log.info("database seeding completed.");
    }
}
