/*
 * University of Central Florida    
 * CAP 4630 - Fall 2018
 * Authors: <Michael Jimenez, Jake Knudson>
 */

/*	We evaluate the board positions in the following ways:
 * 
 *  1. We take the distance from the closest ghost to PacMan and take the negative inverse of that value to denote that the closer the ghost is to PacMan, 
 *     the worse the state. If the ghosts are in FEAR mode, the closer PacMan is to a ghost, the positive inverse will be taken. 
 *      
 *  2. We take the distance from the closest food dot to PacMan and take the inverse of that distance. This rewards PacMan for being closer to food.      
 *
 *  3. We take the number of food remaining on the board and multiply that number by a negative weight. This denotes that the more food present, the worse
 *     the current state is for PacMan and better for the ghosts. The less food in the current state, the better the state is for PacMan.
 *  
 *  4. We take the number of power pellets on the board and return and multiply that number by a negative weight. This suggests that the more power
 *     pellets in the current state, the worse the state is for PacMan. The power pellets are given a larger weight in comparison to the food pellets.   
 * 
 *     I use a tree data structure to store the various states throughout the game. In a single tree at (height%3 == 0) we evaluate PacMan's move and 
 *     return the max of all the children nodes. At (height%3 == 1) ^ (height%3 == 2) we are evaluating ghost nodes and return the min of their children
 *     nodes. 
 *     
 *     If PacMan or a ghost runs into a wall, return a bad value for the individual that moved into the wall. (-1000 for PacMan and 1000 for Ghosts).
 *     Positive values are bad for ghosts, and Negative values are bad for PacMan (MiniMax).
 */

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacMode;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.GhostCell;

class Node 
{  //This is our Generic Structure that holds Generic Data for each move N.S.E.W. Generic Node "pointers".
	double closestGhostDistance, closestFoodDistance, numRemainingFood, numRemainingPowerPellets; //These fields will be used for our evaluation function.
	Node north, south, east, west;
	Node(){}
}

public class PacSimMinimax implements PacAction{
			
	int depth; //Global variable holding our depth for the current run.
	String nextMove; //String representing a move: North, South, East or West.
	
	public PacSimMinimax(int depth, String fname, int te, int gran, int max)
	{	
		this.depth = depth;
		
		PacSim sim = new PacSim(fname, te, gran, max);
		sim.init(this);
	}
	
	public static void main(String[] args)
	{
		String fname = args[0];
		int depth = Integer.parseInt(args[1]);
		int te = 0;
		int gr = 0;
		int ml = 0;
		
		if(args.length == 5)
		{
			te = Integer.parseInt(args[2]);
			gr = Integer.parseInt(args[3]);
			ml = Integer.parseInt(args[4]);
		}
		
		new PacSimMinimax(depth, fname, te, gr, ml);
		
		System.out.println("\nAdversarial Search using Minimax by <Michael Jimenez, Jake Knudson>:");
		System.out.println("\n	Game board   : " + fname);
		System.out.println("	Search depth : " + depth + "\n");
		
		if(te > 0)
		{
			System.out.println("	Preliminary runs: " + te
					+ "\n	Granularity	: " + gr
					+ "\n	Max move limit	: " + ml
					+ "\n\nPreliminary run results :\n");	
		}
	}
	
	@Override
	public void init() {}
	
