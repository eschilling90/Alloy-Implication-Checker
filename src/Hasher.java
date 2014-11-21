import java.util.ArrayList;
import java.util.List;


public class Hasher {

	List<String> possibleDeletions = new ArrayList<String>();
	List<String> possibleInsertions = new ArrayList<String>();
	List<String> unmodifiedLines = new ArrayList<String>();
	
	public void getDiff(List<String> oldFile, List<String> newFile) {
		possibleDeletions = new ArrayList<String>(oldFile);
		possibleDeletions.removeAll(newFile);
		possibleInsertions = new ArrayList<String>(newFile);
		possibleInsertions.removeAll(oldFile);
		unmodifiedLines = new ArrayList<String>(oldFile);
		unmodifiedLines.retainAll(newFile);
		System.out.println("Common code");
		System.out.println(unmodifiedLines.toString().replace(',', '\n'));
		System.out.println("<<<<<<< Deletions");
		System.out.println(possibleDeletions.toString().replace(',', '\n'));
		System.out.println("Insertions >>>>>>>");
		System.out.println(possibleInsertions.toString().replace(',', '\n'));
		System.out.println();
	}
}
