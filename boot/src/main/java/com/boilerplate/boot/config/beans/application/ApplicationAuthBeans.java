package com.boilerplate.boot.config.beans.application;

import com.boilerplate.application.auth.contracts.ports.AuthenticationPort;
import com.boilerplate.application.auth.contracts.ports.PasswordEncoderPort;
import com.boilerplate.application.auth.contracts.ports.TokenProviderPort;
import com.boilerplate.application.auth.contracts.repositories.UserRepository;
import com.boilerplate.application.auth.contracts.usecases.auth.LoginUseCase;
import com.boilerplate.application.auth.contracts.usecases.auth.RefreshTokenUseCase;
import com.boilerplate.application.auth.contracts.usecases.auth.RegisterUseCase;
import com.boilerplate.application.auth.contracts.usecases.users.*;
import com.boilerplate.application.auth.mappers.UserMapper;
import com.boilerplate.application.auth.usecases.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ApplicationAuthBeans {

    private final AuthenticationPort authenticationPort;
    private final TokenProviderPort tokenProviderPort;
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final UserMapper userMapper;

    @Bean
    public LoginUseCase loginUseCase() {
        return new LoginUseCaseImpl(authenticationPort, tokenProviderPort);
    }

    @Bean
    public RegisterUseCase registerUseCase() {
        return new RegisterUseCaseImpl(userRepository, passwordEncoderPort);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase() {
        return new RefreshTokenUseCaseImpl(tokenProviderPort);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase() {
        return new UpdateUserUseCaseImpl(userRepository);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase() {
        return new ChangePasswordUseCaseImpl(userRepository, passwordEncoderPort);
    }

    @Bean
    public ToggleUserUseCase toggleUserUseCase() {
        return new ToggleUserUseCaseImpl(userRepository);
    }

    @Bean
    public PromoteUserUseCase promoteUserUseCase() {
        return new PromoteUserUseCaseImpl(userRepository);
    }

    @Bean
    public DemoteUserUseCase demoteUserUseCase() {
        return new DemoteUserUseCaseImpl(userRepository);
    }

    @Bean
    public GetUserByIdUseCase getUserByIdUseCase() {
        return new GetUserByIdUseCaseImpl(userRepository, userMapper);
    }

    @Bean
    public GetUserByEmailUseCase getUserByEmailUseCase() {
        return new GetUserByEmailUseCaseImpl(userRepository, userMapper);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase() {
        return new ListUsersUseCaseImpl(userRepository, userMapper);
    }
}