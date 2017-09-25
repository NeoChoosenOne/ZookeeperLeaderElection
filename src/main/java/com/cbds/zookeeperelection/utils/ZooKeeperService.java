package com.cbds.zookeeperelection.utils;

import java.io.IOException;
import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import com.cbds.zookeeperelection.nodes.ProcessNode.ProcessNodeWatcher;

public class ZooKeeperService {
	//Defined object of type Zookeeper.
	private ZooKeeper zooKeeper;
	//the constructor for ZooKeeperService
	public ZooKeeperService(final String url, final ProcessNodeWatcher processNodeWatcher) throws IOException {
		//Instance of zookeeper url, timeout, whatcher
		zooKeeper = new ZooKeeper(url, 1000, processNodeWatcher);
	}
	
	//Method to create a znode that returns the path to the current znode. 
	public String createNode(final String node, final boolean watch, final boolean ephimeral) {
		//String to return the current path of the znode who is trying to be created.
		String createdNodePath = null;
		try {
			//Object Stat that contain all the characteristics of the znode and 
			//checks if the current path that is trying to create the new znode is already created.
			final Stat nodeStat =  zooKeeper.exists(node, watch);
			//Conditional sentence that checks if the Object Stat is null or not
			//If is null means the current path of znode not exist else already exist a znode with this path.
			if(nodeStat == null) {
				//If nodeStat is null then create a new znode with the following characteristics:
				//1.-The path is the node String.
				//2.-The initial data of the znode is empty.
				//3.-The znode is created with an unsafe acl list which means all can access to the information.
				//4.-A conditional ternary operator which decides if the znode will be ephemeral_sequential or persistent.
				createdNodePath = zooKeeper.create(node, new byte[0], Ids.OPEN_ACL_UNSAFE, (ephimeral ?  CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.PERSISTENT));
			} else {
				//If the nodeStat is not null then return the same path that this method gets.
				createdNodePath = node;
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}catch(KeeperException e){
			throw new IllegalStateException(e);
		}
		//return the path of the current znode.
		return createdNodePath;
	}
	
	//Method to make a watch on a znode. 
	public boolean watchNode(final String node, final boolean watch) {
		//Create a property called watched with default value of false
		boolean watched = false;
		try {
			//Object Stat that contain all the characteristics of the znode and 
			//checks if the current path that is trying to create the new znode is already created and set a watch on this node.
			final Stat nodeStat =  zooKeeper.exists(node, watch);
			//Conditional sentence to check if nodeStat object is different from null value
			if(nodeStat != null){
				//Set watched property to true value
				watched = true;
			}
			
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}catch(KeeperException e){
			throw new IllegalStateException(e);
		}
		
		return watched;
	}
	
	//Method to get all child on the current path.
	public List<String> getChildren(final String node, final boolean watch) {
		//List to store all child on the current path
		List<String> childNodes = null;
		//Try sentence to catch InterruptedException,KeeperException
		try {
			//List to store all the child nodes on the current path.
			childNodes = zooKeeper.getChildren(node, watch);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}catch(KeeperException e){
			throw new IllegalStateException(e);
		}
		//Return a list of all the child on the path
		return childNodes;
	}
	
}
