package com.boilerplate.application.auth.usecases;

import com.boilerplate.application.auth.contracts.ports.PasswordEncoderPort;
import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.users.ChangePasswordUseCase;
import com.boilerplate.application.auth.schemas.request.ChangePasswordRequest;
import com.boilerplate.domain.auth.vos.Password;
import com.boilerplate.domain.common.exceptions.DomainException;
import com.boilerplate.domain.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ChangePasswordUseCaseImpl implements ChangePasswordUseCase {
    private final UserRepository repository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    public void execute(UUID id, ChangePasswordRequest request) {
        var user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("user not found"));

        var currentMatches = passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword().getValue());

        if (!currentMatches)
            throw new DomainException("current password is incorrect");

        var hashed = Password.fromHashed(
                passwordEncoder.encode(request.newPassword())
        );

        user.changePassword(hashed);

        repository.save(user);
    }
}
