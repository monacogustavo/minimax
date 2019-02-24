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

*/

import java.io.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import pacsim.*;

class PointScore{
    public Point point;
    public int score;

    public PointScore(Point p, int s){
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
    private PacMode pacMode;
    private List<Point> ghostLocations;


    // Utility function for miniMax()
    static int log2(int n) {
        return (n == 1) ? 0 : 1 + log2(n/2);
    }

    // Method returns the desired value from PacMan using minimax 
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
    // Method to validate PointScore
    public boolean validRadius(List<PointScore> radius, Point p){
        for(PointScore ps : radius){
            if(ps.point.getX() == p.getX() && ps.point.getY() == p.getY()){
                return true;
            }
        }
        return false;
    }
    // Method to Get PointScore
    public PointScore getRadiusPoint(List<PointScore> radius, Point p){
        for(PointScore ps : radius){
            if(ps.point.getX() == p.getX() && ps.point.getY() == p.getY()){
                return ps;
            }
        }
        return null;
    }
    // Get the immediate steps 
    public List<PointScore> immediateRadius(List<PointScore> radius, PointScore vertex){
        List<PointScore> pointy = new ArrayList();

        // Get PacMan's current location
        int homeY = vertex.point.y;//ps.point.y;
        int homeX = vertex.point.x;//ps.point.x;

        // Set-up proper indices
        List<Point> cardinal = new ArrayList<Point>();
        cardinal.add(new Point(homeX, homeY - 1)); // north
        cardinal.add(new Point(homeX, homeY + 1)); // south
        cardinal.add(new Point(homeX + 1, homeY)); // east
        cardinal.add(new Point(homeX - 1, homeY)); // west 

        // if point is a valid state, give it a value
        for(Point p : cardinal){
            if(validRadius(radius, p)){
                pointy.add(getRadiusPoint(radius, p));
            }
        }
        return pointy;
    }
    // Method to Maximize
    public PointScore Maximize(List<PointScore> radius, PointScore vertex, int d){
        List<PointScore> pointy = immediateRadius(radius, vertex);
        if(pointy.isEmpty()){
            return vertex;
        }
        PointScore max = new PointScore(vertex.point, POT);   
        if(d <= 0){    
            for(PointScore ps : pointy){
                if(ps.score > max.score){
                    max = ps;
                }
            }
        }
        else{
            d--;     
            for(PointScore ps : pointy){
                PointScore min = Minimize(radius, ps, d);
                if(min.score > max.score){
                    max = min;
                }
            }
        }
        max.score += vertex.score;
        return max;

    }

    // Method to Minimize
    public PointScore Minimize(List<PointScore> radius, PointScore vertex, int d){
        List<PointScore> pointy = immediateRadius(radius, vertex);
        if(pointy.isEmpty()){
            return vertex;
        }
        PointScore min = new PointScore(vertex.point, TOP);   
        if(d <= 0){    
            for(PointScore ps : pointy){
                if(ps.score < min.score){
                    min = ps;
                }
            }
        }
        else{
            d--;     
            for(PointScore ps : pointy){
                PointScore max = Maximize(radius, ps, d);
                if(max.score < min.score){
                    min = max;
                }
            }
        }
        min.score += vertex.score;
        return min;
    }


    // Method to validate state
    public boolean validState(List<List<Point>> state, Point p){
        for(List<Point> row : state){
            for(Point col : row){
                if(col.getX() == p.getX() && col.getY() == p.getY()){
                    return true;
                }
            }
        }
        return false;
    }

    // Method that sets the initial values for PacMan's path
    public List<PointScore> getScorePoints(Point start){
        List<PointScore> pointy = new ArrayList();

        // Get PacMan's current location
        int homeY = start.y;//ps.point.y;
        int homeX = start.x;//ps.point.x;

        // Set-up proper indices
        List<Point> cardinal = new ArrayList<Point>();
        cardinal.add(new Point(homeX, homeY - 1)); // north
        cardinal.add(new Point(homeX, homeY + 1)); // south
        cardinal.add(new Point(homeX + 1, homeY)); // east
        cardinal.add(new Point(homeX - 1, homeY)); // west 

        // if point is a valid state, give it a value
        for(Point p : cardinal){
            if(validState(state, p)){
                pointy.add(new PointScore(p, FOOD));
            }
        }
        return pointy;
    }

    // Method to calculate the scores from a starting point
    public List<PointScore> getScores(Point from){
        List<PointScore> output = new ArrayList<PointScore>();
        int d = depth > MIN_DEPTH ? depth : MIN_DEPTH; // how deep to go

        PointScore start = new PointScore(from, EMPTY);
        output.add(start);
        int index = 0;

        while(d > 0){
            d--;
            
            List<PointScore> radius = new ArrayList<PointScore>();
            for(int i = index; i < output.size(); i++){
                List<PointScore> t = getScorePoints(output.get(i).point);
                for(PointScore ps : t){
                    ps.score += d;
                    radius.add(ps);
                    index++;
                }                 
            }            

            for(PointScore ps : radius){
                for(int j = 0; j < output.size(); j++){
                    if(ps.point.getX() == output.get(j).point.getX() 
                    && ps.point.getY() == output.get(j).point.getY()){
                        ps.score = EMPTY;
                    }
                }
                output.add(ps);
            }

        }
        
        
        return output;
    }

    // Method to recalculate the scores for spooky situations
    public List<PointScore> strangerDanger(List<PointScore> radius){

        List<PointScore> spookySituations;
        for(Point g : ghostLocations){
            spookySituations = getScores(g);

            for(PointScore ps : spookySituations){
                for(PointScore r : radius){
                    if(ps.point.getX() == r.point.getX() 
                    && ps.point.getY() == r.point.getY()){
                        r.score = r.score - (SCARE * ps.score);
                    }
                }                
            }
            spookySituations.clear();
        }

        return radius;
    }

    // Method to get the radius
    public List<PointScore> getRadius(Point from){
        List<PointScore> output = strangerDanger(getScores(from));
        if(debug){
            System.out.println("Radius: [");
            for(PointScore ps : output){
                System.out.println("{(" + ps.point.x +"," + ps.point.y + "):" + ps.score + "},");
            }
            System.out.println("]");                
        }
        return output;
    } 

    // Method returns Ideal path
    public List<Point> getIdealPath(PacCell[][] grid, PacmanCell pc){
        List<Point> idealPath = new ArrayList();
        int d = depth;
        // If there is already a path, then continue in it
        if(!path.isEmpty()){
            idealPath = path;
        }
        else{
            List<PointScore> radius = getRadius(pc.getLoc());
            // Call minimax to build the ideal path
            Point tgt = Maximize(radius, radius.get(0), d).point;
            idealPath = BFSPath.getPath(grid, pc.getLoc(), tgt);
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
        Point tgt = PacUtils.nearestGhost(pc.getLoc(), grid).getLoc();
        attackPath = BFSPath.getPath(grid, pc.getLoc(), tgt);

        return attackPath;
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
            GhostCell spooky = (GhostCell)PacUtils.nearestGhost(ghostLocations.get(0), grid);
            pacMode = spooky.getMode();
        }
        else{
            pacMode = PacMode.CHASE;
        }
        
        return shortestDistance;
    }

    //*** */ Distance of closest food pellet
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

        System.out.println("\nAdversarial Search using Minimax by Gus Monaco:");
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

        PacCell[][] grid = (PacCell[][]) OriginalState;

        // initialize the states
        if(firstAction){
            firstAction = false;

            // if(debug){System.out.println("state:");}
            for(PacCell[] row : grid) {
                List<Point> cols = new ArrayList();
                for(PacCell field : row){
                    if(!(field instanceof WallCell || field instanceof HouseCell)){
                        cols.add(field.getLoc());
                        // if(debug){
                        //     System.out.println("\t[" + field.getLoc().getX() 
                        //     + ", " + field.getLoc().getY() + "]" );}
                    } 
                }
                state.add(cols);
            }
             
        }
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
