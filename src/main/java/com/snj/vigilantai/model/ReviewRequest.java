package com.snj.vigilantai.model;

import java.util.List;

public record ReviewRequest(List<FileEntry> files) {}