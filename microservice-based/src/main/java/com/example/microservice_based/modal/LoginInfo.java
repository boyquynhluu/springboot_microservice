package com.example.microservice_based.modal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginInfo {
    private String accessToken;
    private String tokenType = "Bearer";
}
