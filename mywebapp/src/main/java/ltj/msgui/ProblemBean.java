package ltj.msgui;
import java.util.ArrayList;

public class ProblemBean {
   private ArrayList<Integer> sequence; 
   private int solution;
   
   public ProblemBean() {}

   public ProblemBean(int[] values, int solution) {
      sequence = new ArrayList<Integer>();
      for (int i = 0; i < values.length; i++)
         sequence.add(values[i]);
      this.solution = solution;
   }

   // PROPERTY: sequence
   public ArrayList<Integer> getSequence() { return sequence; }
   public void setSequence(ArrayList<Integer> newValue) { sequence = newValue; }

   // PROPERTY: solution
   public int getSolution() { return solution; }
   public void setSolution(int newValue) { solution = newValue; }
}
