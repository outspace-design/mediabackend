package com.sribalajiads.media_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkImageUploadResult {
    private List<String> matched = new ArrayList<>();
    private List<String> unmatched = new ArrayList<>();
    private List<String> skipped = new ArrayList<>();
}