	@Override
	public PacFace action(Object state) {
		
		PacCell[][] grid = (PacCell[][]) state;
		PacFace newFace = null;
		
		PacCell pc = PacUtils.findPacman(grid);		
		Point Position = PacUtils.findPacman(grid).getLoc();		
		ArrayList<Point> GhostPoints = new ArrayList<Point>(PacUtils.findGhosts(grid));
		int numPower = PacUtils.numPower(grid); //This obtains the correct number of power pellets remaining.

		//All the moves that Pac-Man can make at the given position.
		Point North = new Point(Position.x, Position.y - 1);
		Point South = new Point(Position.x, Position.y + 1);
		Point East = new Point(Position.x + 1, Position.y);
		Point West = new Point(Position.x - 1, Position.y);

		Node tree = new Node(); //Make a new gameState tree. We only need one of these per state. 
		Node StateTree = CreateTree(tree, 0); //Create our Minimax tree and start at a depth of 0.
				
		//Here we are creating ghost cells for our two ghosts. Through this, we are able to see if they are in: SCATTER, CHASE or SCARED mode.  
		GhostCell bg = (GhostCell)grid[GhostPoints.get(0).x][GhostPoints.get(0).y];
		GhostCell rg = (GhostCell)grid[GhostPoints.get(1).x][GhostPoints.get(1).y];
			
		//This is how we tell what mode the ghosts are in and for how long.
		String bgMode = bg.getMode().toString();
		String rgMode = rg.getMode().toString();
		int modeCounter = 0;
		

		//System.out.println(grid[1][1] instanceof pacsim.WallCell);
		System.out.println(grid[8][5] instanceof pacsim.GhostCell);

		
		
		if(bgMode.equals(PacMode.FEAR.toString())) {
			modeCounter = bg.getModeTimer();
			//System.out.println("FEAR MODE" + "," + modeCounter);
		}
		else if(bgMode.equals(PacMode.CHASE.toString())) {
			modeCounter = bg.getModeTimer();
			//System.out.println("CHASE MODE" + "," + modeCounter);
		}
		else if(bgMode.equals(PacMode.SCATTER.toString())) {
			modeCounter = bg.getModeTimer();
			//System.out.println("SCATTER MODE" + "," + modeCounter);
		}
		
		ArrayList<Point> RemainingFood = new ArrayList<Point>(PacUtils.findFood(grid));
		
		//TODO: Our evaluation function needs to take in only the current state that the ghosts are in along with the current mode timer. 
		//Our evaluation function needs the state tree created above, ...
		double EvaluatedTree = Evaluation(StateTree, 0, Position, GhostPoints, grid, RemainingFood, numPower,bgMode.toString(),modeCounter);
	
		//Our MiniMax function where PacMan maximizes and the ghosts minimize game states. It should return the next point that PacMan should take. 
		//double maxScore = MiniMax(EvaluatedTree,0);
		
		if(nextMove == "North") 
		{
		    newFace = PacUtils.direction(pc.getLoc(), North);
		}
		else if(nextMove == "South") {
			newFace = PacUtils.direction(pc.getLoc(), South);		
		}
		else if(nextMove == "East") {
			newFace = PacUtils.direction(pc.getLoc(), East);
		}
		else if(nextMove == "West") {
			newFace = PacUtils.direction(pc.getLoc(), West);
		}		
		return newFace;
	}
	
	public double Min(double a,double b,double c,double d) {
		
		double small, smaller;
		
		if(a < b)
			small = a;
		else
			small = b;
		
		if(c < d)
			smaller = c;
		else 
			smaller = d;
		
		return (small < smaller) ? small : smaller;
	}
	
	//Method that returns the max value of all of the children's nodes.
	public double Max(double a, double b, double c, double d) {
		
		double big, bigger;
		
		if(a > b)
			big = a;
		else 
			big = b;
		
		if(c > d)
			bigger = c;
		else 
			bigger = d;
		
		return (big > bigger) ? big : bigger;
	}
	
