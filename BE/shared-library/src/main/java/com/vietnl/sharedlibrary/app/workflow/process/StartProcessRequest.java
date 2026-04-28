package com.vietnl.sharedlibrary.app.workflow.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StartProcessRequest {
  private String processDefinitionKey;
  private String businessKey;
  private Map<String, Object> variables;
}
