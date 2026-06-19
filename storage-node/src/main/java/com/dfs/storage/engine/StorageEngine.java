package com.dfs.storage.engine;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class StorageEngine {
   private final String  storageDir ="./dfs-data";
   @PostConstruct
   public void init() throws Exception{
     Files.createDirectories(Paths.get(storageDir));
   }
}
