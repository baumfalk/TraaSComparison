import java.io.IOException;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.ws.container.SumoStringList;
 
public class Test {
 
    static String sumo_bin = "sumo";
    static final String config_file = "./scenario/config.sumo.cfg";
     
    public static void main(String[] args) throws Exception {
        
    	int length = Integer.parseInt(args[0]);
    	long startTime = System.nanoTime();
        //start Simulation
        SumoTraciConnection conn = new SumoTraciConnection(sumo_bin, config_file);
         
        //set some options
        conn.addOption("step-length", "1"); //timestep 100 ms
        conn.addOption("start", "1"); //timestep 100 m
        conn.addOption("quit-on-end", "1");
        //start TraCI
        conn.runServer();
 
            //load routes and initialize the simulation
        conn.do_timestep();
         
        for(int i=0; i<length; i++){
			//current simulation time
			int simtime = (int) conn.do_job_get(Simulation.getCurrentTime());
			conn.do_job_set(Vehicle.add("veh"+i, "car", "s1", simtime, 0, 13.8, (byte) 1));
			conn.do_timestep();
			//System.out.println(i);
			// get speed for all agents
			SumoStringList vehicleList = (SumoStringList) conn.do_job_get(Vehicle.getIDList());
			for (int j = 0; j < vehicleList.size(); j++) {
				
				String vehicleID = vehicleList.get(j);
				double speed = (double) conn.do_job_get(Vehicle.getSpeed(vehicleID));
				double distance = (double) conn.do_job_get(Vehicle.getDistance(vehicleID));
				String laneID = (String) conn.do_job_get(Vehicle.getLaneID(vehicleID));

				//System.out.println("Agent: "+vehicleID+" speed:" + speed );
			}
        }
         
        //stop TraCI
        conn.close();
             
        System.out.println((System.nanoTime()-startTime)/1e6+" ms"); 
    }
 
}