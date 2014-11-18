

public class MainParser {
	
	
	public static void main(String[] a) {
		Formatter f1 = new Formatter("test2.txt");
		Formatter f2 = new Formatter("test3Part2.txt");
		f1.Format();
		f2.Format();
		
		Hasher h = new Hasher();
		
		h.getDiff(f1.getAsserts(), f2.getAsserts());
		
		ChangeAnalyzer c = new ChangeAnalyzer();
		
		c.Analyze(h.possibleDeletions, h.possibleInsertions);
	}
}