	//Returns the closest ghost to PacMan in a current state.
	public double closestGhostDistance(ArrayList<Point> ghostPositions, Point pacLocation, PacCell[][] grid, String mode){
				
		int modeFactor = 1;
		
		if(mode == "FEAR")
			modeFactor = -1;
		
//		PacCell p  = grid[pacLocation.x][pacLocation.y];
//		PacCell bg = grid[ghostPositions.get(0).x][ghostPositions.get(0).y]; 
//		PacCell rg = grid[ghostPositions.get(1).x][ghostPositions.get(1).y]; 
//		
//		//Before computing the BFSPath, we need to check if any of our three players are in a wall, ghost house or illegal position.
//		if(p instanceof pacsim.WallCell || p instanceof pacsim.HouseCell || p instanceof pacsim.GhostCell)
//			return -100; //Return a penalty if PacMan is in an illegal or bad spot.
//		else if(bg instanceof pacsim.WallCell)
//			return -100; //Return a penalty if the Blue Ghost is in an illegal or bad spot.
//		else if(rg instanceof pacsim.WallCell)
//			return -100; //Return a penalty if the Red Ghost is in an illegal or bad spot.
//		
		//Only ever two ghosts on the map, so return the minimum distance of the 2 ghosts.
		double ghostDistance = BFSPath.getPath(grid,ghostPositions.get(0),pacLocation).size();
		double ghostDistance2 = BFSPath.getPath(grid,ghostPositions.get(1),pacLocation).size();
		
		//System.out.println("ghostPositions = " + ghostPositions + " PacLocation = " + pacLocation);
		//System.out.println("ghostDistance = " + ghostDistance + " ghostDistance2 = " + ghostDistance2);
		
		//If a ghost and PacMan are currently on the same cell, return the largest possible score for the ghosts.
		if(ghostPositions.get(0).getLocation() == pacLocation.getLocation() || ghostPositions.get(1).getLocation() == pacLocation.getLocation()) 
			return -2 * modeFactor;
		
//		if(ghostDistance <= 0 || ghostDistance2 <= 0) //If PacMan is eaten by a ghost or a ghost is in a wall, return a very negative value.
//			return -100;
//		
		
		//A negative inverse is taken to show that the closer the move the worse it is for PacMan.
		return (ghostDistance < ghostDistance2) ? (modeFactor * (-1/ghostDistance)) : (modeFactor * (-1/ghostDistance2));
	}
	
