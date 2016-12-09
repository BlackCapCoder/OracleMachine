package oraclemachine;
import java.util.*;
import Jama.*;


/**
 *
 * @author tania
 */
public class OracleMachine {
    
    public int maxStates;
    final int stateCodeLength = 16;
    final int transitionLength = 8;
    final int stateLength = 6;
    final int writeMoveLength = 1;
    final Matrix[] weights;
    
    
    public OracleMachine(Matrix [] w) {
        maxStates = 64;
        weights = w;
    }
    
    public OracleMachine(Matrix [] w, int x) {
        maxStates = x;
        weights = w;
    }
    
    class Instruction {
        int currentStateNumber;
        int readSymbol; // true is 1, false is 0
        int oracleSymbol; // true is 1, false is 0
        int newStateNumber;
        int writeSymbol; // true is 1, false is 0
        char moveDirection; //L is left, R is right

        boolean isHalt()
        {
            return (currentStateNumber == maxStates);
        }
    }
    
    /**
     *
     * @param tape
     * @return Instruction[]
     */
    private Instruction[] readCode(String tmCode){
        Instruction[] instructions = new Instruction[maxStates*4];
        int start;
        int end;
        for(int i=0; i<64*4; i+=4){
            //start with 00 instruction
            Instruction inst = new Instruction();
            inst.readSymbol = 0;
            inst.oracleSymbol = 0;
            inst.currentStateNumber = i;
            start = i+transitionLength;
            end = start + stateLength;
            inst.newStateNumber = Integer.parseInt(
                                    tmCode.substring(start,
                                    end),2);
            start = end;
            end = start + writeMoveLength;
            inst.writeSymbol = Integer.parseInt(tmCode.substring(start,
                                end));
            start = end;
            end = start + writeMoveLength;
            inst.moveDirection = (tmCode.substring(start,
                                    end).equals("1")) ? 'R' : 'L';
            instructions[i]=inst;
            
            //then 01 instruction
            Instruction inst2 = new Instruction();
            inst2.readSymbol = 0;
            inst2.oracleSymbol = 1;
            inst2.currentStateNumber = i;
            start = i+2*transitionLength;
            end = start + stateLength;
            inst2.newStateNumber = Integer.parseInt(
                                    tmCode.substring(start,
                                    end),2);
            start = end;
            end = start + writeMoveLength;
            inst2.writeSymbol = Integer.parseInt(tmCode.substring(start,
                                end));
            start = end;
            end = start + writeMoveLength;
            inst2.moveDirection = (tmCode.substring(start,
                                    end).equals("1")) ? 'R' : 'L';
            instructions[i+1]=inst2;
            
            //then 10 instruction
            Instruction inst3 = new Instruction();
            inst3.readSymbol = 1;
            inst3.oracleSymbol = 0;
            inst3.currentStateNumber = i;
            start = i+3*transitionLength;
            end = start + stateLength;
            inst3.newStateNumber = Integer.parseInt(
                                    tmCode.substring(start,
                                    end),2);
            start = end;
            end = start + writeMoveLength;
            inst3.writeSymbol = Integer.parseInt(tmCode.substring(start,
                                end));
            start = end;
            end = start + writeMoveLength;
            inst3.moveDirection = (tmCode.substring(start,
                                    end).equals("1")) ? 'R' : 'L';
            instructions[i+2]=inst3;
            
            //then 11 instruction
            Instruction inst4 = new Instruction();
            inst4.readSymbol = 1;
            inst4.oracleSymbol = 1;
            inst4.currentStateNumber = i;
            start = i+4*transitionLength;
            end = start + stateLength;
            inst4.newStateNumber = Integer.parseInt(
                                    tmCode.substring(start,
                                    end),2);
            start = end;
            end = start + writeMoveLength;
            inst4.writeSymbol = Integer.parseInt(tmCode.substring(start,
                                end));
            start = end;
            end = start + writeMoveLength;
            inst4.moveDirection = (tmCode.substring(start,
                                    end).equals("1")) ? 'R' : 'L';
            instructions[i+3]=inst4;
        }
        return instructions;
    }
    
    private int[] initTape(int tapeSize){
        int[] tape = new int[tapeSize];
        Arrays.fill(tape, 0);
        return tape;
    }
    
    public String simulate(String tmCode, int tapeSize, 
                           int maxIters, boolean returnStates) {
        Instruction[] instructions = readCode(tmCode);
        boolean halt = false;
        int[] tape = initTape(tapeSize);
        int position =  tapeSize/2;
        int state = 0;
        int numIters = 0;
        ArrayList<Integer> usedStates = new ArrayList<>();
        while (!halt){
            usedStates.add(state);
            //System.out.print("position " );
            //System.out.println(position);
            numIters += 1;
            Instruction inst;
            int tapeValue = tape[position];
            int prediction = 1;
            //System.out.print("tape value " );
            //System.out.println(tapeValue);
            if (tapeValue == 0 && prediction == 0){
                inst = instructions[state*4];
            }else if (tapeValue == 0 && prediction == 1){
                inst = instructions[state*4 + 1];
            }else if (tapeValue == 1 && prediction == 0){
                inst = instructions[state*4 + 2];
            }else{
                inst = instructions[state*4 + 3];
            }
            //System.out.print("write symbol " );
            //System.out.println(inst.writeSymbol);
            tape[position] = inst.writeSymbol;
            position = (inst.moveDirection == 'L') ? position-1: position+1;
            //System.out.print("moveDirection " );
            //System.out.println(inst.moveDirection);
            if (position < 0 || position >= tapeSize ||
                numIters > maxIters || inst.isHalt())
            {
                halt = true;
            }
            state = inst.newStateNumber;
        }
        
        if (returnStates){
            StringBuilder sb = new StringBuilder(usedStates.size());
            for (int i : usedStates) {
              sb.append(i);
              sb.append(" ");
            }
            return sb.toString();
        }else{
            StringBuilder sb = new StringBuilder(tape.length);
            for (int i : tape) {
              sb.append(i);
            }
            return sb.toString();
        }
    }
    
    
    
}
