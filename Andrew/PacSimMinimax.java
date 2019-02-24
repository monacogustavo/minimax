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
        newFace = directionAnalysis(grid, pc, inputDepth, newFace);
   

		return newFace;
	}

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	//					METHODS

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

    //					UNDER CONSTRUCTION

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // NOTE: Anything beyond this point might be foobar. Proceed with caution! 


    // Evaluate function for PacMan locations during each action() update. 
    public PacFace directionAnalysis(PacCell[][] grid, PacmanCell pc, int depth, PacFace newFace) {

    	// Set-up temp PacCell with PacMan's current location
    	int tempY = pc.getY();
    	int tempX = pc.getX();
        
        // Our array to pass in and get our minimax results
        int[] directionValues = new int[4];

        // Set-up proper indices for testing
        int northIndex = tempY - depth;
        int eastIndex = tempX + depth;
        int southIndex = tempY + depth;
        int westIndex = tempX - depth;

        PacCell north = grid[tempX][northIndex];
        int northResult = assignValues(north);
        directionValues[0] = northResult;

        PacCell east = grid[eastIndex][tempY];
        int eastResult = assignValues(east);
        directionValues[1] = eastResult;

        PacCell south = grid[tempX][southIndex];
        int southResult = assignValues(south);
        directionValues[2] = southResult;

        PacCell west = grid[westIndex][tempY];
        int westResult = assignValues(west);
        directionValues[3] = westResult;

        // ********TODO: Update results from minimac to face direction based on index. 

        // TESTING BEWARE:
        int n = directionValues.length;
        int h = log2(n);
        int i = minimax(0, 0, true, directionValues, h);

        if (i == 0)
            newFace = newFace.N;
        if (i == 1)
            newFace = newFace.E;
        if (i == 2)
            newFace = newFace.S;
        if (i == 3)
            newFace = newFace.W;
        return newFace;
    }

    // TODO: Mess with proper weights to assign values. 
    // Assigns values to each N,E,S,W direction
    public int assignValues(PacCell currentCell) {

        // Value of cell w/ wall or house
        if (currentCell instanceof WallCell || currentCell instanceof HouseCell)
            return -1000;

        // Value of a cell w/ food or power pellet
        if (currentCell instanceof FoodCell || currentCell instanceof PowerCell)
            return 10;

        // Value of cells w/ a ghost - Worst of all values
        if (currentCell instanceof GhostCell)
            return -100;

        // If empty cell
        else
            return 1;
    }

    // Utility function for minimax()
    static int log2(int n) {
        return (n == 1) ? 0 : 1 + log2(n/2);
    }

    // Since our index is the indicator for N, E, S, W return our index on base case. 
    static int minimax(int depth, int index, boolean isMax, int scores[], int h) {

        // Base case
        if (depth == h)
            return index;

        if (isMax) {
            return Math.max(minimax(depth + 1, index * 2, false, scores, h),
                minimax(depth + 1, index * 2 + 1, false, scores, h));
        }

        else 
            return Math.min(minimax(depth + 1, index * 2, true, scores, h),
                minimax(depth + 1 , index * 2 + 1, true, scores, h));
    }
}
