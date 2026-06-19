package com.dfs.storage.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import com.dfs.storage.engine.StorageEngine;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {
    private final StorageEngine storageEngine;

    public StorageController(StorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }

    @PostMapping(value= "/chunk/{chunkId}", consumes = "application/octet-stream")
    public Mono<ResponseEntity<String>> uploadChunk(
       @PathVariable Long chunkId,
       @RequestBody Flux<DataBuffer> incomingStream
    ){
        Mono<Void> writeTask = storageEngine.writeChunk(chunkId, incomingStream);
        return writeTask.then(Mono.just(ResponseEntity.ok("ok")));
    }
}
