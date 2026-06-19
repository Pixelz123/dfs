package com.dfs.storage.engine;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;

import com.dfs.storage.domain.ChunkMetadata;

import reactor.core.scheduler.Schedulers;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class StorageEngine {
    private final String STORAGE_DIR = "./dfs-data";

    @PostConstruct
    public void init() throws Exception {
        Files.createDirectories(Paths.get(STORAGE_DIR));
    }

   public Mono<Void> writeChunk(Long chunkId, Flux<DataBuffer> dataStream){
      Path dataPath= Paths.get(STORAGE_DIR,"chk_"+chunkId+".dat");
      return DataBufferUtils.write(dataStream,dataPath,StandardOpenOption.CREATE,StandardOpenOption.WRITE)
                            .then(
                                Mono.fromRunnable(()->{
                                    try{
                                        generateMetaData(chunkId,dataPath);
                                    }catch(Exception ex){
                                        throw new RuntimeException("Bad parsing chunk");
                                     }
                                }).subscribeOn(Schedulers.boundedElastic())
                            )
                            .then();
   }

    private void generateMetaData(Long chunkId, Path dataPath) throws Exception{
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] fileBytes = Files.readAllBytes(dataPath);
         byte[] hash = digest.digest(fileBytes);
         StringBuilder hex = new StringBuilder();
         for (byte b: hash){
            hex.append(String.format("%02x",b));
         }
         ChunkMetadata metadata = new ChunkMetadata(
              chunkId,
              Files.size(dataPath),
              hex.toString(),
              System.currentTimeMillis()
         );
         metadata.saveToDisk(STORAGE_DIR);
    }

    public Mono<Void> deleteChunk(Long chunkId){
        return Mono.fromRunnable(()->{
               try{
                Files.delete(Paths.get(STORAGE_DIR,"chk_"+chunkId+".meta"));
                Files.delete(Paths.get(STORAGE_DIR,"chk_"+chunkId+".dat"));
               }catch(Exception e){
                throw new RuntimeException("Could not delete chunk ");
               }
        });
    }

}
