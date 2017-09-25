package com.cbds.zookeeperelection.nodes;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.cbds.zookeeperelection.utils.ZooKeeperService;


public class ProcessNode implements Runnable{
	//Object to print a log on console
	private static final Logger LOG = Logger.getLogger(ProcessNode.class);
	//root node path
	private static final String LEADER_ELECTION_ROOT_NODE = "/election";
	//sequential znodes initial path
	private static final String PROCESS_NODE_PREFIX = "/p_";
	//Property to store the sequential znode id.
	private final int id;
	//Zookeeper Service Object
	private final ZooKeeperService zooKeeperService;
	//String definition to store the value of the process node and watched node.
	private String processNodePath;
	private String watchedNodePath;
	
	//Construct of the ProcessNode Class
	public ProcessNode(final int id, final String zkURL) throws IOException {
		//Set value of id of process to the property id.
		this.id = id;
		//Instance of the ZookeeperService 
		zooKeeperService = new ZooKeeperService(zkURL, new ProcessNodeWatcher());
	}
	
	private void attemptForLeaderPosition() {
		
		//List for all znodes inside the root node of /election
		final List<String> childNodePaths = zooKeeperService.getChildren(LEADER_ELECTION_ROOT_NODE, false);
		//Sort the childNodePaths list in alphabetic order.
		Collections.sort(childNodePaths);
		//Get the index position at the list of the current znode name.  
		int index = childNodePaths.indexOf(processNodePath.substring(processNodePath.lastIndexOf('/') + 1));
		//if the index is the first position make it leader
		if(index == 0) {
			//Conditional log to check if object Log is enabled
			if(LOG.isInfoEnabled()) {
				LOG.info("[Proceso: " + id + "] Yo soy el lider!");
			}
		} else {
			//else get the name of the previous znode.
			final String watchedNodeShortPath = childNodePaths.get(index - 1);
			//construct the path to the watch of a znode
			watchedNodePath = LEADER_ELECTION_ROOT_NODE + "/" + watchedNodeShortPath;
			//Conditional log to check if object Log is enabled
			if(LOG.isInfoEnabled()){
				LOG.info("[Proceso: " + id + "] - Mandando un watch en el nodo con path: " + watchedNodePath);
			}
			//set a watch to the znode watchedNodePath
			zooKeeperService.watchNode(watchedNodePath, true);
		}
	}

	//Run the one thread. 
	public void run() {
		//Conditional log to check if object Log is enabled
		if(LOG.isInfoEnabled()) {
			LOG.info("Process con id: " + id + " se ha iniciado!");
		}
		//Create a node with path LEADER_ELECTION_ROOT_NODE the default type 
		final String rootNodePath = zooKeeperService.createNode(LEADER_ELECTION_ROOT_NODE, false, false);
		if(rootNodePath == null) {
			throw new IllegalStateException("No se puede crear/acceder al leader election root node con path: " + LEADER_ELECTION_ROOT_NODE);
		}
		//Create a node that will be child of rootNodePath this child are ephemeral nodes with initial works p_ 
		processNodePath = zooKeeperService.createNode(rootNodePath + PROCESS_NODE_PREFIX, false, true);
		//Conditional statement that checks if the path of the ephemeral nodes are not null
		if(processNodePath == null) {
			throw new IllegalStateException("No se puede crear/acceder al proceso del nodo con el path: " + LEADER_ELECTION_ROOT_NODE);
		}
		//Conditional log to check if object Log is enabled
		if(LOG.isDebugEnabled()) {
			LOG.debug("[Proceso: " + id + "] Process node creado con path: " + processNodePath);
		}
		//Looking for the leader election according to the position of sequential znodes.
		attemptForLeaderPosition();
	}
	
	//Public class to define a watcher  
	public class ProcessNodeWatcher implements Watcher{
		//Method from Watcher class
		public void process(WatchedEvent event) {
			//Conditional log to check if object Log is enabled
			if(LOG.isDebugEnabled()) {
				LOG.debug("[Process: " + id + "] Evento recivido: " + event);
			}
			//Object of tipe EventType that gets the current type of a event on a znode.
			final EventType eventType = event.getType();
			//Conditional Statement that checks if the event is of the type Deleted
			if(EventType.NodeDeleted.equals(eventType)) {
				//check if the path of the event is the same on the watchedNodePath String
				if(event.getPath().equalsIgnoreCase(watchedNodePath)) {
					//Start the leader position search again.
					attemptForLeaderPosition();
				}
			}	
		}	
	}
}
