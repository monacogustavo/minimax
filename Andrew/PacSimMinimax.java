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

		// TODO: Testing
		/*
		int ghostDistance = closestGhost(grid, pc);
		// Let's pass this initalized to evaluation function

			int testing = closestPellet(grid, pc);
		System.out.println("The closest pellet is: " + testing);
	
	
		Point testPt = pc.getLoc();
		int y = testPt.y + 1;
		System.out.println("The Y value is: " + y);
		*/
		
		return newFace;
	}

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
    
    // Utility function for miniMax()
    static int log2(int n) {
    	return (n == 1) ? 0 : 1 + log2(n/2);
    }

    // Returns the desired value from PacMan using minimax 
    static int miniMax(int depth, int index, boolean isMax, int scores[], int h) {

    	// Base case
    	if (depth == h) {
    		return scores[index];
    	}

    	if (isMax) {
    		return Math.max(miniMax(depth + 1, index * 2, false, scores, h), 
    			miniMax(depth + 1, index * 2 + 1, false, scores, h));
    	}

    	else {
    		return Math.min(miniMax(depth + 1, index * 2, true, scores, h), 
    			miniMax(depth + 1, index * 2 + 1, true, scores, h));
    	}
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
}