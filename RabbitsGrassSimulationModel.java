import java.awt.Color;

import java.util.ArrayList;

import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 *
 * Mechanics: 
 *   1) grass can grow on other grass - this way, its energy capacity is increased
 *      this may happen even in one "spread"
 *   2) grass can grow on a cell where rabbit is standing - what if rabbit doesn't move?
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		
		
		// Default values
		private static final int NUMBER_OF_RABBITS = 50;
		private static final int GRID_WIDTH = 20;
		private static final int GRID_HEIGHT = 20;
		private static final double BIRTH_THRESHOLD = 10.0;
		private static final int GRASS_GROWTH_RATE = 1;
		private static final int AGENT_MIN_LIFESPAN = 30;
		private static final int AGENT_MAX_LIFESPAN = 50;
		
		private int numAgents = NUMBER_OF_RABBITS;
		private int gridWidth = GRID_WIDTH;
		private int gridHeight = GRID_HEIGHT;
		private double birth_threshold = BIRTH_THRESHOLD;
		private int grassGrowthRate = GRASS_GROWTH_RATE;
		private int totalGrass = 0;
		private int agentMinLifespan = AGENT_MIN_LIFESPAN;
		private int agentMaxLifespan = AGENT_MAX_LIFESPAN;
		
		
		private Schedule schedule;
		private RabbitsGrassSimulationSpace rgSpace;
		private ArrayList agentList;
		private DisplaySurface displaySurf;
		
		private OpenSequenceGraph amountOfGrassAgentsInSpace;

		class grassInSpace implements DataSource, Sequence {

		    public Object execute() {
		      return new Double(getSValue());
		    }

		    public double getSValue() {
		      return (double)rgSpace.getTotalGrass();
		    }
		}
		  
		class agentsInSpace implements DataSource, Sequence {

			    public Object execute() {
			      return new Double(getSValue());
			    }

			    public double getSValue() {
			      return (double)agentList.size();
			    }
		}
		
		public static void main(String[] args) {
			
			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		    init.loadModel(model, "", false);
			
		}
		
		public void setup() {
			System.out.println("Running setup");
			rgSpace = null;
			agentList = new ArrayList();
			schedule = new Schedule(1);
			
			if (displaySurf != null){
			      displaySurf.dispose();
			}
			
			// Destroy dispalys
			displaySurf = null;
			
			if (amountOfGrassAgentsInSpace != null){
			      amountOfGrassAgentsInSpace.dispose();
			    }
			    amountOfGrassAgentsInSpace = null;
			
			// Create displays
			displaySurf = new DisplaySurface(this, "Rabbit Grass Simulation Model Window 1");
			amountOfGrassAgentsInSpace = new OpenSequenceGraph("Amount Of Grass In Space",this);
			
			// Register displays
			registerDisplaySurface("Rabbit Grass Model Window 1", displaySurf);
			this.registerMediaProducer("Plot", amountOfGrassAgentsInSpace);
		}

		public void begin() {
			// Done
			buildModel();
			buildSchedule();
			buildDisplay();
			
			for(int i = 0; i < agentList.size(); i++){
			      RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
			      rga.report();
			}
			
			displaySurf.display();
			amountOfGrassAgentsInSpace.display();

			
		}
		
		public void buildModel(){
			System.out.println("Running BuildModel");
			rgSpace = new RabbitsGrassSimulationSpace(gridWidth, gridHeight);
			rgSpace.spreadGrass(grassGrowthRate);
			for(int i = 0; i < numAgents; i++){
			      addNewAgent();
			}
		}
		
		public void buildSchedule(){
			System.out.println("Running BuildSchedule");
			
			class RabbitsGrassSimulationStep extends BasicAction {
			      public void execute() {
			        SimUtilities.shuffle(agentList);
			        for(int i =0; i < agentList.size(); i++){
			        	RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
			          rga.step();
			          rga.report();
			        }
			        int deadAgents = reapDeadAgents();
			        
			        /*for(int i =0; i < deadAgents; i++){
			          addNewAgent();
			        }
			        */
			        
			        displaySurf.updateDisplay();
			      }
			    }

			schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());
			
			class RabbitsGrassSimulationLiving extends BasicAction {
			      public void execute(){
			        countLivingAgents();
			      }
			}

			schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationLiving());
			
			class RabbitsGrassSimulationSpreadGrass extends BasicAction {
			      public void execute(){
			    	  rgSpace.spreadGrass(grassGrowthRate);
			      }
			}

			schedule.scheduleActionAtInterval(1, new RabbitsGrassSimulationSpreadGrass());
			
			
			class RabbitsGrassSimulationUpdateGrassInSpace extends BasicAction {
			      public void execute(){
			        amountOfGrassAgentsInSpace.step();
			      }
			}

			schedule.scheduleActionAtInterval(1, new RabbitsGrassSimulationUpdateGrassInSpace());

		}
		
		public void buildDisplay(){
			System.out.println("Running BuildDisplay");
			
			ColorMap map = new ColorMap();

			map.mapColor(0, Color.black);
		    map.mapColor(1, Color.green);

		    Value2DDisplay displayGrass = 
		        new Value2DDisplay(rgSpace.getCurrentGrassSpace(), map);
		    
		    Object2DDisplay displayAgents = new Object2DDisplay(rgSpace.getCurrentAgentSpace());
		    displayAgents.setObjectList(agentList);
		    
		    displaySurf.addDisplayableProbeable(displayGrass, "Grass");
		    displaySurf.addDisplayableProbeable(displayAgents, "Agents");
		    
		    // adding sequences to graph
		    amountOfGrassAgentsInSpace.addSequence("Grass In Space", new grassInSpace());
		    amountOfGrassAgentsInSpace.addSequence("Rabbits in Space", new agentsInSpace());
		}
		
		private void addNewAgent(){
			RabbitsGrassSimulationAgent a = 
				new RabbitsGrassSimulationAgent(agentMinLifespan, agentMaxLifespan);
			
		    agentList.add(a);
		    rgSpace.addAgent(a);
		}
	
		private int countLivingAgents(){
		    int livingAgents = 0;
		    for(int i = 0; i < agentList.size(); i++){
		      RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
		      if(rga.getEnergy() > 0) livingAgents++;
		    }
		    System.out.println("Number of living agents is: " + livingAgents);

		    return livingAgents;
		}
		
		private int reapDeadAgents(){
		    int count = 0;
		    for(int i = (agentList.size() - 1); i >= 0 ; i--){
		      RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
		      if(rga.getEnergy() < 1){
		        rgSpace.removeAgentAt(rga.getX(), rga.getY());
		        agentList.remove(i);
		        count++;
		      }
		    }
		    return count;
		  }
		
		public String[] getInitParam() {
			String[] initParams = { "numAgents", 
								    "gridWidth",
								    "gridHeight",
								    "birth threshold",
								    "grass_growth_rate"
								  };
			return initParams;
		}

		public String getName() {
			return "Rabbits & Grass Simulation";
		}

		public Schedule getSchedule() {
			return schedule;
		}
		
		// Class variables methods
		
		public int getnumAgents() {
			return numAgents;
		}
	
		public void setnumAgents(int num) {
			numAgents = num;
		}
	
		public int getGridWidth() {
			return gridWidth;
		}
	
		public void setGridWidth(int num) {
			gridWidth = num;
		}
	
		public int getGridHeight() {
			return gridHeight;
		}
	
		public void setGridHeight(int num) {
			gridHeight = num;
		}	
	
		public double getbirth_threshold() {
			return birth_threshold;
		}
	
		public void setbirth_threshold(double num) {
			birth_threshold = num;
		}
		
		public int getGrassGrowthRate() {
			return grassGrowthRate;
		}
	
		public void setGrassGrowthRate(int num) {
			grassGrowthRate = num;
		}

		public int getTotalGrass() {
			return totalGrass;
		}
	

		
}
