package com.tracefinder.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class ReportWriter {
    public void write(String content, Path path) throws IOException {
        Files.writeString(path, content);
    }
}