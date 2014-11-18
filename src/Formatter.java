import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class Formatter {
	
	String filename = "";
    private boolean inComment = false;
    private int numBracket = 0;
    private List<String> fileStorage = new ArrayList<String>();
    private List<String> predList = new ArrayList<String>();
    private Map<String, ArrayList<String>> predCalls = new HashMap<String, ArrayList<String>>();
    private List<String> runCommands = new ArrayList<String>();
    private boolean inFact = false;
    private boolean inPred = false;
    private String curPredName = "";
    private Set<String> calledPredSet = new HashSet<String>();
    
    List<String> assertList = new ArrayList<String>();
    
    public Formatter (){
    	
    }
    
	public Formatter (String filename) {
		this.filename = filename;
	}
	
	public List<String> getAsserts() {
		return assertList;
	}
	
	public void Format () {
		getFileContents();
		getCalledPreds();
        StringBuilder sb = new StringBuilder();
        boolean inUncalledPred = true;
        int badPredBrackets = 0;
        for (String line : fileStorage) {
        	String predName = "";
        	if (line.startsWith("pred")) {
        		predName = getPredName(line);
        		if (!calledPredSet.contains(predName)) {
        			inUncalledPred = true;
        			badPredBrackets += line.length() - line.replace("{", "").length();
    	        	badPredBrackets -= line.length() - line.replace("}", "").length();
    	        	continue;
        		}
        	}
        	if (inUncalledPred) {
        		badPredBrackets += line.length() - line.replace("{", "").length();
	        	badPredBrackets -= line.length() - line.replace("}", "").length();
	        	if (badPredBrackets == 0) {
	        		inUncalledPred = false;
	        	}
        	} else {
	        	line = inBrackets(line);
	        	if (!line.equals("")) {
		            sb.append(line);
		            sb.append(System.lineSeparator());
		            assertList.add(line);
	        	}
        	}
        }
        String everything = sb.toString();
        System.out.println(everything);
        System.out.println(predList);
        System.out.println(runCommands);
        System.out.println(calledPredSet);
	}
	
	private void getCalledPreds() {
		for (String s : runCommands) {
			calledPredSet.add(s);
			calledPredSet.addAll(getPreds(s));
		}
	}

	private Collection<? extends String> getPreds(String s) {
		List<String> result = new ArrayList<String>();
		List<String> calls = predCalls.get(s);
		if (calls != null) {
			for (String c : calls) {
				if (!calledPredSet.contains(c)) {
					result.add(c);
					result.addAll(getPreds(c));
				}
			}
		}
		return result;
	}

	private void getFileContents() {
		FileReader fr;
		BufferedReader br = null;
		try {
			fr = new FileReader(filename);
			br = new BufferedReader(fr);
	        String line = br.readLine();
	        numBracket = 0;
	        while (line != null) {
	        	line = trimComments(line);
	        	line = trimExtraWhitespace(line);
	        	numBracket += line.length() - line.replace("{", "").length();
	        	numBracket -= line.length() - line.replace("}", "").length();
	        	if (numBracket == 0 && inPred) {
	        		inPred = false;
	        		curPredName = "";
	        	}
	        	if (numBracket == 0 && inFact) {
	        		inFact = false;
	        	}
	        	if (inPred && line.contains("[")) {
	        		ArrayList<String> callList = new ArrayList<String>();
	        		if (predCalls.containsKey(curPredName)) {
	        			callList = predCalls.get(curPredName);
	        		}
	        		String temp = line.substring(0,line.indexOf("["));
	        		if (temp.contains(" ")) {
	        			int lastSpace = temp.lastIndexOf(" ");
	        			temp = temp.substring(lastSpace+1,temp.length());
	        		}
	        		callList.add(temp.trim());
	        		predCalls.put(curPredName, callList);
	        	}
	        	if (inFact && line.contains("[")) {
	        		String temp = line.substring(0,line.indexOf("["));
	        		if (temp.contains(" ")) {
	        			int lastSpace = temp.lastIndexOf(" ");
	        			temp = temp.substring(lastSpace+1,temp.length());
	        		}
	        		runCommands.add(temp);
	        		if (!predCalls.containsKey(temp)) {
	        			predCalls.put(temp, new ArrayList<String>());
	        		}
	        	}
	        	if (line.startsWith("fact")) {
	        		inFact = true;
	        	}
	        	if (line.startsWith("pred")) {
	        		String name = getPredName(line);
	        		curPredName = name;
	        		inPred = true;
		            predList.add(name);
	        	}
	        	if (line.startsWith("run")) {
	        		String name = getRunName(line);
	        		runCommands.add(name);
	        		if (!predCalls.containsKey(name))
	        			predCalls.put(name, new ArrayList<String>());
	        	}
	        	fileStorage.add(line);
		        line = br.readLine();
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    } finally {
	    	try {
				if (br != null) br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}

	private String getRunName(String line) {
		return line.split(" ")[1];
	}

	String getPredName (String line) {
		String name = line.split(" ")[1];
		if (name.contains("(")) {
			return name.substring(0,name.indexOf("("));
		}
		return name;
	}
	
	String inBrackets (String line) {
		if (line.contains("{") && line.contains("}")) {
			int start = line.indexOf("{");
			int end = line.indexOf("}");
			if (numBracket > 0) {
				return line;
			}
			return line.substring(start+1,end);
		}
		if (line.contains("{")) {
			numBracket++;
			int start = 0;
			if (numBracket == 1) {
				start = line.indexOf("{")+1;
			}
			return line.substring(start,line.length());
		}
		if (line.contains("}")) {
			numBracket--;
			int start = line.indexOf("}");
			return line.substring(0,start);
		}
		if (numBracket > 0) {
			return line;
		} else {
			return "";
		}
	}
	
	String trimComments (String line) {
		if (line.contains("/*") && line.contains("*/")) {
			int start = line.indexOf("/*");
			int end = line.indexOf("*/") + 2;
			return line.substring(0,start) + line.substring(end,line.length());
		}
		if (line.contains("/*")) {
    		inComment = true;
    		int start = line.indexOf("/*");
    		return line.substring(0,start);
    	}
    	if (line.contains("*/")) {
    		inComment = false;
    		int start = line.indexOf("*/");
    		return line.substring(start+2, line.length());
    	}
    	if (line.contains("//")) {
    		int start = line.indexOf("//");
    		line = line.substring(0,start);
    	}
    	if (inComment) {
    		return "";
    	}
		return line;
	}
	
	String trimExtraWhitespace (String line) {
		return line.trim().replaceAll(" +", " ");
	}
	
	public static void main(String[] a) {
		Formatter f = new Formatter("test2.txt");
		f.Format();
	}
}
