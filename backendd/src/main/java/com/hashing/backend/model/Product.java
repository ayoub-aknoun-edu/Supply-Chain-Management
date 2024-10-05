package com.hashing.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String hash;

    @Column(nullable = false)
    @CreatedDate
    private Date date = new Date();

    @Column(nullable = false, length = 1000)
    private String signature;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "signer_id", nullable = false)
    private User signer;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    private String filePath;
    @Enumerated(EnumType.STRING)
    private Status status;


    public enum Status {
        CREATED, SIGNED, VERIFIED, INSECURE
    }
}