	//TODO: Make PacMan avoid power pellets if he is in FEAR MODE.
	//This function will evaluate how likely or unlikely PacMan is to choose a state in the state tree. 
	public double Evaluation(Node tree, int levelsDeep, Point pacLocation, ArrayList<Point> GhostDistances, PacCell[][] grid, ArrayList<Point> RemainingFood,
			 int RemainingPower, String mode, int modeTimer)
	{
		//System.out.println("mode = " + mode + " modeTimer = " + modeTimer);
		double northVal, southVal, eastVal, westVal, maxVal, minVal;
		int numRemainingPower = RemainingPower;
		
		//TODO: Implement the scared functionality into the game.
		//If PacMan lands on a power pellet, he gains 20 moves of immunity from the ghosts. 
		if(grid[pacLocation.x][pacLocation.y] instanceof pacsim.PowerCell) 
		{
			mode = "FEAR";
			modeTimer = 20; //If we eat a power pellet, reset the FEAR timer to 20.
		}
		
		//If PacMan moves into a wall. Punish him with a bad score and return to avoid illegal/excessive computation.
		if (grid[pacLocation.x][pacLocation.y] instanceof pacsim.WallCell || grid[pacLocation.x][pacLocation.y] instanceof pacsim.HouseCell)
		{
			tree.closestFoodDistance = -1000;
			return -1000;
		}
		
		if (grid[pacLocation.x][pacLocation.y] instanceof pacsim.GhostCell)
		{
			if(mode == "FEAR" && modeTimer > 0) 
			{
				tree.closestFoodDistance = 800;
				return 800;
			}
			else
			{
				tree.closestFoodDistance = -800;
				return -800;	
			}
		}
		// If PacMan is currently in FEAR mode, avoid eating another power pellet until FEAR mode ends.
		if(grid[pacLocation.x][pacLocation.y] instanceof pacsim.PowerCell && (mode == "FEAR" && modeTimer > 0))
		{
			tree.closestFoodDistance = -50;
			return -50;
		}
		else if(grid[pacLocation.x][pacLocation.y] instanceof pacsim.PowerCell)
		{
			tree.closestFoodDistance = 50;
			return 50;
		}
		
		//If one of the ghosts moves into a wall, punish him with a bad score and return to avoid illegal/excessive computation.
		if (grid[GhostDistances.get(0).x][GhostDistances.get(0).y] instanceof pacsim.WallCell || grid[GhostDistances.get(1).x][GhostDistances.get(1).y] instanceof pacsim.WallCell)
		{
			tree.closestFoodDistance = 1000;
			return 1000;
		}
		//If the ghosts run into PacMan while they are in scared mode, punish them.
		if ((grid[GhostDistances.get(0).x][GhostDistances.get(0).y] instanceof pacsim.PacmanCell && (mode == "FEAR" && modeTimer > 0)) ||
			 grid[GhostDistances.get(1).x][GhostDistances.get(1).y] instanceof pacsim.PacmanCell && (mode == "FEAR" && modeTimer > 0))
		{
			tree.closestFoodDistance = 800;
			return 800;
		}
					
		//These are the points and the array lists where we will hold our moves throughout our recursive descent. 
		Point PacNorth = pacLocation;
		Point PacSouth = pacLocation;
		Point PacEast = pacLocation;
		Point PacWest = pacLocation;
		ArrayList<Point> ghostNorth = new ArrayList<Point>(GhostDistances);
		ArrayList<Point> ghostSouth = new ArrayList<Point>(GhostDistances);
		ArrayList<Point> ghostEast = new ArrayList<Point>(GhostDistances);
		ArrayList<Point> ghostWest = new ArrayList<Point>(GhostDistances);
		ArrayList<Point> NorthRemainingFood = new ArrayList<Point>(RemainingFood);
		ArrayList<Point> SouthRemainingFood = new ArrayList<Point>(RemainingFood);
		ArrayList<Point> EastRemainingFood = new ArrayList<Point>(RemainingFood);
		ArrayList<Point> WestRemainingFood = new ArrayList<Point>(RemainingFood);

		//BaseCase, this is our terminal node. 
		if (levelsDeep == depth * 3)
		{
//			System.out.println("Terminal Node reached!!!!!!!!!!");
			
			tree.closestGhostDistance = closestGhostDistance(GhostDistances,pacLocation,grid, mode);
			//System.out.println(tree.closestGhostDistance);
			
			 //Find the closest remaining food at the current game state.
			 tree.closestFoodDistance = ClosestRemainingFoodPosition(PacNorth,grid,RemainingFood);
			 //System.out.println("tree.closestFoodDistance = " + tree.closestFoodDistance);
			 
			//This is the number of remaining food in each state. The more food, the worse the state is for PacMan hence the negative weight.
			 tree.numRemainingFood = (RemainingFood.size() * -2); 
			 //System.out.println(tree.numRemainingFood);
			
			//Checks the current state to see if PacMan is on a PowerPellet.
			if(grid[pacLocation.x][pacLocation.y] instanceof pacsim.PowerCell)
			{
				modeTimer = 20;
				mode = "FEAR";
				numRemainingPower -= 1;
				tree.numRemainingPowerPellets = -20 * numRemainingPower;
			}
			else
				tree.numRemainingPowerPellets = -20 * numRemainingPower;
			 
			
			//Return the sum of all the evaluation fields here.
			return (tree.closestFoodDistance + tree.closestGhostDistance + tree.numRemainingFood + tree.numRemainingPowerPellets);
		}

		//All the moves that PacMan can make at the given position. Only move PacMan every 3 levels in the tree!!!
		if(levelsDeep%3 == 0)
		{
			if(modeTimer > 0)
				modeTimer -= 1;
			
			if(modeTimer == 0 && mode == "CHASE")
			{
				mode = "Scatter";
				modeTimer = 7;
			}
			else if(modeTimer == 0 && mode == "SCATTER")
			{
				mode = "CHASE";
				modeTimer = 20;
			}
			else if (modeTimer == 0)
			{
				mode = "CHASE or SCATTER MODE";
			}
				
			
	//		System.out.println("PacMan's Turn to move!!!!!!!!");
			 PacNorth = new Point(pacLocation.x,pacLocation.y - 1);
			 PacSouth = new Point(pacLocation.x,pacLocation.y + 1);
			 PacEast = new Point(pacLocation.x + 1,pacLocation.y);
			 PacWest = new Point(pacLocation.x - 1,pacLocation.y);
			 
			 //If PacMan moves either NSEW and lands on a food dot, remove it from the respective list.
			 if(grid[PacNorth.x][PacNorth.y] instanceof pacsim.FoodCell)
			 {
				 
				 for(int j = 0; j < NorthRemainingFood.size(); j++)
				 {
					 
					 if(NorthRemainingFood.get(j).equals(PacNorth))
					 {
						 NorthRemainingFood.remove(j);
						 break;
					 }
				 }
			 }
			 
			 if(grid[PacSouth.x][PacSouth.y] instanceof pacsim.FoodCell)
			 {
				 
				 for(int j = 0; j < SouthRemainingFood.size(); j++)
				 {
					 if(SouthRemainingFood.get(j).equals(PacSouth))
					 {
						 SouthRemainingFood.remove(j);
						 break;
					 }
				 }
			 }
			 
			 if(grid[PacEast.x][PacEast.y] instanceof pacsim.FoodCell)
			 {
				 for(int j = 0; j < EastRemainingFood.size(); j++)
				 {
					 if(EastRemainingFood.get(j).equals(PacEast))
					 {
						 EastRemainingFood.remove(j);
						 break;
					 }
				 }
			 }
			 
			 if(grid[PacWest.x][PacWest.y] instanceof pacsim.FoodCell)
			 {
				 
				 for(int j = 0; j < WestRemainingFood.size(); j++)
				 {
					 if(WestRemainingFood.get(j).equals(PacWest))
					 {
						 WestRemainingFood.remove(j);
						 break;
					 }
				 }
			 }			 
		}
		//All the moves that the red ghost can make at the given position. Only move the red ghost every 3 levels in the tree!!
		else if(levelsDeep%3 == 1)
		{
		//	System.out.println("Red Ghosts turn to move!");
			//Position 1 in the array is the red ghost.
			ghostNorth.set(1, new Point(ghostNorth.get(1).x, ghostNorth.get(1).y - 1));
			ghostSouth.set(1, new Point(ghostSouth.get(1).x, ghostSouth.get(1).y + 1));
			ghostEast.set(1, new Point(ghostEast.get(1).x + 1, ghostEast.get(1).y));
			ghostWest.set(1, new Point(ghostWest.get(1).x - 1, ghostWest.get(1).y));
			
		}
		//All the moves that the blue ghost can make at the given position. Only move the blue ghost every 3 levels in the tree!!
		else if(levelsDeep%3 == 2)
		{
		//System.out.println("Blue ghosts turn to move!!!");
			//Position 0 in the array is the blue ghost.
			ghostNorth.set(0, new Point(ghostNorth.get(0).x, ghostNorth.get(0).y - 1));
			ghostSouth.set(0, new Point(ghostSouth.get(0).x, ghostSouth.get(0).y + 1));
			ghostEast.set(0, new Point(ghostEast.get(0).x + 1, ghostEast.get(0).y));
			ghostWest.set(0, new Point(ghostWest.get(0).x - 1, ghostWest.get(0).y));	
		}
		
		//Traverse all the children nodes for ever root node.
		northVal = Evaluation(tree.north,levelsDeep + 1,PacNorth,ghostNorth,grid,NorthRemainingFood,numRemainingPower,mode,modeTimer);
		southVal = Evaluation(tree.south,levelsDeep + 1,PacSouth,ghostSouth,grid,SouthRemainingFood,numRemainingPower,mode, modeTimer);
		eastVal  = Evaluation(tree.east,levelsDeep + 1,PacEast,ghostEast,grid,EastRemainingFood,numRemainingPower,mode, modeTimer);
		westVal  = Evaluation(tree.west,levelsDeep + 1,PacWest,ghostWest,grid,WestRemainingFood,numRemainingPower,mode, modeTimer);
		
		
		//If we are looking at a PacMan node, take the max of the children nodes.
		if(levelsDeep % 3 == 0) {
			//Find the best move that PacMan can take.
			maxVal = Max(northVal, southVal, eastVal, westVal);
			
			if(maxVal == northVal) {
				nextMove = "North";
			}	
			else if(maxVal == southVal) {
				nextMove = "South";
			}
			else if(maxVal == eastVal) {
				nextMove = "East";
			}
			else if(maxVal == westVal) {
				nextMove = "West";
			}
		//	System.out.println("northval = " + northVal + "southVal = " + southVal +  "eastVal = " + eastVal + "westVal = " + westVal );
		//	System.out.println("MAX = " + maxVal);
			
			return maxVal;
		}
		//If we are looking at one of the ghost nodes, take the min of the children nodes.
		else
		{
			//Find the best move that the ghost's can take.
			minVal = Min(northVal, southVal, eastVal, westVal);
		
			if(minVal == northVal) {
				nextMove = "North";
			}	
			else if(minVal == southVal) {
				nextMove = "South";
			}
			else if(minVal == eastVal) {
				nextMove = "East";
			}
			else if(minVal == westVal) {
				nextMove = "West";
			}
			//System.out.println("northval = " + northVal + "southVal = " + southVal +  "eastVal = " + eastVal + "westVal = " + westVal );
			//System.out.println("Min = " + minVal);
			return minVal;
		}
	 }
	
