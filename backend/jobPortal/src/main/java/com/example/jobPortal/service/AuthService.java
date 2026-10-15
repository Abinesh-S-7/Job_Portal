package com.example.jobPortal.service;

import com.example.jobPortal.dtos.auth.AuthResponse;
import com.example.jobPortal.dtos.auth.LoginRequest;
import com.example.jobPortal.dtos.auth.RegisterRequest;
import com.example.jobPortal.models.CandidateProfile;
import com.example.jobPortal.models.Company;
import com.example.jobPortal.models.Role;
import com.example.jobPortal.models.User;
import com.example.jobPortal.repository.CandidateRepository;
import com.example.jobPortal.repository.CompanyRepository;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request)
    {
        System.out.println("Incoming RegisterRequest: " + request);
        if(userRepository.existsByEmail(request.email()))
        {
            throw new RuntimeException("Email Already Registered");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());

        User savedUser = userRepository.save(user);

        if(savedUser.getRole()== Role.CANDIDATE)
        {
            CandidateProfile candidate = new CandidateProfile();
            candidate.setUser(savedUser);
            candidateRepository.save(candidate);
        }else if(savedUser.getRole()==Role.RECRUITER)
        {
            Company company = new Company();
            company.setRecruiter(savedUser);
            company.setCompanyName(savedUser.getName() + "'s Company");
            companyRepository.save(company);
        }

        String token = jwtUtils.generateToken(savedUser.getId(),savedUser.getEmail(),savedUser.getRole().name());
        return new AuthResponse(
                token,
                "Bearer",
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    public AuthResponse login(LoginRequest request)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),request.password()
                )
        );
        //fetch user

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(()-> new RuntimeException("Invalid Email or Password"));

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
