package com.audaque.hiveudf;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.sasl.AuthenticationException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hive.service.auth.PasswdAuthenticationProvider;

public class CustomPasswdAuthenticator implements PasswdAuthenticationProvider {
  
  private Map<String, String> userPasswordMap = new HashMap<String, String>();
  
  public CustomPasswdAuthenticator() {
    init();
  }
  
  private void init() {
    Configuration conf = new Configuration();
    String passwdFile = conf.get("hive.server2.custom.authentication.file", "/home/hcsds/bigdata/hive-3.1.2/conf/hive-passwd");
    
    try (BufferedReader reader = new BufferedReader(new FileReader(passwdFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty() && !line.startsWith("#")) {
          String[] parts = line.split(":", 2);
          if (parts.length == 2) {
            userPasswordMap.put(parts[0], parts[1]);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read password file: " + passwdFile, e);
    }
  }
  
  @Override
  public void Authenticate(String user, String password) throws AuthenticationException {
    String storedPassword = userPasswordMap.get(user);
    if (storedPassword == null || !storedPassword.equals(password)) {
      throw new AuthenticationException("Invalid username or password");
    }
  }
}