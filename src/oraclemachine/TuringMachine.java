/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oraclemachine;
import java.util.*;

/**
 *
 * @author tania
 */
public class TuringMachine {
    
    public int maxStates;
    final int stateCodeLength = 16;
    final int transitionLength = 8;
    final int stateLength = 6;
    final int writeMoveLength = 1;
    
    public TuringMachine() {
        maxStates = 64;
    }
    
    public TuringMachine(int x) {
        maxStates = x;
    }
    
    class Instruction {
        int currentStateNumber;
        int readSymbol; // true is 1, false is 0
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
        Instruction[] instructions = new Instruction[maxStates*2];
        int start;
        int end;
        for(int i=0; i<128; i+=2){
            //start with 0 instruction
            Instruction inst = new Instruction();
            inst.readSymbol = 0;
            inst.currentStateNumber = i;
            start = i*transitionLength;
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
            
            //start with 0 instruction
            Instruction inst2 = new Instruction();
            inst2.readSymbol = 1;
            inst2.currentStateNumber = i;
            start = (i+1)*transitionLength;
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
        }
        return instructions;
    }
    
    private int[] initTape(int tapeSize){
        int[] tape = new int[tapeSize];
        Arrays.fill(tape, 0);
        return tape;
    }
    
    public String simulate(String tmCode, int tapeSize, 
                           int maxIters, boolean returnWrites) {
        Instruction[] instructions = readCode(tmCode);
        boolean halt = false;
        int[] tape = initTape(tapeSize);
        int position =  tapeSize/2;
        int state = 0;
        int numIters = 0;
        int[] writes = new int[maxIters+1];
        while (!halt){
            
            System.out.println("position");
            System.out.println(position);
            numIters += 1;
            Instruction inst;
            int tapeValue = tape[position];
            System.out.print("tape value " );
            System.out.println(tapeValue);
            if (tapeValue == 0){
                inst = instructions[state*2];
            }else{
                inst = instructions[state*2 + 1];
            }
            System.out.println(inst.isHalt());
            System.out.print("write symbol " );
            System.out.println(inst.writeSymbol);
            tape[position] = inst.writeSymbol;
            writes[numIters] = inst.writeSymbol;
            position = (inst.moveDirection == 'L') ? position-1: position+1;
            System.out.print("moveDirection " );
            System.out.println(inst.moveDirection);
            if (position < 0 || position >= tapeSize ||
                numIters >= maxIters || inst.isHalt())
            {
                System.out.println("Entered Halt");
                halt = true;
            }
            state = inst.newStateNumber;
        }
        
        
        
        if (returnWrites){
            StringBuilder sb = new StringBuilder(writes.length);
            for (int i : writes) {
              sb.append(i);
              sb.append("");
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
