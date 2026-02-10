package com.ihome24.ihome24.dto.response.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceListResponse {

    private List<InvoiceResponse> invoices;
    private Long totalInvoices;
}
