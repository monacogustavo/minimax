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
+---------------------+
| Initial Strategies  |
+---------------------+
+-------------+---------------+---------------+---------------+
|      Escape | close == true |               | close == true |
+-------------+---------------+---------------+---------------+
|      Attack |               | close == true |               |
+-------------+---------------+---------------+---------------+
|       Ideal |                    Minimax                    |
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
+------------+
| Directions |
+------------+
        +---+
        | N |
    +---+---+---+
    | W | H | E |
    +---+---+---+
        | S |
        +---+
+----------------+
| Seudo-Minimax  |
+----------------+
 * minimax(pacmanLocation, ghost1Location, ghost2Location, turn) {

  if (turn == SEARCH_DEPTH) {
    am I touching a ghost? return PointScore(reallyNegative)
    add points for being close to food (value += 1/nearestFood.distance)
    subtract points for being close to ghosts (value -= ghostDistance)
    return PointScore(value)
  }
  else {
    List PossibleMaxMoves;
    for (each valid pacman direction) {
      movePacman;
      List PossibleMinMoves;
      for (each valid ghost direction for each ghost) {
        moveGhost;
        move = minimax(newPacmanLocation, newGhostLocations, turn++);
        PossibleMinMoves.add(move);
      }

      minMove = findMinMove(PossibleMinMoves);
      PossibleMaxMoves.add(minMove);
    }

    maxMove = findMaxMove(PossibleMaxMoves);
    return maxMove;
}
 */
import java.io.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import pacsim.*;

class Move{
    public Point point;
    public int score;

    public Move(Point p, int s){
        this.point = p;
        this.score = s;
    }
}

public class PacSimMinimax implements PacAction {
    // General variables
    private static final Integer TOP = Integer.MAX_VALUE;
    private static final Integer POT = Integer.MIN_VALUE;
    private static final Integer FOOD = 10;
    private static final Integer SCARE = 2;
    private static final Integer MIN_DEPTH = 4;
    private static final Integer EMPTY = 0;

    private static boolean debug = true;
    private boolean firstAction;
    private List<List<Point>> state;
    private List<Point> path;
    private int simTime;
    private static int depth;
    private int attackDepth;
    private PacMode pacMode;
    private List<Point> ghostLocations;


    // Method returns Point to go next
    public static Point nextStep(PacCell[][] grid, PacmanCell pc, List<Point> pathTaken){
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
        depth = Integer.parseInt(args[1]);
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

        System.out.println("\nAdversarial Search using Minimax by Gus Monaco and Andrew Morse:");
        System.out.println("\n\tGame board   : " + fname);
        System.out.println("\tSearch depth : " + depth + "\n");
        System.out.println(simMessage);
    }

    @Override
    public void init(){
        simTime = 0;
        path = new ArrayList();
        firstAction = true;
        state = new ArrayList();
    }

    @Override
    public PacFace action( Object OriginalState ) {

        PacCell[][] grid = (PacCell[][]) state;
        PacmanCell pc = PacUtils.findPacman( grid );
        
        // make sure Pac-Man is in this game
        if( pc == null ) return null;
            
        if(PacUtils.numFood(grid) == 0) return null;

        boolean close = (closestGhost(grid, pc) <= 3 ? true : false);

        if(close){
            // if(debug){System.out.println("[Close = true]");}

            if(pacMode == PacMode.FEAR){
                // if(debug){System.out.println("[ATTACK]");}
                path = getAttackPath(grid, pc);
            }
            else{
                // if(debug){System.out.println("[ESCAPE]");}
                path = getEscapePath(grid, pc);
            }
        }
        else{
            // if(debug){System.out.println("[Close = false]");}
            path = getIdealPath(grid, pc);

            if(path.isEmpty()){
                path = getEasyPath(grid, pc);
            }

        }
        Point next = nextStep(grid, pc, path);
        PacFace face = PacUtils.direction( pc.getLoc(), next );
        // System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", 
        //     ++simTime, pc.getLoc().x, pc.getLoc().y, face );
        return face;
    }
}
