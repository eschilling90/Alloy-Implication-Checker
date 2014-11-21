import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.json.*;

public class ChangeAnalyzer {
	
	LineList deletion;
	LineList insertion;
	JSONObject implicationTable;
	Integer SMTSavings = 0;
	Integer SMTMisses = 0;
	
	public class LineList {
		private List<List<String>> wordList;
		private List<JSONObject> operatorList;
		private List<Integer> lineNumbers;
		
		public LineList(List<List<String>> wlist,
				List<JSONObject> oplist, List<Integer> lineNums) {
			wordList = wlist;
			operatorList = oplist;
			lineNumbers = lineNums;
		}
	}
	
	public Integer operationsCounter(List<String> lines){
		LineList lineList = listConstructor(lines);
		return lineList.operatorList.size();
	}
	
	public Integer operationsCounter(LineList lineList){
		return lineList.operatorList.size();
	}
	
	public LineList listConstructor(List<String> lines){
		List<List<String>> wordList = new ArrayList<List<String>>();
		List<JSONObject> operatorList = new ArrayList<JSONObject>();
		List<Integer> lineNumbers = new ArrayList<Integer>();
		Integer i = 0;
		Pattern pattern = Pattern.compile("[ {}:|]");
		for (String line : lines){
			List<String> wordsInLine = new ArrayList<String>();
			JSONObject operatorsInLine = new JSONObject();
			String[] splitLine = pattern.split(line);
			Integer index = 0;
			for (String word : splitLine){
				wordsInLine.add(word);
				if(implicationTable.has(word)){
					List<String> operands = new ArrayList<String>();
					switch (implicationTable.getJSONObject(word).getInt("operands")){
						case 1:
							operands.add(splitLine[index + 1]);
							operatorsInLine.append("operands", operands);
							break;
						case 2:
							operands.add(splitLine[index - 1]);
							operands.add(splitLine[index + 1]);
							operatorsInLine.append("operands", operands);
							break;
					}
					operatorsInLine.append("operator", word);
				}
				index += 1;
			}
			operatorList.add(operatorsInLine);
			wordList.add(wordsInLine);
			lineNumbers.add(i);
			i += 1;
		}
		return new LineList(wordList, operatorList, lineNumbers);
	}
	
	public Boolean checkRelaxation(String deletion, String insertion){
		JSONArray implications = implicationTable.getJSONObject(insertion).getJSONArray("implies");
		if(implications.toString().contains(deletion)){
			return true;
		} else {
			return false;
		}
	}
	
	public static Integer tryParse(String text) {
	  try {
	    return new Integer(text);
	  } catch (NumberFormatException e) {
	    return null;
	  }
	}
	
	public Boolean checkImplication(String deletion, List<String> deletionOperands, String insertion, List<String> insertionOperands){
		if(insertionOperands.size() != deletionOperands.size()){
			return false;
		}
		Boolean implied = true;
		JSONArray implications = implicationTable.getJSONObject(insertion).getJSONArray("implies");
		int i;
		for(i = 0; i < implications.length(); i++){
			if(implications.getString(i).equals(deletion)){
				break;
			}
		}
		if(i == implications.length()){
			return false;
		}
		String matchEq = implicationTable.getJSONObject(insertion).getJSONArray("matches").getString(i);
		if(matchEq.equals("exact")){
			for(int j=0; j < insertionOperands.size(); j++) {
				if(!deletionOperands.get(j).equals(insertionOperands.get(j))){
					implied = false;
				}
			}
		} else {
			for(int j=0; j < insertionOperands.size(); j++){
				String operand = insertionOperands.get(j);
				Integer literal = tryParse(operand);
				if(literal != null){
					Integer delLiteral = tryParse(deletionOperands.get(j));
					if(delLiteral != null){
						switch(matchEq) {
							case ">":
								if(literal <= delLiteral){
									implied = false;
								}
								break;
							case ">=":
								if(literal < delLiteral){
									implied = false;
								}
								break;
							case "==":
								if(literal != delLiteral){
									implied = false;
								}
								break;
							case "<":
								if(literal >= delLiteral){
									implied = false;
								}
								break;
							case "<=":
								if(literal > delLiteral){
									implied = false;
								}
								break;
						}
					} else {
						implied = false;
					}
				} else {
					if(!deletionOperands.contains(operand)){
						implied = false;
					}
				}
			}
		}
		return implied;

	}
	
