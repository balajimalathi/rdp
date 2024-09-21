package com.skndan.rdp.model;

public record UserRegistrationRecord(
  String username,
  String email,
  String firstName,
  String lastName,
  String password) {
}
