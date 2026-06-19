package com.dfs.storage.domain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

public record ChunkMetadata(
    Long chunkId, 
    Long sizeInbytes,
    String checksum,
    Long generationStamp
) {
    private static final ObjectMapper mapper = new ObjectMapper();
    public void saveToDisk(String directory) throws Exception{
        Path metaPath = Paths.get(directory,"chk_"+chunkId+".meta");
        Files.write(metaPath,mapper.writeValueAsBytes(this));
    }
    public static ChunkMetadata loadFromDisk(Path metaPath) throws Exception {
        return mapper.readValue(Files.readAllBytes(metaPath),ChunkMetadata.class);
    }
}