	public void Analyze(List<String> possibleDeletions, List<String> possibleInsertions) throws JSONException, IOException {
		byte[] inputStream = Files.readAllBytes(Paths.get("Implication-Table.txt"));
		String tableString = new String(inputStream);
		implicationTable = new JSONObject(tableString);
		
		// Loop over insertions and deletions, split by words
		// 1) compare all words in code to implication table to find operators
		// 2) Create entry of operator and operand(s) in insertion/deletion line entry
		// 3) Use implication table to determine lines that possibly match by checking if each operator in a deleted line
		// is in the implication set of a inserted line. Then check if the deletion is a subset of the insertion for each operator in the line.
		// 3a) if the previous condition is true, remove both the deletion and insertion and mark the line in question as implied
		// 3b) if the previous condition is false, add one to the SMT execution statement counter for each non-implied operator, then mark for insertion/deletion
		
		insertion = listConstructor(possibleInsertions);
		deletion = listConstructor(possibleDeletions);
		
		Integer delLineNumber = 0;
		// For each line in the deletion candidates
		for(JSONObject deleteLine: deletion.operatorList){
			Boolean lineMatch = false;
			JSONObject matchedLine = null;
			List<List<Integer>> skipList = new ArrayList<List<Integer>>();
			// For each operator in each line of the deletion candidates
			for(int delIndex=0; delIndex < deleteLine.getJSONArray("operator").length(); delIndex++){
				String deletionLineOperator = (String) deleteLine.getJSONArray("operator").get(delIndex);
				List<String> deletionLineOperands = (List<String>) deleteLine.getJSONArray("operands").get(delIndex);
				// For each operand in each operator of the deletion candidates
				// Check to see if each statement is implied by an insertion candidate
				for(JSONObject insertLine: insertion.operatorList){
					Integer lineNumber = insertion.operatorList.indexOf(insertLine);
					if(skipList.size() <= lineNumber) skipList.add(new ArrayList<Integer>());
					for(Integer insIndex=0; insIndex < insertLine.getJSONArray("operator").length(); insIndex++){
						if(skipList.get(lineNumber).contains(insIndex)) continue;
						String insertionLineOperator = (String) insertLine.getJSONArray("operator").get(insIndex);
						Boolean result = checkRelaxation(deletionLineOperator, insertionLineOperator);
						if(!result) continue;
						List<String> insertionLineOperands = (List<String>) insertLine.getJSONArray("operands").get(insIndex);
						result = checkImplication(deletionLineOperator, deletionLineOperands, insertionLineOperator, insertionLineOperands);
						if(result) {
							System.out.printf("%s, %s is implied by: %s, %s\n", deletionLineOperator, deletionLineOperands, insertionLineOperator, insertionLineOperands);
							skipList.get(lineNumber).add(insIndex);
							break;
						}
					}
				}
				for(List<Integer> line: skipList) {
					if(line.size() == deleteLine.getJSONArray("operator").length()){
						lineMatch = true;
						matchedLine = insertion.operatorList.get(skipList.indexOf(line));
					}
				}
			}
			if(lineMatch){ // Implied!
				SMTSavings += deleteLine.getJSONArray("operator").length();
				System.out.println("match:");
				System.out.println(matchedLine);
				System.out.println(deleteLine);
				System.out.println();
				Integer insLineNumber = insertion.operatorList.indexOf(matchedLine);
				int index = insertion.lineNumbers.get(insLineNumber);
				possibleInsertions.remove(index);
				insertion.lineNumbers.remove(insLineNumber);
				index = deletion.lineNumbers.get(delLineNumber);
				possibleDeletions.remove(index);
				insertion.operatorList.remove(matchedLine);
				insertion.wordList.remove(insLineNumber);
				delLineNumber -= 1;
			}
			delLineNumber += 1;
		}
		
		System.out.println();
		System.out.printf("Unmatchable Deleted lines: %s\n",possibleDeletions);
		System.out.printf("Unmatchable Inserted lines: %s\n",possibleInsertions);
		SMTMisses = operationsCounter(possibleInsertions) + operationsCounter(possibleDeletions);
	}
}