//CS440 PA3
//Timothy Mock

import java.util.*;
import java.io.*;
import java.text.*;

public class HMM {
  
  //The main function basically grabs the commands and passes it to the scanFile function.
  public static void main(String args[]) throws IOException {
    System.out.println("Select Command (or type \"quit\"):");
    
    Scanner console = new Scanner(System.in);
    String line = console.nextLine();
    
    while(!line.equalsIgnoreCase("quit")){
      if(line.equalsIgnoreCase("./recognize") || line.equalsIgnoreCase("recognize")){
        try{
          System.out.println("HMM File Name:");
          String hmmFile = console.nextLine();
          File hmm = new File(hmmFile);
          System.out.println("OBS File Name:");
          String obsFile = console.nextLine();
          File obs = new File(obsFile);
          System.out.println("Running \"./recognize "+ hmmFile +" "+ obsFile + "\"...");
          System.out.println();
          scanFile(hmm, obs, "", "recognize");
        }catch (FileNotFoundException e){
          System.out.println("One or more files not found. Please try again.");
        }
        System.out.println();
        
      }else if(line.equalsIgnoreCase("./statepath") || line.equalsIgnoreCase("statepath")){
        try{
          System.out.println("HMM File Name:");
          String hmmFile = console.nextLine();
          File hmm = new File(hmmFile);
          System.out.println("OBS File Name:");
          String obsFile = console.nextLine();
          File obs = new File(obsFile);
          System.out.println("Running \"./statepath "+ hmmFile +" "+ obsFile +"\"...");
          System.out.println();
          scanFile(hmm, obs, "", "statepath");
        }catch (FileNotFoundException e){
          System.out.println("One or more files not found. Please try again.");
        }
        System.out.println();
        
      }else if(line.equalsIgnoreCase("./optimize") || line.equalsIgnoreCase("optimize")){
        try{
          System.out.println("HMM File Name:");
          String hmmFile = console.nextLine();
          File hmm = new File(hmmFile);
          System.out.println("OBS File Name:");
          String obsFile = console.nextLine();
          File obs = new File(obsFile);
          System.out.println("New HMM File Name:");
          String newHmmFile = console.nextLine();
          System.out.println("Running \"./optimize "+ hmmFile +" "+ obsFile +" " + newHmmFile + "\"...");
          System.out.println();
          scanFile(hmm, obs, newHmmFile, "optimize");
        }catch (FileNotFoundException e){
          System.out.println("One or more files not found. Please try again.");
        }
        System.out.println();
        
      }else{
        System.out.println("\nIncorrect command.");
        System.out.println();
      }
      System.out.println("Select Command (or type \"quit\"):");
      line = console.nextLine();
    }
  }
  //What this method does, is it takes the inputs given from the prompts in the main method and opens the files that were given
  //It then scans through the file and takes out all the important information and stores it into each given variable.
  public static void scanFile(File hmm, File obs, String newHmm, String command) throws FileNotFoundException{
    //Initialize Variables
    int numStates;
    int syms;
    int T;
    String syntax[];
    String vocab[];
    State HMM[];
    String pi[];
    String A[];
    String B[];
    int[] numobs;
    String observ[];
    
    Scanner scan = new Scanner(hmm); //Begin scanning the hmm file.
    numStates = scan.nextInt(); //states
    syms = scan.nextInt();  //symbols
    T = scan.nextInt(); //length of observation sequences
    scan.nextLine(); //skip to next line
    syntax = scan.nextLine().split(" ");  //gets the syntactic structures
    vocab = new String[syms]; ;
    vocab = scan.nextLine().split(" ");  //gets the vocab used 
    scan.nextLine();  //skips to next line
    A = new String[numStates];  // transition probs for state
    for(int i=0; i<numStates; i++){ A[i] = scan.nextLine(); }
    scan.nextLine();       // skip over the text "b:"
    B = new String[numStates];  // observation probs for vocab for state
    for(int i=0; i<numStates; i++){ B[i] = scan.nextLine(); }
    scan.nextLine();       // skip to next line
    pi = scan.nextLine().split(" ");    // pi is the starting probs
    
    HMM = new State[numStates]; //an array of states (class on bottom of proj) 
    for(int i=0; i<numStates; i++){
      HMM[i] = new State(syntax[i], i, A[i], B[i], Double.parseDouble(pi[i])); //initializes each state
    }
    
    scan = new Scanner(obs); //start scanning the obs file
    int numsets = scan.nextInt(); // number of sets
    scan.nextLine();
    observ = new String[numsets]; // get the observation sentence
    
    // For each set, run the command
    for (int i = 0; i < numsets; i++) {
      scan.nextInt();
      scan.nextLine();
      observ = scan.nextLine().split("\\s+");
      if (command.equalsIgnoreCase("recognize")){
        recognize(HMM, vocab, observ, observ.length);
      } else if (command.equalsIgnoreCase("statepath")) {
        statepath(HMM, vocab, observ, observ.length);
      } else if (command.equalsIgnoreCase("optimize")){
        optimize(HMM, vocab, observ, observ.length, newHmm, obs.getName(), syms);
      }
    }
  }
  //The recognize program outputs the observation probability of each input sequence 
  //Equations provided here:
  //http://cs.brown.edu/research/ai/dynamics/tutorial/Documents/HiddenMarkovModels.html
  public static void recognize(State HMM[], String vocab[], String words[], int seqLen){
    //Initialize Variables
    int numStates = HMM.length;    
    double[][] probs = new double[numStates][seqLen+1];
    double prob = 0;
    double sum = 0;
    
    //initialize
    for (int k = 0; k < numStates; k++) {
      probs[k][0] = HMM[k].pi * vocabmatch(words[0], k, vocab, HMM[k].B);
    }
    
    //calculate the forward alpha values
    for (int t = 1; t < words.length; t++) {
      for (int j = 0; j < numStates; j++) {
        sum = 0;
        for (int i = 0; i < numStates; i++) {
          sum += probs[i][t-1]*HMM[i].A[j];  //[sum_i=1^N alpha_t(i) a_ij]
        }
        probs[j][t] = sum*vocabmatch(words[t], j, vocab, HMM[j].B); //[sum_i=1^N alpha_t(i) a_ij] b_j(o_t+1)
      }
    }
    
    //Print out the probability in decimal format
    prob = probs[0][seqLen-1] + probs[1][seqLen-1] + probs[2][seqLen-1] + probs[3][seqLen-1];
    if (prob == 0.0) {
      System.out.println(prob);
    } else {
      DecimalFormat df = new DecimalFormat("#.######");
      System.out.println(df.format(prob));
    }
  }
  //Statepath uses the viterbi algorithm to determine the optimal path for each obs set and report the probability of it
  //Equations provided here:
  //http://cs.brown.edu/research/ai/dynamics/tutorial/Documents/HiddenMarkovModels.html
  public static void statepath(State HMM[], String vocab[], String words[], int seqLen){
    //Initialize variables
    int numStates = HMM.length;
    int state = 0;
    double delta[][] = new double[seqLen+1][numStates];
    int psi[][] = new int[seqLen+1][numStates];
    double[] maxarray = new double[numStates];
    double[] maxes = new double[2];
    int ind = 0;
    
    //initialize deltas
    for(state=0; state<numStates; state++){ 
      delta[0][state] = HMM[state].pi * vocabmatch(words[0], state, vocab, HMM[state].B); //delta_0(i) = pi b_i(o_1)
    }
    for(state=0; state<numStates; state++){ 
      delta[1][state] = delta[0][state] * vocabmatch(words[0], state, vocab, HMM[state].B); //delta_1(i) = delta_0(i) b_i(o_1)
    }
    
    //Here, we go through and calculate the maximum array to store for the delta and psi values
    for (int t = 0; t < seqLen-1; t++) {
      for (int j = 0; j < numStates; j++) {
        ind = 0;
        for (int i = 0; i < numStates; i++) {
          maxarray[i] = delta[t][i]*HMM[i].A[j]*vocabmatch(words[t+1], j, vocab, HMM[j].B); //Here, we calculate [delta_t-1(i)a_ij]b_j(o_t)
        }
        //This sorts the maxarray so we can get the maximum value to store for delta
        for (int i = 1; i < numStates; i++) {
          if (maxarray[i-1] > maxarray[i]) {
            maxarray[i] = maxarray[i-1];
          } else {
            ind = i;
          }
        }
        delta[t+1][j] = maxarray[numStates-1]; //delta_t(j) = max_i [delta_t-1(i)a_ij]b_j(o_t)
        psi[t+1][j] = ind; //argmax_i [delta_t-1(i)a_ij]
      }
    }

    //Terminate
    double p[] = new double[seqLen+1];  // probability of best path at length t
    int q[] = new int[seqLen+1];    // state with highest probability at time t
    
    for(int t=0; t<seqLen; t++){
      for(int i=0; i<numStates; i++){
        if(delta[t][i] > p[t]){ //find the highest delta probability and store it in p
          p[t] = delta[t][i]; //store delta in p
          q[t] = i; //store the location of the highest probability
        }
      }
    }
    
    //Format and print the probability in decimal format
    if(p[seqLen-1] > 0.0){
      DecimalFormat df = new DecimalFormat("#.######");
      System.out.print(df.format(p[seqLen-1]) + " ");
    }
    else{
      System.out.print(p[seqLen-1] + " ");
    }
    
    //Print out the name of the path for each state of the HMM.
    if(p[seqLen -1] > 0.0){
      for(int t=0; t<seqLen; t++){
        System.out.print( HMM[ q[t] ].name + " ");
      }
    }
    System.out.println();
    
  }
  //Uses the Baum-Welch algorithm to optimize the hmm file. Then it prints out the probability aftewards.
  //Equations provided here:
  //http://cs.brown.edu/research/ai/dynamics/tutorial/Documents/HiddenMarkovModels.html
  public static void optimize(State HMM[], String vocab[], String words[], int seqLen, String newHmm, String obs, int syms){
    //Initialize variables
    int numStates = HMM.length;
    double probs[][] = new double[numStates][seqLen+1];
    double backwardProbs[][] = new double[numStates][seqLen+1];
    double gamma[][] = new double[numStates][seqLen+1];
    double[] newpi = new double[numStates];
    double[][] newA = new double[numStates][numStates];
    double[][] newB = new double[syms][numStates];
    double[][][] xis = new double[seqLen-1][numStates][numStates];
    double PrOlamda = 0;
    double prob = 0.0;
    double sum = 0.0;
    double sumgamma = 0;
    double sumxi = 0;
    double sumgammaeq = 0;
    BufferedWriter outputHmmFile = null;
    File hmm = new File(newHmm);
    File obsFile = new File(obs);
    
    //do forward algorithm like we did in recognize
    for (int k = 0; k < numStates; k++) {
      probs[k][0] = HMM[k].pi * vocabmatch(words[0], k, vocab, HMM[k].B);
    }
    for (int t = 1; t < words.length; t++) {
      for (int j = 0; j < numStates; j++) {
        sum = 0;
        for (int i = 0; i < numStates; i++) {
          sum += probs[i][t-1]*HMM[i].A[j];
        }
        probs[j][t] = sum*vocabmatch(words[t], j, vocab, HMM[j].B);
      }
    }
    //print the first probability
    prob = probs[0][seqLen-1] + probs[1][seqLen-1] + probs[2][seqLen-1] + probs[3][seqLen-1];
    if (prob == 0.0) {
      System.out.println(prob);
    } else {
      DecimalFormat df = new DecimalFormat("#.######");
      System.out.print(df.format(prob) + "      ");
    }
  
    //Get the backwards probabilities
    for(int state=0; state<numStates; state++){
      backwardProbs[state][seqLen-1] = 1;
    }

    //do the induction step for every time
    for(int t=seqLen-2; t>=0; t--){
      for(int state=0; state<numStates; state++){
        sum = 0.0; //reset the summation every time.
        for(int j=0; j<numStates; j++){
          double aij = HMM[state].A[j]; //get a_ij
          double bj = vocabmatch(words[t+1], j, vocab, HMM[j].B); //get b_j
          double betaj = backwardProbs[j][t+1]; //get betaj
          sum += aij * bj * betaj; 
        }
        backwardProbs[state][t] = sum;
      }
    }   
    
    for (int t = 0; t < seqLen+1; t++) {
      PrOlamda = 0; //Reset the summation every time
      for (int x = 0; x < numStates; x++) {
        PrOlamda += probs[x][t]*backwardProbs[x][t]; //here we initialize Pr(O|Lambda)
      }
      for (int i = 0; i < numStates; i++) {    //using the forwardbackwards variables, we compute for gamma
        gamma[i][t] = (probs[i][t]*backwardProbs[i][t])/PrOlamda; // alpha*beta/Pr(O|Lambda)
      }     
    }
    
    
    for (int t = 0; t < seqLen-1; t++) {
      sum = 0; //Reset the summation every time
      for (int i = 0; i < numStates; i++) {
        for (int j = 0; j < numStates; j++) {
          double aij = HMM[i].A[j]; //get a_ij
          double bj = vocabmatch(words[t+1], j, vocab, HMM[j].B); //get b_j
          double betaj = backwardProbs[j][t+1]; //get betaj
          sum += probs[i][t]*aij*bj*betaj;
        }
      }
      for (int i = 0; i < numStates; i++) {
        for (int j = 0; j < numStates; j++) {
          //calculate the xi for each state
          double aij = HMM[i].A[j]; //get a_ij
          double bj = vocabmatch(words[t+1], j, vocab, HMM[j].B); //get b_j
          double betaj = backwardProbs[j][t+1]; //get betaj
          xis[t][i][j] = (probs[i][t]*aij*bj*betaj)/sum;  // alphai*betaj*b_j*a_ij
        }
      }
    }
    //calculating the new pi values
    for (int i = 0; i < numStates; i++) {
      newpi[i] = gamma[i][0]; //new{pi}_i = gamma_t(i) 
    }
    
    //we now want to calculate the new A values
    //new{a}_ij = sum_t=1^T-1 xi_t(i,j) / sum_t=1^T-1 gamma_t(i) 
    for (int i = 0; i < numStates; i++) {
      sumgamma = 0;
      for (int x = 0; x < seqLen-1; x++) {
        sumgamma += gamma[i][x]; //sum_t=1^T-1 gamma_t(i) 
      }
      for (int j = 0; j < numStates; j++) {
        sumxi = 0;
        for (int t = 0; t < seqLen-1; t++) {
          sumxi += xis[t][i][j]; //sum_t=1^T-1 xi_t(i,j) 
        }
        if (sumgamma == 0.0) {
          newA[i][j] = HMM[i].A[j]; //if the sum of the gammas is 0, use the old alpha value
        } else {
          newA[i][j] = (sumxi/sumgamma);  //new alphas = sum of xi / sum of gammas
        }
      }
    }
    
    //we now want to calculate the new B values
    //new{b}_j(k) = sum_t=1^T-1 gamma_t(j) 1_{o_t = k}/sum_t=1^T-1 gamma_t(j) 
    for (int j = 0; j < numStates; j++) {
      for (int k = 0; k < syms; k++) {
        sumgammaeq = 0;
        sumgamma = 0;
        for (int t = 0; t < seqLen; t++) {
          if (words[t].equals(vocab[k])) {
            sumgammaeq += gamma[j][t]; //sum_t=1^T-1 gamma_t(j) 1_{o_t = k}
          }
          sumgamma += gamma[j][t]; //sum_t=1^T-1 gamma_t(j)
        }
        if (sumgamma == 0.0) {
          newB[k][j] = HMM[j].B[k];  ////if the sum of the gammas is 0, use the old beta value
        } else {
          newB[k][j] = (sumgammaeq/sumgamma); //new betas = sum of gammas when matching words / sum of gammas
        }
      }
    }
    
    //We now want to write into a new file
    
    try{
      outputHmmFile = new BufferedWriter(new FileWriter(newHmm));
      //Write out the number of states, vocab length and observation count 
      outputHmmFile.write(String.format("%d %d %d\n", numStates, vocab.length, seqLen)); 
      for (int i = 0; i < numStates; i++){
        outputHmmFile.write(HMM[i].name + " "); //Write the names of each HMM state
      }
      outputHmmFile.write("\n");
      for (int i = 0; i < vocab.length; i++){
        outputHmmFile.write(vocab[i] + " ");  //Write the vocab words
      }
      outputHmmFile.write("\n");
      outputHmmFile.write("a:\n");
      for (int i = 0; i < numStates; i++) {
        for (int j = 0; j < numStates; j++) {
          outputHmmFile.write(String.format("%.2f ", newA[i][j])); //Write the newly calculated As
        }
        outputHmmFile.write("\n");
      }
      outputHmmFile.write("b:\n");
      for (int j = 0; j < numStates; j++) {
        for (int k = 0; k < syms; k++) {
          outputHmmFile.write(String.format("%.2f ", newB[k][j])); //Write the newly calculated Bs
        }
        outputHmmFile.write("\n");
      }
      outputHmmFile.write("pi:\n");
      for (int i = 0; i < numStates; i++) {
        outputHmmFile.write(String.format("%.2f ", newpi[i])); ////Write the newly calculated Pis
      }
      outputHmmFile.close();
    } catch (IOException e){}
    
    //Run the recognize command for the new hmm file and the obs file
    try{
      scanFile(hmm, obsFile, "", "recognize");
    }catch (FileNotFoundException e){
      System.out.println("One or more files not found. Please try again.");
    }
  }
  
  //This method gets the beta value of the observation in the vocab array
  public static double vocabmatch(String obs, int state, String[] vocab, double[] B) {
    for (int i = 0; i < vocab.length; i++) {
      if (obs.equals(vocab[i])) {   
        return B[i]; //finds the Beta of the observation when it matches the vocab
      }
    }
    return 0.0;
  }

  //State is a helper class used to store each state of the HMM. An HMM is basically an array of states
  public static class State {
    
    String name;  //state name
    int num;   //state number
    double A[];   //transition probabilities
    double B[];   //observing probabilities
    double pi;
    
    public State(String n, int x, String a, String b, double p){
      name = n;
      num = x;
      
      String splitA[] = a.split(" "); //split the As up
      A = new double[splitA.length];
      
      for(int i=0; i<splitA.length; i++){
        A[i] = Double.parseDouble(splitA[i]); //convert the As from strings to doubles
      }
      
      String splitB[] = b.split(" "); //split the Bs up
      B = new double[splitB.length];
      
      for(int j=0; j<splitB.length; j++){
        B[j] = Double.parseDouble(splitB[j]); //convert the Bs from strings to doubles
      }
      
      pi = p;  //set the pi
    }
  }
  
}