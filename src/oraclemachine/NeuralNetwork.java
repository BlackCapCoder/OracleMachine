/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oraclemachine;

import java.util.*;
import java.util.Random;
import java.lang.Math;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import Jama.*;

/**
 *
 * @author tania
 */
public class NeuralNetwork {
    
    private static int randInt(int aStart, int aEnd, Random aRandom){
        long range       = (long)aEnd - (long)aStart + 1;
        long fraction    = (long)(range * aRandom.nextDouble());
        int randomNumber = (int)(fraction + aStart);
        return randomNumber;
    }
    
    private static Matrix[] setWeights(int numHiddenLayers, int[] layerSizes){
        Matrix[] weightsMatrix = new Matrix[2];
        for(int i = 0; i < 2; i++){
            weightsMatrix[i] = Matrix.random(layerSizes[i + 1], layerSizes[i] + 1);
        }
        return weightsMatrix;
    }
    
    private static double[] randomWrites(int inputLayerSize, Random r){
        double[] x = new double[inputLayerSize];
        for(int i = 0; i < inputLayerSize; i++){
            x[i]  = randInt(0, 1, r);//Math.random();//
        }
        return x;
    }
    
    private static double[] addBias(double[] writesHistory){
        int rowLen = writesHistory.length;
        double[] newX = new double[rowLen + 1];
        for(int i = 0; i < rowLen; i++){
            newX[i] = writesHistory[i];
        }
        newX[rowLen] = 1;
        return newX;
    }
    
    private static double[] hiddenLayer(double[][] weights, double[] writesHistory){
        Matrix W = new Matrix(weights);
        Matrix b = new Matrix(addBias(writesHistory), 1).transpose();
        Matrix H = W.times(b);
        int n_hidden = H.getRowDimension();
        double[] hidden = new double[n_hidden];
        for(int i = 0; i < n_hidden; i++){
            hidden[i] = Math.tanh(H.get(i, 0));
        }
        return hidden;
    }
    
    private static double outputLayer(double[][] weights, double[] writesHistory){
        Matrix W = new Matrix(weights);
        Matrix b = new Matrix(addBias(writesHistory), 1).transpose();
        double pred = W.times(b).get(0, 0);
        return Math.tanh(pred);
    }
    
    private static double[][] createObservations(int sampleSize, int inputLayerSize, Random r){
        double[][] X = new double[sampleSize][inputLayerSize];
        for(int i = 0; i < sampleSize; i++){
            for(int j = 0; j < inputLayerSize; j++)
                X[i][j]  = randInt(0, 1, r); //Math.random(); //
        }
        return X;
    }
    
    private static double runNetwork(Matrix[] layers, double[] observation){
        double[] hiddenOutput = hiddenLayer(layers[0].getArray(), observation);
        for(int i = 1; i < (layers.length - 1); i++){
            hiddenOutput = hiddenLayer(layers[i].getArray(), hiddenOutput);
        }
        double result = outputLayer(layers[layers.length - 1].getArray(), hiddenOutput);
        //System.out.println(Math.round(result));
        return result;
    }
    
    private static double evaluateAll(double[] writesHistory, Matrix[] layers, 
                                      double[][] observations){
        double resNet = 0;
        for(int i = 0; i < writesHistory.length; i++){
            resNet = resNet + 
                    (writesHistory[i] - runNetwork(layers, observations[i]))*
                    (writesHistory[i] - runNetwork(layers, observations[i]));
        }
        return resNet;
    }
    
