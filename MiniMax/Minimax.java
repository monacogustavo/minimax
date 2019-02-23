/**
 * [Round 3]
 * 
 * Modes:
 * ======
 * Instead of having a single Minimax algorithm implementation, 
 * we will have 3 implementations: 
 * 1- Eat
 * 2- Escape
 * 3- Attack
 * 
 * The Eat:
 * --------
 * If Pac-Man is not in "danger", it will try to eat all pellets.
 * 
 * The Escape:
 * -----------
 * If Pac-Man is in "danger", it will try to escape and avoid the ghosts.
 * 
 * The Attack: (Optional)
 * -----------
 * If Pac-Man eats a power pellet, it will try to eat the ghosts. 
 * 
 * The Core: 
 * =========
 * In the first iteration, initialize the maze: 
 *      all initial states and their base utility.
 * 
 * For every iteration, find all the goodies and the ghosts. Set the variables:
 * - Maze: change the status of any eaten state
 * - Mode: based on distance to closest ghost: 
 *  - If < 3 then Escape Mode
 *  - If >= 3 then Eat Mode
 *  - Optional: if power pellet, then Attack Mode
 *  
 * 
 */
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
import pacsim.PacMode;

/**
 * Dr. Demetrios Glinos
 * CAP 4630 - Artificial Intelligence
 * College of Engineering and Computer Science
 * University of Central Florida
 * Spring 2019
 * @author Gustavo Monaco | 3626975 | gu355280
 */

class Game{
    boolean won;
    int moves;

    public Game(Boolean won, int moves){
        this.won = won;
        this.moves = moves;
    }
}
class Move{
    Point target;
    PacFace direction;
}
class State{
    Point vertex;
    double utility;
    List<Move> possibleMoves; 
    Boolean visited;

    public State(Point vertex){
        this.vertex = vertex;
        this.utility = 0.0;
        this.visited = false;
        this.possibleMoves = new ArrayList<Move>();
    }

    public State(Point vertex, double utility){
        this.vertex = vertex;
        this.utility = utility;
        this.possibleMoves = new ArrayList<Move>();
    }
    
    public List<Move> GetMoves(PacFace[] faces){
        List<Move> output = new ArrayList<Move>();
        for(PacFace face : faces){
            for(Move m : possibleMoves){
                if(face == m.direction){
                    output.add(m);
                }
            }
        }
        return output;
    }

    public void beenThereDoneThat(){
        this.visited = true;
    }
}
class Maze {
    List<State> stateList; 
    List<State> ghostStateList; 

    public double GetUtility(Point vertex){
        double output = 0.0;
        for(State s : this.stateList){
            if(s.vertex.x == vertex.x && s.vertex.y == vertex.y){
                output = s.utility;
                break;
            }
        }
        return output;
    }
    public State GetState(Point vertex){
        State output = new State(vertex);
        for(State s : this.stateList){
            if(s.vertex.x == vertex.x && s.vertex.y == vertex.y){
                output = s;
                break;
            }
        }
        return output;
    }

    @Override
    public String toString(){
        String output = "";
        int i = 0;
        double sum = 0.0;
        for(State s : this.stateList){
            output += "\n\t{ (" + i + ")";
            output += "[" + s.vertex.x + "," + s.vertex.y + "]";
            output += "=" + s.utility + ",";
            output += "[";
            for(Move p : s.possibleMoves){
                output += p.direction + ":[" + p.target.x + "," + p.target.y + "], ";
            }
            output += "]},";
            sum += s.utility;
            i++;
        }

        output = "\nMaze: (" + i + "," + sum + ")" + output;
        return output;
    }

    public Point beenThereDoneThat(State rm){
        Point output = rm.vertex;
        ArrayList<Integer> k = new ArrayList<Integer>();
        for(int i = 0; i < this.stateList.size(); i++){
            for(int j = 0; j < this.stateList.get(i).possibleMoves.size(); j++){
                if(this.stateList.get(i).possibleMoves.get(j).target == rm.vertex){
                    k.add(j);
                }
            }
            if(k.size()>0){
                for(int j : k){
                    this.stateList.get(i).possibleMoves.remove(j);
                }
                k.clear();
            }
            if(this.stateList.get(i).vertex == rm.vertex){
                k.add(i);
            }
            if(k.size()>0){
                this.stateList.remove(k.get(0));
                k.clear();
            }
        }    
        this.toString();
        return output;
    }
}
  
