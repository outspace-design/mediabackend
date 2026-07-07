package com.sribalajiads.media_app.dto;

import lombok.Data;
import java.util.List;

@Data
public class PptRequest {
    private List<Long> mediaIds;
    private String companyName;
}
