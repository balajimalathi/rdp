package com.skndan.rdp.model;

public record UserRecord(
  String id,
  String username,
  String email,
  String firstName,
  String lastName,
  String password) {
}