    private static Matrix[] runBackPropagation(Matrix[] layers, double[] x, 
                                               int ToL, double outputValue,
                                               double eta){
        Matrix  W1 = layers[0];
        Matrix  W2 = layers[1];
        double[] y = hiddenLayer(W1.getArray(), x);
        double z = outputLayer(W2.getArray(), y);
        double[] x_new = addBias(x);
        double[] y_new = addBias(y);
        for(int k = 0; k < ToL; k++){
            double[] updateW2 = new double[W2.getColumnDimension()];
            for(int i = 0; i < W2.getColumnDimension(); i++){
                updateW2[i] = eta*y_new[i]*(outputValue - z)*(1 - z*z);
            }
            int count = 1;
            double[][] updateW1 = new double[W1.getColumnDimension()][W1.getRowDimension()];
            for(int i = 0; i < W1.getColumnDimension(); i++){
                for(int j = 0; j < W1.getRowDimension(); j++){
                    updateW1[i][j] = eta*(outputValue - z)*(1 - z * z)*W2.get(0, j)*(1 - y_new[j]*y_new[j])*x_new[i];
                    //updateW1[i][j] = -eta*(outputValue - z)*W2.get(0, j)*x_new[i];
                    count++;
                }
            }
          
            // UPDATE
            Matrix MupdateW1 = new Matrix(updateW1).transpose();
            Matrix MupdateW2 = new Matrix(updateW2, 1);
            W1.plusEquals(MupdateW1);
            W2.plusEquals(MupdateW2);
            // Recalculate inner values.
            y = hiddenLayer(W1.getArray(),  x);
            z = outputLayer(W2.getArray(), y);
            // Add bias
            y_new = addBias(y);
        }
        layers[0] = W1;
        layers[1] = W2;
        return layers;
    }


    
    
    // Execute all the pipeline
    private static Matrix[] trainNN(double[][] observations, double[] writesHistory,
                                    Matrix[] layers,
                                    double eta, int numHiddenLayers, 
                                    int[] layerSizes, Random r, 
                                    int numEpochs){
        int obs;
        double y;
        // int ToL;
        int epoch_index = 0;
        double [] x;
        Set<Integer> totalObs = new HashSet<Integer>();
        // Initial Evaluation
        double objective = evaluateAll(writesHistory, layers, observations);
        double auxObjective = objective;
        // ToL  = (int)Math.sqrt(objective);
        System.out.println("numero_epoca = " + epoch_index+" error_cuadratico = " + objective);
        //while(epochs < numEpochs && auxObjective >= objective){
        while(epoch_index < numEpochs){
            auxObjective = objective;
            obs = randInt(0, (writesHistory.length - 1), r);
            x = observations[obs];
            y = writesHistory[obs];
            // Run Back Propagation
            layers = runBackPropagation(layers, x, 1, y, eta);
            objective = evaluateAll(writesHistory, layers, observations);
            // Add state to observed states
            totalObs.add(obs);
            // If epoch completed
            if(totalObs.size() == writesHistory.length){
                epoch_index++;
                System.out.println("numero_epoca = " + epoch_index+" error_cuadratico = " + objective);
                totalObs.clear();
            }
        }
        return layers;
    }
    
    private static String generateRandomCode(Random r, int size){
        String[] code;
        code = new String[size];
        for(int i=0; i<1024; i++){
            code[i] = String.valueOf(r.nextInt(2));
        }
        return String.join("", code);
    }
    
    
    
