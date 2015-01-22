import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.config.Constants;
import de.tudresden.sumo.util.SumoCommand;
import de.tudresden.ws.container.SumoStringList;
 
public class StressTestBreaking {
 
    static String sumo_bin = "sumo";
    static final String config_file = "./StressTest/ConfigTest.xml";
     
    public static void main(String[] args) {
    	int length = Integer.parseInt(args[0]); 
    	long startTime = System.nanoTime();
        //start Simulation
        SumoTraciConnection conn = new SumoTraciConnection(sumo_bin, config_file);
         
        //set some options
        conn.addOption("step-length", "1"); //timestep 100 ms
        conn.addOption("start", "1"); //timestep 100 m
        conn.addOption("quit-on-end", "1");
        try{
             
            //start TraCI
            conn.runServer();
 
            //load routes and initialize the simulation
            conn.do_timestep();
            List<SumoCommand> cmdList = null;
            int carid=0;
            for(int i=0; i<length; i++){
				//current simulation time
				int simtime = (int) conn.do_job_get(Simulation.getCurrentTime());
				cmdList = new ArrayList<SumoCommand>();
				for (int j = 0; j < 41; j++) {
					cmdList.add(Vehicle.add("veh"+carid++, "Car", "route"+j, simtime, 0, 13.8, (byte) 0));
					cmdList.add(Vehicle.add("veh"+carid++, "Car", "route"+j, simtime, 0, 13.8, (byte) 1));
					cmdList.add(Vehicle.add("veh"+carid++, "Car", "route"+j, simtime, 0, 13.8, (byte) 2));
					cmdList.add(Vehicle.add("veh"+carid++, "Car", "route"+j, simtime, 0, 13.8, (byte) 3));
				}
				
				cmdList.add(new SumoCommand(Constants.CMD_SIMSTEP2, 0));
				conn.do_jobs_set(cmdList);
				// get speed for all agents
				SumoStringList vehicleList = (SumoStringList) conn.do_job_get(Vehicle.getIDList());
				cmdList = new ArrayList<SumoCommand>(vehicleList.size());
				
				for (int j = 0; j < vehicleList.size(); j++) {
					String vehicleID = vehicleList.get(j);
					cmdList.add(Vehicle.getSpeed(vehicleID));
					cmdList.add(Vehicle.getDistance(vehicleID));
					cmdList.add(Vehicle.getLaneID(vehicleID));
				}
				
				LinkedList<Object> results = conn.do_jobs_get(cmdList);
				
				for (int j = 0; j < results.size(); j++) {
					String vehicleID = vehicleList.get(j);
					double speed = (double) results.remove();
					double distance = (double) results.remove();
					String laneID = (String) results.remove();
					//System.out.println("Agent: "+vehicleID+" speed:" + speed );
				}
            }
             
            //stop TraCI
            conn.close();
             
        }catch(Exception ex){ex.printStackTrace();}
        System.out.println((System.nanoTime()-startTime)/1e6+" ms"); 
    }
 
}