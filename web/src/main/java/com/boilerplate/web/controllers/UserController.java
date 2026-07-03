package com.boilerplate.web.controllers;

import com.boilerplate.application.auth.contracts.usecases.users.*;
import com.boilerplate.application.auth.criteria.UserCriteria;
import com.boilerplate.domain.auth.enums.UserStatus;
import com.boilerplate.application.auth.schemas.request.ChangePasswordRequest;
import com.boilerplate.application.auth.schemas.request.UpdateUserRequest;
import com.boilerplate.application.auth.schemas.response.UserResponse;
import com.boilerplate.application.common.pagination.Page;
import com.boilerplate.application.common.pagination.Pageable;
import com.boilerplate.application.common.schemas.UUIDResponse;
import com.boilerplate.infrastructure.auth.data.entities.UserEntity;
import com.boilerplate.web.security.AdminOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UpdateUserUseCase update;
    private final ChangePasswordUseCase changePassword;
    private final ToggleUserUseCase toggle;
    private final PromoteUserUseCase promote;
    private final DemoteUserUseCase demote;
    private final GetUserByIdUseCase getById;
    private final GetUserByEmailUseCase getByEmail;
    private final ListUsersUseCase list;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getById.execute(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(getByEmail.execute(email));
    }

    @GetMapping
    @AdminOnly
    public ResponseEntity<Page<UserResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        var criteria = new UserCriteria(name, role, status);
        var pageable = Pageable.of(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(list.execute(criteria, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UUIDResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        return ResponseEntity.ok(update.execute(id, request));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        changePassword.execute(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    @AdminOnly
    public ResponseEntity<Void> toggle(@PathVariable UUID id) {
        toggle.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/promote")
    @AdminOnly
    public ResponseEntity<Void> promote(@PathVariable UUID id) {
        promote.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/demote")
    @AdminOnly
    public ResponseEntity<Void> demote(@PathVariable UUID id) {
        demote.execute(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isCurrentUser(UUID userId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserEntity user)) {
            return false;
        }

        return user.getId().equals(userId);
    }
}