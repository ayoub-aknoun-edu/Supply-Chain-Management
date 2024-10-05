package com.hashing.backend.service;

import com.hashing.backend.model.Product;
import com.hashing.backend.model.User;
import com.hashing.backend.repository.ProductRepository;
import com.hashing.backend.repository.UserRepository;
import com.hashing.backend.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final DigitalSignatureService digitalSignatureService;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;

    public UserService(UserRepository userRepository, DigitalSignatureService digitalSignatureService, @Lazy PasswordEncoder passwordEncoder, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.digitalSignatureService = digitalSignatureService;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new CustomUserDetails(user);
    }

    public User createUser(User user) throws Exception {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var keyPair = digitalSignatureService.generateKeyPair();
        user.setPublicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        user.setPrivateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        return userRepository.save(user);
    }

    public HashMap<String, String> secretKeys(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        HashMap<String, String> keys = new HashMap<>();
        keys.put("publicKey", user.getPublicKey());
        keys.put("privateKey", user.getPrivateKey());
        return keys;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public Optional<User> findByUsernameCmd(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // get all users except the current user
    public List<User> findAllUsersExceptMe(String username) {
        return userRepository.findAllByUsernameNot(username);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
                .orElse(null);
        if (user == null) {
            return;
        }
        productRepository.deleteAllBySender(user);
        productRepository.deleteAllByReceiver(user);
        userRepository.deleteById(id);
    }

}
