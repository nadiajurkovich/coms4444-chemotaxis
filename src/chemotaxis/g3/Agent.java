package chemotaxis.g3;

import java.util.Map;
import chemotaxis.g3.Language;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

public class Agent extends chemotaxis.sim.Agent {

    Language lang;

    /**
     * Agent constructor
     *
     * @param simPrinter  simulation printer
     *
     */
	public Agent(SimPrinter simPrinter) {
        super(simPrinter);
        this.lang = new Language(simPrinter);
	}

    /**
     * Move agent
     *
     * @param randomNum        random number available for agents
     * @param previousState    byte of previous state
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     * @return                 agent move
     *
     */
	@Override
	public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        // TODO: self-realize silent + n/a, pause
        // TODO: what happens if the state instructed is fully blocked off? 
        Move move = new Move();
        Integer prevByte = (int) previousState;
        char[] nextState = new char[9];
        
        // check for new instruction first
        String instructionCheck = checkForInstructions(currentCell, neighborMap);
        String prevState = " ";

        System.out.println("New instruction: " + instructionCheck);
        
        // if instruction exists, get the new state
        if (instructionCheck != null) {
            prevState = lang.getState(instructionCheck, prevByte);
        }
        else {
            // if no instruction exists, keep running with the previous state
            prevState = lang.getState(prevByte);
        }

        System.out.println("prevState: " + prevState);

        //  X    ±    N   [.*]  Y    ±    M   [.*] [C/R]
        // '0', '+', '0', '*', '0', '+', '0', '.', 'C'
        //  0    1    2    3    4    5    6    7    8