public class Minimax implements PacAction {

    private static boolean debug = true;

    private static final double TOP = (double)Integer.MAX_VALUE;
    private static final double POT = (double)Integer.MIN_VALUE;
    private static final double FOOD = 1.00;
    private static final double POWER = 2.00;
    private static final double GHOST = -3.00;
    private static final double GHOST_NEXT = -2.00;
    private static final double GHOST_FOLLOW = -1.00;
    private static final double EMPTY = 0.00;

    private boolean firstAction;
    private Maze maze;
    private List<Point> ghosts;
    private PacMode pacMode;
    private List<Point> path;
    private int simTime;
    private static int depth;
    private int depthLeft;
    private int bubbleRadius;
    private String mode;

    private ArrayList<Game> SimGames;

    private void InitializeMaze(PacmanCell pc, PacCell[][] grid){
        maze = new Maze();
        List<State> states = new ArrayList<State>();
        states.add(new State(pc.getLoc(), EMPTY));

        // initialize the states
        for(PacCell[] row : grid) {
            for(PacCell field : row){
                if(PacUtils.goody(field.getX(), field.getY(), grid)){ 
                    states.add(
                        new State(
                            field.getLoc(), (
                                PacUtils.power(field.getX(), field.getY(), grid) 
                                    ? POWER 
                                    : FOOD
                            )
                        )
                    );
                }            
            }
        }

        PacCell next;
        for(State s : states){
            for(PacFace face : PacFace.values()){
                next = PacUtils.distantNeighbor(face, 1, grid[s.vertex.x][s.vertex.y], grid);
                for(State ns : states){
                    if(s == ns)
                        continue;                    
                    if(ns.vertex.x == next.getX() && ns.vertex.y == next.getY()){
                        Move m = new Move();
                        m.target = next.getLoc();
                        m.direction = face; 
                        s.possibleMoves.add(m);

                        continue;
                    }
                }
            }            
        }

        maze.stateList = states;

        maze.ghostStateList = new ArrayList<State>();

    }

    private void StrangerDanger(PacCell[][] grid, Point paco){

        ghosts = PacUtils.findGhosts(grid);
        GhostCell mofo = (GhostCell)PacUtils.nearestGhost(paco, grid);
        
        // bubbleRadius = PacUtils.manhattanDistance(paco, mofo.getLoc());
        bubbleRadius = (int)PacUtils.euclideanDistance(paco, mofo.getLoc());

        if(bubbleRadius < depth){   // ESCAPE MODE ACTIVATED!
            mode = "RUUUUUUUUN";
        }
        else {                  // EAT MODE ACTIVATED!
            mode = "CHILLYBEAN";
        }  

        pacMode = mofo.getMode();
        maze.ghostStateList.clear();        

        PacCell next;
        for(Point g : ghosts) {
            for(PacFace face : PacFace.values()){
                next = PacUtils.distantNeighbor(face, 1, grid[g.x][g.y], grid);
                for(State ns : maze.stateList){               
                    if(ns.vertex.x == next.getX() && ns.vertex.y == next.getY()){
                        maze.ghostStateList.add(new State(ns.vertex, GHOST_NEXT));
                        continue;
                    }
                }
            }    
            maze.ghostStateList.add(new State(g, GHOST));        
        }
    }

    private PacFace ClosestGhost(Point point){
        PacFace output = PacFace.E;
        double dist = TOP;
        for(Point g : ghosts) {
            if(PacUtils.euclideanDistance(point, g) < dist){
                output = PacUtils.direction(point, g);
            }
        }
        return output;
    }
    public PacFace[] GetAwayFromPacFace(PacFace f){
        PacFace[] output = new PacFace[4];
        int i = 0;
        output[0] = PacUtils.reverse(f);
        for(PacFace face : PacFace.values()){
            if(face != output[0]){
                i++;
                output[i] = face;
            }
        }
        return output;
    }
    private double EvalUtility(Point point){
        double output = 0.0;
        double multiplier = 1.0;
        
        for(State s : maze.ghostStateList){
            if(point.x == s.vertex.x && point.y == s.vertex.y){
                output = s.utility;
                break;
            }
        }
        if(output == EMPTY){
            output = maze.GetUtility(point);
        }
        
        /* depending on mode: multiplier!
            chase   = utility < 0 ? 2 : 1/2
            fear    = utility < 0 ? -1 : 1
            scatter = 1

         */
        if(mode == "RUUUUUUUUN"){
            // Get away!
            multiplier = output < 0 ? 2.0 : 1/2.0;
        }
        else if(pacMode == PacMode.FEAR){
            // Get them!
            multiplier = output < 0 ? -1.0 : 1;
        }
        else{
            // Avoid them... 
            multiplier = 1.0;
        }

        output = multiplier * output;

        return output;
    }

