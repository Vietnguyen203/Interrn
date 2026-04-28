package com.vietnl.sharedlibrary.observability.audit;

import com.eps.shared.core.context.HeaderContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayloadAuditLogEvent {
  private String inputData;
  private String outputData;

  private HeaderContext context;
}
