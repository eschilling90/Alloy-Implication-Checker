import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class Formatter {
	
	String filename = "";
    private boolean inComment = false;
    private int numBracket = 0;
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
		FileReader fr;
		BufferedReader br = null;
		try {
			fr = new FileReader(filename);
			br = new BufferedReader(fr);
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	        	line = trimComments(line);
	        	line = trimExtraWhitespace(line);
	        	line = inBrackets(line);
	        	if (!line.equals("")) {
		            sb.append(line);
		            sb.append(System.lineSeparator());
		            assertList.add(line);
	        	}
		        line = br.readLine();
	        }
	        String everything = sb.toString();
	        System.out.println(everything);
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
