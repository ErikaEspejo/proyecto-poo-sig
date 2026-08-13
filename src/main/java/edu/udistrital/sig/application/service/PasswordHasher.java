package edu.udistrital.sig.application.service;

public interface PasswordHasher {

    String hash(String plainText);
}
