package com.mulaflow.mulaflow.dto.notification.channels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String receiptId;
    private String subject;
    private String htmlContent;
}
