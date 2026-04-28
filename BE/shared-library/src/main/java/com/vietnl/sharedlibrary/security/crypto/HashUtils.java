package com.vietnl.sharedlibrary.security.crypto;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashUtils {

  private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

  public static String encodeBCrypt(String sequence) {

    return bCryptPasswordEncoder.encode(sequence);
  }

  public static boolean matchBcrypt(String sequence, String hashedSequence) {

    return bCryptPasswordEncoder.matches(sequence, hashedSequence);
  }

  public static String encode(HashType type, String sequence) {
    return switch (type) {
      case BCRYPT -> encodeBCrypt(sequence);
      default -> throw new IllegalArgumentException("Invalid hash type");
    };
  }

  public static boolean match(HashType type, String sequence, String hashedSequence) {
    return switch (type) {
      case BCRYPT -> matchBcrypt(sequence, hashedSequence);
      default -> throw new IllegalArgumentException("Invalid hash type");
    };
  }
}
