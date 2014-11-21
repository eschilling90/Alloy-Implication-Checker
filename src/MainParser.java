import java.io.IOException;

import org.json.JSONException;



public class MainParser {
	
	
	public static void main(String[] a) {
		final long startTime = System.currentTimeMillis();
		Formatter f1 = new Formatter("test2.txt");
		Formatter f2 = new Formatter("test2Part2.txt");
		f1.Format();
		f2.Format();
		
		Hasher h = new Hasher();
		
		h.getDiff(f1.getAsserts(), f2.getAsserts());
		
		ChangeAnalyzer c = new ChangeAnalyzer();
		
		try {
			c.Analyze(h.possibleDeletions, h.possibleInsertions);
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Integer unchanged = c.operationsCounter(h.unmodifiedLines);
		Integer changed = c.SMTSavings;
		Integer rerun = c.SMTMisses;
		Double total = (double) (unchanged + changed + rerun) / 100;
		
		final long endTime = System.currentTimeMillis();
		
		System.out.println();
		System.out.println("For models that already exist from the previous execution, the tool presents:");
		System.out.printf("Total operation savings from unchanged lines: %s, or %s%%\n", unchanged, unchanged / total);
		System.out.printf("Total operation savings from changed lines: %s, or %s%%\n", changed, changed / total);
		System.out.printf("Total operations that need to be rerun: %s, or %s%%\n", rerun, rerun / total);
		System.out.println();
		System.out.printf("Total execution time: %s ms", (endTime - startTime) );
	}
}
