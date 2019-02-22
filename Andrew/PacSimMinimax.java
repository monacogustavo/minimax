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
        newFace = newFace.S;

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


    // TODO: Test this function
    // Evaluate function for PacMan locations during each action() update. 
    public void evaluation(PacCell[][] grid, PacmanCell pc, int depth) {

    	// Set-up temp PacCell with PacMan's current location
    	int tempY = pc.getY();
    	int tempX = pc.getX();

    	// Temps for functionality
    	int tempDepth = depth;
    	PacCell tempPac = grid[tempX][tempY + 1];

    	// Do a N,E,S,& W evaluation
    	if (!(tempPac instanceof WallCell)) {
    		System.out.println("Not a wall");
    	}

    	if (tempPac instanceof WallCell) {
    		System.out.println("Oh shit!!!! A WALL!!!!!");
    	}
    }
}
