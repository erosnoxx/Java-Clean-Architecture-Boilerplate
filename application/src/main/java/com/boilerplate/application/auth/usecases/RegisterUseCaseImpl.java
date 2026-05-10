package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.auth.RegisterUseCase;
import com.boilerplate.application.auth.schemas.request.RegisterRequest;
import com.boilerplate.application.auth.contracts.ports.PasswordEncoderPort;
import com.boilerplate.application.common.schemas.UUIDResponse;
import com.boilerplate.domain.auth.entities.User;
import com.boilerplate.domain.auth.enums.UserRole;
import com.boilerplate.domain.shared.vos.Email;
import com.boilerplate.domain.auth.vos.FullName;
import com.boilerplate.domain.auth.vos.Password;
import com.boilerplate.domain.common.exceptions.ConflictException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterUseCaseImpl implements RegisterUseCase {
    private final UserRepository repository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    public UUIDResponse execute(RegisterRequest request) {
        if (repository.findByEmail(request.email()).isPresent())
            throw new ConflictException("User with email: " + request.email() + " already exists");

        if (repository.existsByRole(UserRole.ADMIN) && request.role() == UserRole.ADMIN)
            throw new ConflictException("Admin user already exists");

        var hashed = Password.fromHashed(
                passwordEncoder.encode(request.password())
        );

        var user = repository.save(
                User.create(
                        FullName.of(request.name()),
                        Email.of(request.email()),
                        hashed,
                        request.role()
                ));

        return UUIDResponse.of(user.getId());
    }
}