        // based on the prevState, check the surroundings and find an opening for the next move 
        if (mobilityUp(prevState, neighborMap)) {
            System.out.println("Agent can + should move east/up");
            if (needsToRepeat(prevState)) {
                nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '.',
                                         '0', prevState.charAt(5), prevState.charAt(6), '*', 'C'};
                if (prevState.charAt(2) == '0' || prevState.charAt(6) == '0')
                    nextState[8] = 'R';
            }
            else {
                nextState = new char[] { prevState.charAt(0), prevState.charAt(1), prevState.charAt(2), '.',
                                         (char)((int) prevState.charAt(4) + 1), prevState.charAt(5), prevState.charAt(6), '*', 'C'};
                if (nextState[4] == (char)((int)nextState[6] - 1))
                    nextState[8] = 'R';
            }
            move.directionType = DirectionType.EAST;
        }
        else if (mobilityDown(prevState, neighborMap)) {
            System.out.println("Agent can + should move west/down");
            if (needsToRepeat(prevState)) {
                nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '.',
                                         '0', prevState.charAt(5), prevState.charAt(6), '*', 'C'};
                if (prevState.charAt(2) == '0' || prevState.charAt(6) == '0')
                    nextState[8] = 'R';
            }
            else {
                nextState = new char[] { prevState.charAt(0), prevState.charAt(1), prevState.charAt(2), '.',
                                         (char)((int) prevState.charAt(4) + 1), prevState.charAt(5), prevState.charAt(6), '*', 'C'};
                if (nextState[4] == (char)((int)nextState[6] - 1))
                    nextState[8] = 'R';
            }
            move.directionType = DirectionType.WEST;
        }
        else if (mobilityLeft(prevState, neighborMap)) {
            System.out.println("Agent can + should move north/left");
            if (needsToRepeat(prevState)) {
                nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '*',
                                         '0', prevState.charAt(5), prevState.charAt(6), '.', 'C'};
                if (prevState.charAt(2) == '0' || prevState.charAt(6) == '0')
                    nextState[8] = 'R';
            }
            else {
                nextState = new char[] { (char)((int) prevState.charAt(0) + 1), prevState.charAt(1), prevState.charAt(2), '*',
                                         prevState.charAt(4), prevState.charAt(5), prevState.charAt(6), '.', 'C'};
                if (nextState[0] == (char)((int)nextState[2] - 1))
                    nextState[8] = 'R';
            } 
            move.directionType = DirectionType.NORTH;
        }
        else if (mobilityRight(prevState, neighborMap)) {
            System.out.println("Agent can + should move south/right");
            if (needsToRepeat(prevState)) {
                nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '*',
                                         '0', prevState.charAt(5), prevState.charAt(6), '.', 'C'};
                if (prevState.charAt(2) == '0' || prevState.charAt(6) == '0')
                    nextState[8] = 'R';
            }
            else {
                nextState = new char[] { (char)((int) prevState.charAt(0) + 1), prevState.charAt(1), prevState.charAt(2), '*',
                                         prevState.charAt(4), prevState.charAt(5), prevState.charAt(6), '.', 'C'};
                if (nextState[0] == (char)((int)nextState[2] - 1))
                    nextState[8] = 'R';
            }
            move.directionType = DirectionType.SOUTH;
        }
        else {
            nextState = "pause".toCharArray();
            move.directionType = DirectionType.CURRENT;
        }

        // translate to Byte for memory 
        System.out.println("Byte for next round: " + lang.getByte(String.valueOf(nextState)).byteValue());
        move.currentState = lang.getByte(String.valueOf(nextState)).byteValue();
		System.out.println("Next state: " + String.valueOf(nextState));
		return move;
    }
    
    private String checkForInstructions(ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        char[] directions = new char[] {'_','_','_','_'};

        if (currentCell.getConcentration(ChemicalType.RED) == 1)
            directions[1] = 'R';
        if (currentCell.getConcentration(ChemicalType.GREEN) == 1)
            directions[2] = 'G';
        if (currentCell.getConcentration(ChemicalType.BLUE) == 1)
            directions[3] = 'B';

        char temp = ' ';
        for (DirectionType directionType : neighborMap.keySet()) {
            if (directionType == DirectionType.SOUTH) 
                temp = 'r';
            else if (directionType == DirectionType.NORTH) 
                temp = 'l';
            else if (directionType == DirectionType.EAST) 
                temp = 'u';
            else
                temp = 'd';

            if (neighborMap.get(directionType).getConcentration(ChemicalType.RED) == 1) {
                directions[0] = temp;
                directions[1] = 'R';
            }
            if (neighborMap.get(directionType).getConcentration(ChemicalType.GREEN) == 1) {
                directions[0] = temp;
                directions[2] = 'G';
            }
            if (neighborMap.get(directionType).getConcentration(ChemicalType.BLUE) == 1) {
                directions[0] = temp;
                directions[3] = 'B';
            }
        }

        if (!(String.valueOf(directions).equals("____")))
            return String.valueOf(directions);
        return null;
    }

    //  X    ±    N   [.*]  Y    ±    M   [.*] [C/R]
    // '0', '+', '0', '*', '0', '+', '0', '.', 'C'
    //  0    1    2    3    4    5    6    7    8
    private Boolean mobilityUp(String state, Map<DirectionType, ChemicalCell> surroundings) {
        // There's space above this cell
        // and the instuction is moving in that direction
        // and moving up is possible according to state
        // and either – not resetting AND you can move up in this cycle 
        //         or – are resetting AND you have not maxed out moves in that axis for this cycle 
        return (surroundings.get(DirectionType.EAST).isOpen()
                && state.charAt(5) == '+'
                && ((state.charAt(8) == 'C' && state.charAt(4) <= state.charAt(6) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(4) == state.charAt(6) - 1))
        );
    }

    private Boolean mobilityDown(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (surroundings.get(DirectionType.WEST).isOpen()
                && state.charAt(5) == '-'
                && ((state.charAt(8) == 'C' && state.charAt(4) <= state.charAt(6) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(4) == state.charAt(6) - 1))
        );
    }

    private Boolean mobilityLeft(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (surroundings.get(DirectionType.NORTH).isOpen()
                && state.charAt(1) == '-'
                && ((state.charAt(8) == 'C' && state.charAt(0) <= state.charAt(2) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(0) == state.charAt(2) - 1))
        );
    }

    private Boolean mobilityRight(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (surroundings.get(DirectionType.SOUTH).isOpen()
                && state.charAt(1) == '+'
                && ((state.charAt(8) == 'C' && state.charAt(0) <= state.charAt(2) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(0) == state.charAt(2) - 1))
        );
    }

    private Boolean needsToRepeat(String state) {
        return state.charAt(8) == 'R';
    }

    private Boolean followingWall(String state) {
        return state.charAt(8) == 'W';
    }

}