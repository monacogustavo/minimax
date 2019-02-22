// Andrew's Version
import java.io.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;
import pacsim.GhostCell;
import pacsim.WallCell;
import pacsim.PacMode;
import pacsim.HouseCell;
import pacsim.PowerCell;
import pacsim.FoodCell;

public class PacSimMinimax implements PacAction {

	int inputDepth;


	public PacSimMinimax(int depth, String fname, int te, int gran, int max) {

		// We'll use this for look ahead distance
		this.inputDepth = depth;

		PacSim sim = new PacSim(fname, te, gran, max);
		sim.init(this);
	}

	// Main driver
	public static void main(String[] args) {

		String fname = args[0];
		int depth = Integer.parseInt(args[1]);
        
		int te = 0;
		int gr = 0;
		int ml = 0;

		if (args.length == 5) {
			te = Integer.parseInt(args[2]);
			gr = Integer.parseInt(args[3]);
			ml = Integer.parseInt(args[4]);
		}

		new PacSimMinimax(depth, fname, te, gr, ml);

		System.out.println("\nAdversarial Search using Minimax by Andrew Morse:");

		System.out.println("\n    Game board : " + fname);
		System.out.println("    Search depth : " + depth + "\n");

		if (te > 0) {
			System.out.println("    Preliminary runs : " + te
			+ "\n    Granularity    : " + gr
			+ "\n    Max move limit : " + ml
			+ "\n\nPreliminary run results :\n");
		}
	}

	@Override
	public void init() {
		// Can be empty
	}

	// Once per frame action(s)
	@Override
	public PacFace action(Object state) {

		PacCell[][] grid = (PacCell[][]) state;
		PacFace newFace = null;

        PacmanCell pc = PacUtils.findPacman(grid);

        // Check if PacMan exists
        if (pc == null)
            return null;


        // Testing function
        evaluation(grid, pc, inputDepth);
   

		return newFace;
	}

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	//			METHODS

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    // Method returns integer distance of closest ghost
    public int closestGhost(PacCell[][] grid, PacmanCell pc) {

    	int closestDistance = Integer.MAX_VALUE;
    	Point pacMan = pc.getLoc();

        // Get a list of ghost points
        List<Point> ghostLocations = PacUtils.findGhosts(grid);

        for (Point ghost : ghostLocations) {
        	int tempDistance = BFSPath.getPath(grid, ghost, pacMan).size();
        	if (tempDistance < closestDistance) {
        		closestDistance = tempDistance;
        	}
        }

        return closestDistance;
    }
    
    // Distance of closest food pellet
    public int closestPellet(PacCell[][] grid, PacmanCell pc) {

    	// Initalize point of pacman
    	Point pacMan = pc.getLoc();

    	// Initialize point of nearest food pellet
    	Point food = PacUtils.nearestFood(pacMan, grid);

    	// Use BFS to account for walls in distance
    	int distance = BFSPath.getPath(grid, food, pacMan).size();

    	return distance;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //			UNDER CONSTRUCTION

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // NOTE: Anything beyond this point might be foobar. Proceed with caution! 


    // Evaluate function for PacMan locations during each action() update. 
    public void evaluation(PacCell[][] grid, PacmanCell pc, int depth) {

    	// Set-up temp PacCell with PacMan's current location
    	int tempY = pc.getY();
    	int tempX = pc.getX();

    	// Temps for functionality
    	int tempDepth = depth;
    	

        while (tempDepth != 0) {

            // Reset for each pass
            ArrayList<Integer> directionValue = new ArrayList<Integer>();

            // Evaluate: N,E,S,W

            // Looking north
            PacCell lookNorth = grid[tempX - tempDepth][tempY];
            // Add north value
            int north = assignValues(lookNorth);
            directionValue.add(north);
            

            // Looking east
            PacCell lookEast = grid[tempX][tempY + tempDepth];
            // Add east value
            int east = assignValues(lookEast);
            directionValue.add(east);
         
            // Looking south
            PacCell lookSouth = grid[tempX + tempDepth][tempY];
             // Add south value
            int south = assignValues(lookSouth);
            directionValue.add(south);

        
            // Looking west
            PacCell lookWest = grid[tempX][tempY - tempDepth];
            // Add west value
            int west = assignValues(lookWest);
            directionValue.add(west);


            // Update: Get gext level depth 
            tempDepth--;

            for (int value : directionValue) {
                System.out.println("This value is " + value);
            }
            System.out.println();

            // TODO: Do something or pass ArrayList into miniMax()
        }
    }

    // TODO: Assign proper weights to situation

    // Utility function for evaluation(): Assigns values to each N,E,S,W direction
    public int assignValues(PacCell lookDirection) {

        // Account for walls and house cells for the same value - You shall not pass!
        if (lookDirection instanceof WallCell || lookDirection instanceof HouseCell)
            return -1;

        // Account for food and power as the same weight
        if (lookDirection instanceof FoodCell || lookDirection instanceof PowerCell)
            return 1;

        // Stranger danger!
        if (lookDirection instanceof GhostCell)
            return -100;

        // The case where it's and empty cell. 
        else
            return 0;
    }
}
