package com.zane.smapiinstaller.entity;

import lombok.Data;

@Data
public class DownloadableContent {
    private String type;
    private String name;
    private String assetPath;
    private String url;
    private String description;
    private String hash;
}
