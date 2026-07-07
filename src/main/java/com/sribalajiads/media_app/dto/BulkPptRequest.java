package com.sribalajiads.media_app.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkPptRequest {
    private List<String> codes;
    private String companyName;
}