	//This function will return the value of the closest food to PacMan in a given state. Closer food yields a higher score.
	public double ClosestRemainingFoodPosition(Point pacLocation, PacCell[][] grid, ArrayList<Point> RemainingFood)
	{	
//		System.out.println("PacMan location = " + pacLocation);
//		System.out.println("RemainingFood = " + RemainingFood);
		
		Integer MIN = Integer.MAX_VALUE; //This is used to find the min distance in our Remaining Food Array. 
		
		//Loop through our Remaining Food Array and find the closest Food to PacMan.
		for(int i = 0; i < RemainingFood.size();i++) 
		{
			if(BFSPath.getPath(grid, pacLocation,RemainingFood.get(i)).size() < MIN) {
				MIN = BFSPath.getPath(grid, pacLocation,RemainingFood.get(i)).size();
			}
						
			if(MIN == 1) //If we have a food dot right next to us, then return because this is the closest we can be to a food dot.
			{
				return 1/(double)MIN;
			}
		}

		//System.out.println("MIN = " + (double)MIN);
//		System.out.println("1/MIN = " + 1/(double)MIN);
		
		return 1/(double)MIN;
	}
	
	//This is our recursive method that will construct our Minimax tree. 
	public Node CreateTree(Node tree, int levelsDeep)
	{
		//Need our base case
		if (tree == null)
			tree = new Node();
		
		//We need to return if we reach a depth too deep. (depth*3 in the tree = depth of the game)
		if(levelsDeep == depth * 3)
			return tree;
			
		//Create all the children nodes for ever root node.
		tree.north = CreateTree(tree.north,levelsDeep + 1);
		tree.south = CreateTree(tree.south,levelsDeep + 1);
		tree.east  = CreateTree(tree.east,levelsDeep + 1);
		tree.west  = CreateTree(tree.west,levelsDeep + 1);
		
		return tree; //Return the root of our Minimax tree.
	}
}