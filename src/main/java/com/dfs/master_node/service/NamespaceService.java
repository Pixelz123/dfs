package com.dfs.master_node.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.dfs.master_node.namespace.DirectoryNode;
import com.dfs.master_node.namespace.FilesystemNode;

@Service
public class NamespaceService {
    private final DirectoryNode rootNamespace= new DirectoryNode("");
    public FilesystemNode resolvePath(String fullPath){
        if (fullPath==null || fullPath.equals("/")||fullPath.trim().isEmpty()){
            return rootNamespace;
        }
        Queue<String> segment= new LinkedList<>(Arrays.asList(fullPath.replaceAll("^/+", "").split("/")));
        return rootNamespace.resolve(segment);
    }
}
