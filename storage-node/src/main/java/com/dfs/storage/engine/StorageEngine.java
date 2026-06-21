package com.dfs.storage.engine;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class StorageEngine {
    private final String STORAGE_DIR = "./dfs-data";

    @PostConstruct
    public void init() throws Exception {
        Files.createDirectories(Paths.get(STORAGE_DIR));
    }

    public void writeData(Long chunkId, ReadableByteChannel tcpByteChannel, long payloadSize) {
        Path dataPath = Paths.get(STORAGE_DIR, "blk_" + chunkId + ".dat");
        try {
            FileChannel diskChannel = FileChannel.open(dataPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            long bytesTransferred = 0;

            while (bytesTransferred < payloadSize) {
                long transferredByte = diskChannel.transferFrom(
                        tcpByteChannel,
                        bytesTransferred,
                        payloadSize - bytesTransferred);
                if (transferredByte <= 0)
                    throw new RuntimeException("TCP Transafer error !!!");

                bytesTransferred += transferredByte;

            }
        } catch (Exception ex) {
            throw new RuntimeException("TCP Socket failure !!!");
        }
    }

    public void deleteChunk(Long chunkId) throws Exception {
        Files.deleteIfExists(Paths.get("blk_" + chunkId + ".dat"));
        Files.deleteIfExists(Paths.get("blk_" + chunkId + ".meta"));
    }
}
