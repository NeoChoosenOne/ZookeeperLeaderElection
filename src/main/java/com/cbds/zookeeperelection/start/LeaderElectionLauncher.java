package com.cbds.zookeeperelection.start;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;
import com.cbds.zookeeperelection.nodes.ProcessNode;

//Main class to the process from zookeeper election
public class LeaderElectionLauncher {
	
	private static final Logger LOG = Logger.getLogger(LeaderElectionLauncher.class);
	
	//Main method to start the simulation of a zookeeper election in a zookeeper ensamble of one node.
	public static void main(String[] args) throws IOException {
		
		//Conditional to ask for two params as arguments.
		if(args.length < 2) {
			System.err.println("Uso: java -jar <nombre_del_jar> <id del proceso (entero)> <par zkhost:port>");
			//Finish the hole process.
			System.exit(2);
		}
		//Cast String number from console args into a number.
		final int id = Integer.valueOf(args[0]);
		//Path to the zookeeper ensamble
		final String zkURL = args[1];
		//Executor Service object to make threads of the process call ProcessNode.
		final ExecutorService service = Executors.newSingleThreadExecutor();
		//Run a a process call ProcessNode 
		final Future<?> status = service.submit(new ProcessNode(id, zkURL));
		
		try {
			//Get the status of the process running.
			status.get();
		} catch (InterruptedException e) {
			//Log out the message of the Exception 
			LOG.fatal(e.getMessage(), e);
			//Stop the service threads.
			service.shutdown();
		}catch(ExecutionException e){
			//Log out the message of the Exception 
			LOG.fatal(e.getMessage(), e);
			//Stop the service threads.
			service.shutdown();
		}
	}
}
