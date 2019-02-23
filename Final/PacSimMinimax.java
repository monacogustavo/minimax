/**
 * Dr. Demetrios Glinos
 * CAP 4630 - Artificial Intelligence
 * College of Engineering and Computer Science
 * University of Central Florida
 * Spring 2019
 * @author Andrew Morse   | 3626975 | gu355280
 * @author Gustavo Monaco | 3626975 | gu355280
 */

/*

+-------------+
| Strategies  |
+-------------+
+-------------+---------------+---------------+---------------+
|      Escape | close == true |               | close == true |
+-------------+---------------+---------------+---------------+
|      Attack |               | close == true |               |
+-------------+---------------+---------------+---------------+
|       Ideal |                 Max(Path.Eval)                |
+-------------+---------------+---------------+---------------+
|       First |            Ideal == (null || Empty)           |
+-------------+---------------+---------------+---------------+
              |    Scatter    |      Fear     |     Chase     |
              +---------------+---------------+---------------+

+-----------+
| PacAction |
+-----------+
    if( close ){
        if ( PacMode == Fear )
            TakePath(Attack);
        else
            TakePath(Escape);
    }    
    else{
        Ideal = GetIdealPath();

        if( Ideal is null )
            TakePath(First);
        else
            TakePath( Ideal );
    }

+-------+
| Terms |
+-------+
    * close: distance(Pacman, ClosestGhost) <= 2;
    * Ideal: path with best utility
    * First: path to closest food pellet.
    * GetIdealPath: checks if a path is currently being followed,
                if not, then calculates Ideal path.

*/

import java.io.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import pacsim.*;

public class PacSimMinimax implements PacAction {
    // General variables
    private static boolean debug = true;

    private PacMode pacMode;

    private List<Point> path;
    private int simTime;

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



    // Method returns Escape path
    public List<Point> getEscapePath(PacCell[][] grid, PacmanCell pc){
        List<Point> escapePath = new ArrayList();
        
        // Call minimax to escape!

        return escapePath;
    }

    // Method returns Attak path
    public List<Point> getAttackPath(PacCell[][] grid, PacmanCell pc){
        List<Point> attackPath = new ArrayList();
        
        // Call minimax to attack!

        return attackPath;
    }

    // Method returns Ideal path
    public List<Point> getIdealPath(PacCell[][] grid, PacmanCell pc){
        List<Point> idealPath = new ArrayList();
        
        // Call minimax to build the ideal path

        return idealPath;
    }

    // Method returns Easy path (when there is no ideal available)
    public List<Point> getEasyPath(PacCell[][] grid, PacmanCell pc){
        List<Point> easyPath = new ArrayList();
        
        Point tgt = PacUtils.nearestFood( pc.getLoc(), grid);
        easyPath = BFSPath.getPath(grid, pc.getLoc(), tgt);

        return easyPath;
    }

    // Method returns integer distance of closest ghost
    public int closestGhost(PacCell[][] grid, PacmanCell pc) {

        int shortestDistance = Integer.MAX_VALUE;
        Point pacMan = pc.getLoc();

        // Get a list of ghost points
        List<Point> ghostLocations = PacUtils.findGhosts(grid);

        for (Point ghost : ghostLocations) {
            int tempDistance = BFSPath.getPath(grid, ghost, pacMan).size();
            if (tempDistance < shortestDistance) {
                shortestDistance = tempDistance;
            }
        }

        // Get and Set the PacMode
        GhostCell spooky = (GhostCell)PacUtils.nearestGhost(ghostLocations.get(0), grid);
        pacMode = spooky.getMode();

        return shortestDistance;
    }

    // Method returns Point to go next
    public static Point takePath(PacCell[][] grid, PacmanCell pc, List<Point> pathTaken){
        // if current path completed (or just starting out),
        // select a the nearest food using the city-block 
        // measure and generate a path to that target
        
        if( pathTaken.isEmpty() ) {
            Point tgt = PacUtils.nearestFood( pc.getLoc(), grid);
            pathTaken = BFSPath.getPath(grid, pc.getLoc(), tgt);
            
            // System.out.println("Pac-Man currently at: [ " + pc.getLoc().x
            //     + ", " + pc.getLoc().y + " ]");
            // System.out.println("Setting new target  : [ " + tgt.x
            //     + ", " + tgt.y + " ]");
        }
        
        // take the next step on the current path
        return pathTaken.remove( 0 );
    }

    public PacSimMinimax(int depth, String fname, int te, int gran, int max){
        PacSim sim = new PacSim(fname, te, gran, max);
        sim.init(this);
    }

    public static void main(String[] args){
        String fname = args[0];
        int depth = Integer.parseInt(args[1]);
        String simMessage = "";

        int te = 0;
        int gr = 0;
        int ml = 0;

        if(args.length == 5){
            te = Integer.parseInt(args[2]);
            gr = Integer.parseInt(args[3]);
            ml = Integer.parseInt(args[4]);

            simMessage += "\n\tPreliminary runs : " + te;
            simMessage += "\n\tGranularity      : " + gr;
            simMessage += "\n\tMax move limit   : " + ml;
            simMessage += "\n\nPreliminary run results : \n";
        }

        new PacSimMinimax(depth, fname, te, gr, ml);

        System.out.println("\nAdversarial Search using Minimax by Gus Monaco:");
        System.out.println("\n\tGame board   : " + fname);
        System.out.println("\tSearch depth : " + depth + "\n");
        System.out.println(simMessage);
    }
    @Override
    public void init(){
        simTime = 0;
        path = new ArrayList();
    }

    @Override
    public PacFace action( Object state ) {

        PacCell[][] grid = (PacCell[][]) state;
        PacmanCell pc = PacUtils.findPacman( grid );
        
        // make sure Pac-Man is in this game
        if( pc == null ) return null;
            
        if(PacUtils.numFood() == 0) return null;
        
        boolean close = (closestGhost(grid, pc) <= 2 ? true : false);

        if(close){
            if(debug){System.out.println("[Close = true]");}

            if(pacMode == PacMode.FEAR){
                if(debug){System.out.println("[ATTACK]");}
                path = getEasyPath(grid, pc);
            }
            else{
                if(debug){System.out.println("[ESCAPE]");}
                path = getEasyPath(grid, pc);
            }
        }
        else{
            if(debug){System.out.println("[Close = false]");}
            path = getIdealPath(grid, pc);

            if(path.isEmpty()){
                path = getEasyPath(grid, pc);
            }

        }
        Point next = takePath(grid, pc, path);
        PacFace face = PacUtils.direction( pc.getLoc(), next );
        // System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", 
        //     ++simTime, pc.getLoc().x, pc.getLoc().y, face );
        return face;
    }
}