    private State Minimize(State state, int count, PacFace[] faces){

        if(debug){
            System.out.println("Minimizing...");
        }

        if(count <= 0){
            State output = new State(state.vertex, EvalUtility(state.vertex));
            return output;
        }

        count--;
        State min = new State(null);
        min.utility = TOP;

        State temp = null;
        for(Move p : state.GetMoves(faces)){
            temp = Maximize(maze.GetState(p.target), count, faces);

            if(temp.utility < min.utility){
                min = temp;
            }
        }

        return min;
    }

    private State Maximize(State state, int count, PacFace[] faces){   

        if(count <= 0){
            State output = new State(state.vertex, EvalUtility(state.vertex));
            return output;
        }
        count--;
        State max = new State(null);
        max.utility = POT;
        
        State temp;
        for(Move p : state.GetMoves(faces)){
            temp = Minimize(maze.GetState(p.target), count, faces);
            if(temp.utility > max.utility){
                max = temp;
            }
        }
     
        if(debug){
            System.out.println("Maximizing...");
        }
        return max;
    }

    private Point Decision(Point vertex, PacCell[][] grid){
        if(mode == "RUUUUUUUUN"){    
            // if(debug){
            //     System.out.println("GET IN THE CHOPAA! ");
            // }
            PacFace g = ClosestGhost(vertex);
            PacFace[] faces = GetAwayFromPacFace(g);            
    
            return maze.beenThereDoneThat(Maximize(maze.GetState(vertex), depth, faces));
        }
        else if(mode == "CHILLYBEAN"){
            return PacUtils.nearestFood(vertex, grid);
        }
        else {
            return PacUtils.nearestFood(vertex, grid);
        }
    }

    public Minimax(int depth, String fname, int te, int gran, int max){
        if(debug){
            System.out.println("Initializing the SimGames array...");
        }
        SimGames = new ArrayList<Game>();

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

        new Minimax(depth, fname, te, gr, ml);

        System.out.println("\nAdversarial Search using Minimax by Gus Monaco:");
        System.out.println("\n\tGame board   : " + fname);
        System.out.println("\tSearch depth : " + depth + "\n");
        System.out.println(simMessage);
    }

    @Override
    public void init(){
        if(debug){
            System.out.println("Initializing the rest... ");
        }
        simTime = 0;
        firstAction = true;
        ghosts = new ArrayList();
        path = new ArrayList();
        // foodNodes = new ArrayList<PacCell>();
        // powerNodes = new ArrayList<PacCell>();
    }

    @Override
    public PacFace action(Object state){
        PacCell[][] grid = (PacCell[][]) state;

        PacmanCell pc = PacUtils.findPacman( grid );

        // 1. Make sure Pac-Man is in this game
        if( pc == null ) return null;

        // initiallizing the food and power pellets arrays
        if(firstAction){
            firstAction = false;

            if(debug){
                System.out.println("First action: before maze");
            }
            InitializeMaze(pc, grid);
            if(debug){
                System.out.println(maze.toString());
            }
             
        }

        StrangerDanger(grid, pc.getLoc());  

        if(debug){
            System.out.println("Stranger Danger: " + mode);
        } 

        
        Point target = PacUtils.nearestFood(pc.getLoc(), grid);
        if(mode == "RUUUUUUUUN" || path.isEmpty()){    
            target = Decision(pc.getLoc(), grid); 
        }  
        path = BFSPath.getPath(grid, pc.getLoc(), target);    

        if(path.size() == 0){
            Point tgt = PacUtils.nearestFood( pc.getLoc(), grid);
            path = BFSPath.getPath(grid, pc.getLoc(), tgt);
        }

        if(debug){
            System.out.println(mode + "->" + path.size());
        } 

        // take the next step on the current path

        Point next = path.remove( 0 );
        PacFace face = PacUtils.direction( pc.getLoc(), next );
        System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", 
            ++simTime, pc.getLoc().x, pc.getLoc().y, face );
        return face;
        // return newFace;
    }


}