    public static void main(String args[]){
        Scanner scanner  = new Scanner(System.in);
        
        System.out.println("Entra el valor de la semilla: ");
        Random r = new Random(Integer.parseInt(scanner.next()));
        System.out.println("Introduce el tamanio de la capa oculta: ");
        int hiddenLayerSize = Integer.parseInt(scanner.next());
        System.out.println("Introduce el numero de epocas ");
        int numEpochs = Integer.parseInt(scanner.next());
        System.out.println("Introduce la tasa de aprendizaje: ");
        double eta = Double.parseDouble(scanner.next());
        System.out.println("¿Correr test de backpropagation? (1=y/0=n)");
        int testBackProp = Integer.parseInt(scanner.next());
        if (testBackProp==1){
            int numHiddenLayers = 1;
            int[] layerSizes = new int[3];
            layerSizes[0] = 3;
            layerSizes[1] = hiddenLayerSize;
            layerSizes[2] = 1;
            // Simulate observations
            int numObservations = 100; // This should be given by the Turing Machine.
            Matrix[] layers  = setWeights(numHiddenLayers, layerSizes);
            double[] writesHistory = new double[] {0,0,1,0,0,1,0,0,1,0,0,1,0,0,1,0,0,1,0,0,1};
            double[][] observations = new double[21][3];
            observations[0] = new double[] {1, 1, 0 };
            observations[1] = new double[] {1, 0, 1};
            observations[2] = new double[] {0, 1, 0};
            observations[3] = new double[] {1, 1, 0 };
            observations[4] = new double[] {1, 0, 1};
            observations[5] = new double[] {0, 1, 0};
            observations[6] = new double[] {1, 1, 0 };
            observations[7] = new double[] {1, 0, 1};
            observations[8] = new double[] {0, 1, 0};
            observations[9] = new double[] {1, 1, 0 };
            observations[10] = new double[] {1, 0, 1};
            observations[11] = new double[] {0, 1, 0};
            observations[12] = new double[] {1, 1, 0 };
            observations[13] = new double[] {1, 0, 1};
            observations[14] = new double[] {0, 1, 0};
            observations[15] = new double[] {1, 1, 0 };
            observations[16] = new double[] {1, 0, 1};
            observations[17] = new double[] {0, 1, 0};
            observations[18] = new double[] {1, 1, 0 };
            observations[19] = new double[] {1, 0, 1};
            observations[20] = new double[] {0, 1, 0};

            // Run Neural Net
            System.out.println("Inicia entrenamiento");

            Matrix[] outputLayers = trainNN(observations, writesHistory, layers,
                                            eta, numHiddenLayers, layerSizes, 
                                            r, numEpochs);

            for(int k = 0; k < 20; k++){ 
                double prediction = runNetwork(outputLayers,observations[k]);
                System.out.println(Math.round(prediction)==writesHistory[k]);
            }
        }else{
            System.out.println("Introduce el numero de tiempos observados: (sugerencia 4) ");
            int inputLayerSize = Integer.parseInt(scanner.next());
            System.out.println("Longitud de la cinta(integer > 0):");
            int tapeSize = Integer.parseInt(scanner.next());
            System.out.println("Maximo numero de transiciones para la maquina de Turing (integer > 0):");
            int tmMaxIters = Integer.parseInt(scanner.next());



            //Genera un código aleatorio+
            String tmCode = generateRandomCode(r, 1024);
            System.out.println("code");
            System.out.println(tmCode);
            TuringMachine tm = new TuringMachine();
            String writeSequence = tm.simulate(tmCode,tapeSize, tmMaxIters, true);
            System.out.println(writeSequence);
            System.out.println("writeSequence");
            char[] wsArray = writeSequence.toCharArray();
            int numHiddenLayers = 1;
            int[] layerSizes = new int[3];
            layerSizes[0] = inputLayerSize;
            layerSizes[1] = hiddenLayerSize;
            layerSizes[2] = 1;
            // Simulate observations
            int numObservations = wsArray.length-inputLayerSize;
            Matrix[] layers  = setWeights(numHiddenLayers, layerSizes);
            double[] writesHistory = new double[numObservations];
            
            double[][] observations = new double[numObservations][inputLayerSize];
            for(int k = 0; k < numObservations; k++){
                for(int i = 0; i < inputLayerSize; i++){ 
                    observations[k][i] = Double.parseDouble(Character.toString(wsArray[k+i]));
                }
                writesHistory[inputLayerSize] = Double.parseDouble(Character.toString(wsArray[k+inputLayerSize-1]));
                
            }
            System.out.println("Inicia entrenamiento");

            Matrix[] outputLayers = trainNN(observations, writesHistory, layers,
                                            eta, numHiddenLayers, layerSizes, 
                                            r, numEpochs);
            String omCode = generateRandomCode(r, 2048);
            System.out.println("Codigo para maquina oraculo");
            System.out.println(omCode);
            OracleMachine om = new OracleMachine(outputLayers);
            String usedStates = om.simulate(omCode,tapeSize, tmMaxIters, true);
            System.out.println("Estados usados por la maquina oraculo");
            System.out.println(usedStates);
            
        }
            
    }

    
}

