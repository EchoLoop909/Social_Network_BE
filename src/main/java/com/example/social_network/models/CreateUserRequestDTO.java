package com.example.social_network.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateUserRequestDTO {
    private String username;
    private String email;
    private String name;
    private String surname;
    private String description;
    private String photo;
    private String status;
    private String firstname;
    private String lastname;
    private String keycloakId;
}