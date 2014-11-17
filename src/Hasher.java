import java.util.ArrayList;
import java.util.List;


public class Hasher {

	List<String> possibleDeletions = new ArrayList<String>();
	List<String> possibleInsertions = new ArrayList<String>();
	
	public void getDiff(List<String> oldFile, List<String> newFile) {
		possibleDeletions = new ArrayList<String>(oldFile);
		possibleDeletions.removeAll(newFile);
		possibleInsertions = new ArrayList<String>(newFile);
		possibleInsertions.removeAll(oldFile);
		System.out.println(possibleDeletions);
		System.out.println(possibleInsertions);
		System.out.println("");
	}
}
