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
minimax(pacmanLocation, ghost1Location, ghost2Location, turn) {

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
    private static final Integer GHOST = -100;
    private static final Integer SCARE = 2;
    private static final Integer MIN_DEPTH = 4;
    private static final Integer EMPTY = 0;

    private static boolean debug = true;
    private static int depth;
    private PacMode pacMode;
    private List<Point> ghostLocations;

    private List<Point> path;
    private int simTime;

 
    public Point getPaco(PacmanCell pc) {

        int x  = pc.getX();
        int y = pc.getY();

        Point pacMan = new Point(x,y);

        return pacMan;
    }

    public Point getLoka(PacCell[][] grid) {
        for (Point ghost : ghostLocations) {
            int x = ghost.x;
            int y = ghost.y;

            if (grid[x][y] instanceof BlinkyCell)
                return ghost;
        } 
        return null;
    }

    public Point getInka(PacCell[][] grid) {
        for (Point ghost : ghostLocations) {
            int x = ghost.x;
            int y = ghost.y;
            if (grid[x][y] instanceof BlinkyCell)
                return ghost;
        } 
        return null;
    }
    // Assigns values to each N,E,S,W direction
    public int assignValues(PacCell currentCell, PacCell[][] grid, boolean isGhost) {

        try {
            // Value of cell w/ wall or house
            if (currentCell instanceof WallCell || currentCell instanceof HouseCell)
                return GHOST;
    
            // Value of cells w/ a ghost
            if (currentCell instanceof GhostCell || isGhost)
                return GHOST;
    
            // Value of a cell w/ food or power pellet
            if (currentCell instanceof FoodCell || currentCell instanceof PowerCell)
                return FOOD;
    
            // If nothing specific, then this is empty
            else{
                int score = FOOD;
                int distanceToFood = PacUtils.manhattanDistance(currentCell.getLoc(),
                    PacUtils.nearestFood(currentCell.getLoc(), grid));
                score = score/distanceToFood;
    
                int distanceToGhost = PacUtils.manhattanDistance(currentCell.getLoc(),
                    PacUtils.nearestGhost(currentCell.getLoc(), grid).getLoc());
    
                score = score + GHOST/distanceToGhost;
    
                return score;
            }            
            
        } catch (Exception e) {
            return 0;
        }
    }

    public PacCell getPacCell(PacCell[][] grid, Point p){
        PacCell output = null;
        for(PacCell[] row : grid){
            for(PacCell col : row){
                if(p.getX() == col.getX() && p.getY() == col.getY()){
                    return col;
                }
            }
        }
        return output;
    }

    // Evaluate function for PacMan locations during each action() update. 
    // public Point directionAnalysis(PacCell[][] grid, PacmanCell pc, int depth) {

    //     // Let the index 0...3 represent N, E, S, W ordering.
    //     int[]scores = new int[4];

    //     // Set-up temp PacCell with PacMan's current location
    //     int tempY = pc.getY();
    //     int tempX = pc.getX();
    
    //     // Set-up proper indices for testing
    //     int northIndex = tempY - depth;
    //     int eastIndex = tempX + depth;
    //     int southIndex = tempY + depth;
    //     int westIndex = tempX - depth;

    //     PacCell north = grid[tempX][northIndex];
    //     int northResult = assignValues(north);
    //     scores[0] = northResult;

    //     PacCell east = grid[eastIndex][tempY];
    //     int eastResult = assignValues(east);
    //     scores[1] = eastResult;

    //     PacCell south = grid[tempX][southIndex];
    //     int southResult = assignValues(south);
    //     scores[2] = southResult;

    //     PacCell west = grid[westIndex][tempY];
    //     int westResult = assignValues(west);
    //     scores[3] = westResult;

    //     // Set-up for minimax
    //     int n = scores.length;
    //     int h = log2(n);

    //     int miniMaxResult = miniMax(0, 0, true, scores, h);
    //     int directionResult = 0;

    //     // Find if it's N,E,S,W through the matching index value
    //     for (int i = 0; i < 4; i++) {
    //         if (scores[i] == miniMaxResult) {
    //             directionResult = i;
    //         }
    //     }
        
    //     // Our resulting point we should pick
    //     Point nextStep = null;

    //     // Going north
    //     if (directionResult == 0) {
    //         nextStep = new Point(tempX,northIndex);
    //     }
    //     // Going east
    //     else if (directionResult == 1) {
    //         nextStep = new Point(eastIndex,tempY);
    //     } 
    //     // Going south
    //     else if (directionResult == 2) {
    //         nextStep = new Point(tempX, southIndex);
    //     }
    //     // Going west
    //     else
    //         nextStep = new Point(westIndex,tempY);
    //     // Point taken using miniMax()
    //     return nextStep;
    // }

    // Utility function for miniMax()
    static int log2(int n) {
        return (n == 1) ? 0 : 1 + log2(n/2);
    }

    // method to confirm if this is a valid PacCell
    public boolean validPacCell(PacCell[][] grid, Point p){
        boolean output = false;
        if(!(grid[p.x][p.y] instanceof WallCell || grid[p.x][p.y] instanceof HouseCell)){
            output = true;
        }
        return output;
    }

    // Get the immediate steps 
    public List<Point> immediateRadius(PacCell[][] grid, Point vertex){
        // Get PacMan's current location
        int homeY = vertex.y;//ps.point.y;
        int homeX = vertex.x;//ps.point.x;

        // Set-up proper indices
        List<Point> cardinal = new ArrayList<Point>();
        Point north = new Point(homeX, homeY - 1);
        if(validPacCell(grid, north))
            cardinal.add(north); 

        Point south = new Point(homeX, homeY + 1);
        if(validPacCell(grid, south))
            cardinal.add(south); 

        Point east = new Point(homeX + 1, homeY);
        if(validPacCell(grid, east))
            cardinal.add(east); 


        Point west = new Point(homeX - 1, homeY);
        if(validPacCell(grid, west))
            cardinal.add(west); 

        return cardinal;
    }

    public Move findMaxMove(List<Move> moves){
        int max = POT;
        Move output = null;
        for(Move m : moves){
            if(m.score > max){
                max = m.score;
                output = m;
            }
        }
        return output;
    }

    public Move findMinMove(List<Move> moves){
        int min = TOP;
        Move output = null;
        for(Move m : moves){
            if(m.score < min){
                min = m.score;
                output = m;
            }
        }
        return output;
    }

    // Method returns the desired value from PacMan using minimax 
    public Move minimax(PacCell[][] grid, Point paco, Point loka, Point inka, int turn, boolean isGhost){
        if(turn == depth){
            return new Move(
                paco,
                assignValues(getPacCell(grid, paco), grid, isGhost)
            );            
        }
        else{
            turn++;
            List<Move> possibleMaxMoves = new ArrayList<Move>();
            for(Point p : immediateRadius(grid, paco)){
                List<Move> possibleMinMoves = new ArrayList<Move>(); 
                for(Point gl : immediateRadius(grid, loka)){
                    for(Point gi : immediateRadius(grid, inka)){
                        possibleMinMoves.add(minimax(grid, paco, gl, gi, turn, true));
                    }
                    
                }
                possibleMaxMoves.add(findMinMove(possibleMinMoves));
            }
            return findMaxMove(possibleMaxMoves);
        }       

    }
    
    // Method returns Escape path
    public List<Point> getEscapePath(PacCell[][] grid, PacmanCell pc){
        List<Point> escapePath = new ArrayList();
        
        // Call minimax to escape!
        Point tgt = PacUtils.neighbor(
            PacUtils.oppositeFace(
                PacUtils.direction(
                    pc.getLoc(), PacUtils.nearestGhost(pc.getLoc(), grid).getLoc()
                )
            ), pc.getLoc(), grid).getLoc();
        escapePath = BFSPath.getPath(grid, pc.getLoc(), tgt);

        return escapePath;
    }

    // Method returns Attak path
    public List<Point> getAttackPath(PacCell[][] grid, PacmanCell pc){
        List<Point> attackPath = new ArrayList();
        // Call minimax to attack!
        PacCell cell = PacUtils.nearestGhost(pc.getLoc(), grid);
        // If the pursuit is going to the house cell, don't
        if(!(cell instanceof HouseCell)){
            attackPath = BFSPath.getPath(grid, pc.getLoc(), cell.getLoc());
        }

        return attackPath;
    }

    // Method returns Ideal path
    public List<Point> getIdealPath(PacCell[][] grid, PacmanCell pc){
        List<Point> idealPath = new ArrayList();
        
        // If there is already a path, then continue in it
        if(!path.isEmpty()){
            idealPath = path;
        }
        else{
            // Call minimax to build the ideal path
            idealPath = BFSPath.getPath(grid, pc.getLoc(),
                minimax(grid, getPaco(pc), getLoka(grid), getInka(grid), 0, false).point);
        }

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
        ghostLocations = PacUtils.findGhosts(grid);

        for (Point ghost : ghostLocations) {
            int tempDistance = BFSPath.getPath(grid, ghost, pacMan).size();
            if (tempDistance < shortestDistance) {
                shortestDistance = tempDistance;
            }
        }

        // Get and Set the PacMode
        if(!ghostLocations.isEmpty()){
            GhostCell spooky = (GhostCell)PacUtils.nearestGhost(pc.getLoc(), grid);
            pacMode = spooky.getMode();
        }
        else{
            pacMode = PacMode.CHASE;
        }
        
        return shortestDistance;
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
