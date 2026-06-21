package com.dfs.master_node.namespace;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DirectoryNode extends FilesystemNode {
    private  ConcurrentHashMap<String , FilesystemNode> children= new ConcurrentHashMap<>(); 

    public DirectoryNode(String name) {super(name);}

    @Override
    public FilesystemNode resolve(Queue<String> pathSegment) {
        if (pathSegment.isEmpty()) return this;
        String nextString = pathSegment.poll();
        System.out.println("getting the child of "+nextString);
        this.getLock().readLock().lock();
        try{
            FilesystemNode child = children.get(nextString);
            if (child == null){
                throw new RuntimeException("Path Not Found");
            }
            return child.resolve(pathSegment);
        }finally{
            this.getLock().readLock().unlock();
        }
    }

    public void addChildren(FilesystemNode child){ children.put(child.getName(),child);}
    public FilesystemNode removeChild(String name){ return children.remove(name);}
    public FilesystemNode getChild(String name){ return children.get(name);}
    public String getContent(){
        return children.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue().getClass().getSimpleName())
                .collect(Collectors.joining(", ", "{", "}"));
    }
     public ConcurrentHashMap<String , FilesystemNode> getChildren(){
        return children;
    }

}
