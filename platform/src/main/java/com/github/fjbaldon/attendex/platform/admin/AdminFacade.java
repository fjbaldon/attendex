package com.github.fjbaldon.attendex.platform.admin;

import com.github.fjbaldon.attendex.platform.admin.dto.StewardDto;
import com.github.fjbaldon.attendex.platform.admin.dto.UserAuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AdminFacade {

    private final StewardRepository stewardRepository;
    private final PasswordEncoder passwordEncoder;

    public void createDefaultStewardIfNeeded() {
        if (stewardRepository.count() == 0) {
            Steward defaultSteward = Steward.create("admin@attendex.com", passwordEncoder.encode("admin"));
            stewardRepository.save(defaultSteward);
        }
    }

    @Transactional(readOnly = true)
    public List<StewardDto> findAllStewards() {
        return StreamSupport.stream(stewardRepository.findAll().spliterator(), false)
                .map(steward -> new StewardDto(
                        steward.getId(),
                        steward.getEmail(),
                        steward.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserAuthDto> findStewardAuthByEmail(String email) {
        return stewardRepository.findByEmail(email)
                .map(steward -> new UserAuthDto(steward.getEmail(), steward.getPassword(), "ROLE_STEWARD"));
    }
}
