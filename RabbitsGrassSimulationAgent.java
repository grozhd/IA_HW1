import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	
	private int x;
	private int y;
	private int vX;
	private int vY;
	private double energy;
	private static int IDNumber = 0;
	private int ID;
	private RabbitsGrassSimulationSpace rgSpace;
	
	public RabbitsGrassSimulationAgent(int minLifespan, int maxLifespan){
	    x = -1;
	    y = -1;
	    energy = 100;
	    setVxVy();
	    IDNumber++;
	    ID = IDNumber;
	}
	
	private void setVxVy(){
	    vX = 0;
	    vY = 0;
	    while((vX == 0) && ( vY == 0)){
	      vX = (int)Math.floor(Math.random() * 3) - 1;
	      vY = (int)Math.floor(Math.random() * 3) - 1;
	    }
	  }
	
	public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace rgs){
	    rgSpace = rgs;
	}
	
	public String getID(){
	    return "A-" + ID;
	  }

	public double getEnergy(){
		    return energy;
	}
	
	public void report(){
	    System.out.println(getID() + 
	                       " at " + 
	                       x + ", " + y + 
	                       " has " + 
	                       getEnergy() + " energy");
	}
	
	public void setXY(int newX, int newY){
	    x = newX;
	    y = newY;
	}
	
	public void draw(SimGraphics G){
		    if(energy > 0)
		        G.drawFastRoundRect(Color.white);
		      else
		        G.drawFastRoundRect(Color.blue);
	 }

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void step(){
		setVxVy();
		
		int newX = x + vX;
	    int newY = y + vY;
	    
	    Object2DGrid grid = rgSpace.getCurrentAgentSpace();
	    newX = (newX + grid.getSizeX()) % grid.getSizeX();
	    newY = (newY + grid.getSizeY()) % grid.getSizeY();
	    
	    if(tryMove(newX, newY)){
	        energy += rgSpace.eatGrassAt(newX, newY);
	    }
	    else{
	        setVxVy();
	    }
	    
	    energy -= 1.0;
	}
	
	private boolean tryMove(int newX, int newY){
	    return rgSpace.moveAgentAt(x, y, newX, newY);
	}
}
