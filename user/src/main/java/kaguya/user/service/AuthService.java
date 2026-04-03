package kaguya.user.service;

import kaguya.user.model.dto.response.RegisterRes;
import kaguya.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public RegisterRes register() {
        return null;
    }
}
