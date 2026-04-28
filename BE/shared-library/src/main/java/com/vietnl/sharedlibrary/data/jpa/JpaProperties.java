package com.vietnl.sharedlibrary.data.jpa;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("platform.data.jpa")
public class JpaProperties {

  private boolean enabled = true;
}
