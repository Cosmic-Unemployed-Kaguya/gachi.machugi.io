package kaguya.user.user.service;

import kaguya.user.user.model.dto.response.RegisterRes;
import kaguya.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public RegisterRes register() {
        return null;
    }
}
