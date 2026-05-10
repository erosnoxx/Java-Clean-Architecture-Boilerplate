package com.boilerplate.boot.config.seeder;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.auth.RegisterUseCase;
import com.boilerplate.application.auth.schemas.request.RegisterRequest;
import com.boilerplate.boot.common.seeder.Seeder;
import com.boilerplate.domain.auth.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements Seeder<AdminSeeder.UserSeedRecord> {

    private final RegisterUseCase registerUseCase;
    private final UserRepository userRepository;

    @Override
    public String resourcePath() {
        return "seeders/users.json";
    }

    @Override
    public Class<UserSeedRecord> recordType() {
        return UserSeedRecord.class;
    }

    @Override
    public void seed(List<UserSeedRecord> data) {
        for (var seed : data) {
            if (userRepository.findByEmail(seed.email()).isPresent()) {
                log.info("user already exists, skipping: {}", seed.email());
                continue;
            }

            registerUseCase.execute(new RegisterRequest(
                    seed.name(),
                    seed.email(),
                    seed.password(),
                    UserRole.valueOf(seed.role())
            ));

            log.info("seeded user: {}", seed.email());
        }
    }

    public record UserSeedRecord(
            String name,
            String email,
            String password,
            String role
    ) {}
}