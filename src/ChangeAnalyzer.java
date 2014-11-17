import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeAnalyzer {
	
	List<List<String>> deletionWords;
	List<List<String>> insertionWords;
	
	public List<List<String>> listConstructor(List<String> lines){
		List<List<String>> wordList = new ArrayList<List<String>>();
		for (String line : lines){
			List<String> wordsInLine = new ArrayList<String>();
			for (String word : line.split(" ")){
				wordsInLine.add(word);
			}
			wordList.add(wordsInLine);
		}
		return wordList;
	}
	
	public Boolean checkRelaxation(String deletion, String insertion){
		return null;
		
	}
	
	public Boolean checkImplication(String deletion, String insertion){
		return null;
		
	}
	
	public void Analyze(List<String> possibleDeletions, List<String> possibleInsertions) {
		// Loop over insertions and deletions, split by words
		// 1) compare all words in code to implication table to find operators
		// 2) Create entry of operator and operand(s) in insertion/deletion line entry
		// 3) Use implication table to determine lines that possibly match by checking if each operator in a deleted line
		// is in the implication set of a inserted line. Then check if the deletion is a subset of the insertion for each operator in the line.
		// 3a) if the previous condition is true, remove both the deletion and insertion and mark the line in question as implied
		// 3b) if the previous condition is false, add one to the SMT execution statement counter for each non-implied operator, then mark for insertion/deletion
		
		deletionWords = listConstructor(possibleDeletions);
		insertionWords = listConstructor(possibleInsertions);
		System.out.println(deletionWords);
		System.out.println(insertionWords);
		
		FileInputStream fis = new FileInputStream("Implication-Table.txt");
		ObjectInputStream ois = new ObjectInputStream(fis);
		byte[] read = (byte[]) ois.readObject();
		String line = new String(read);
		
		Map<String, List<String>> implicationTable = new HashMap<String, List<String>>();
		
		//for(String key: line){
			
		//}
	}
}