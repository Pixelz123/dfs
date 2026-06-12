package com.dfs.master_node.namespace;

import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//Composite Pattern .. Hehehe....
public abstract class FilesystemNode {
    protected String name;
    protected final ReentrantReadWriteLock nodeLock = new ReentrantReadWriteLock();
    public FilesystemNode(String name) {this.name=name;}
    public String getName() {return this.name;} 
    abstract FilesystemNode resolve(Queue<String> pathSegment);
    public ReentrantReadWriteLock getLock(){return this.nodeLock;}
}
