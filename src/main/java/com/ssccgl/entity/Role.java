package com.ssccgl.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // ROLE_USER, ROLE_ADMIN

    public Role() {}

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId()       { return id; }
    public String getName()   { return name; }
    public void setId(Long id)       { this.id = id; }
    public void setName(String name) { this.name = name; }
}
