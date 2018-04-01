import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner; 

public class searchEngine {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		String arg1 = args[0]; // "/Users/yash/eclipse-workspace/Hwk1"
	
		HashMap<Integer, Extract> meta = new HashMap<Integer, Extract>();
		File file1 = new File("temp");  
		FileInputStream f1 = new FileInputStream(arg1 + "/metadata.txt");  
		ObjectInputStream s1 = new ObjectInputStream(f1);  
		meta = (HashMap<Integer, Extract>)s1.readObject();         
		s1.close();
		
		HashMap<Integer, ArrayList<Integer>> invertedIndex = new HashMap<Integer, ArrayList<Integer>>();
		File file2 = new File("temp");  
		FileInputStream f2 = new FileInputStream(arg1 + "/invertedIndex.txt");  
		ObjectInputStream s2 = new ObjectInputStream(f2);  
		invertedIndex = (HashMap<Integer, ArrayList<Integer>>)s2.readObject();         
		s2.close();
		
		HashMap<String, Integer> lexicon = new HashMap<String, Integer>();
		File file3 = new File("temp");  
		FileInputStream f3 = new FileInputStream(arg1 + "/lexicon.txt");  
		ObjectInputStream s3 = new ObjectInputStream(f3);  
		lexicon = (HashMap<String, Integer>)s3.readObject();         
		s3.close();
		
 	    HashMap<Integer, String> tokens = new HashMap<Integer, String>();
 	    ArrayList<Integer> queryTokenID = new ArrayList<Integer>();
 	    ArrayList<Integer> topicNumbers = new ArrayList<Integer>();
 	    HashMap<Integer, Double> queryResult = new HashMap<Integer, Double>(); 	    
 	    ArrayList<Double> singleBM25Scores = new ArrayList<Double>(); 	    
 	    ArrayList<Integer> postingsList = new ArrayList<Integer>();
 	    
 	    String newLine = System.getProperty("line.separator");
 	    
 	    double avgDocLength = getAvgDocLength(meta);	    
	    int docsInCollection = docsInCollection(meta);
 	    
 	    String results = "";
 	    
 	   	boolean search = true;
 	   	
 	    Scanner queryInput = new Scanner(System.in); //initialize scanner for guesses
 	  
 	    System.out.println("Welcome to Yash's Fire Search Engine");
 	    while(search) {	
 	    	if(queryInput.nextLine().equals("N") || queryInput.nextLine().equals("new query")) {
				System.out.println("Enter your query");
				String query = queryInput.nextLine();
				Stopwatch time = new Stopwatch();
				
				//show all results
				int rank = 1;
				    
				         
				ArrayList<String> queryTokens = Tokenize(query);
				queryTokenID = convertTokensToIDs(queryTokens, lexicon); //arraylist of the int value of the word 
				       	         
				queryResult = BM25(queryTokenID, invertedIndex, meta, avgDocLength, docsInCollection); //this method retrives the scores based on the query search
				         
				HashMap<Integer, Double> sorted = sortByValues(queryResult);
				         
				ArrayList<Double> sortedList = new ArrayList<Double>();
				         
				for (Integer name: sorted.keySet()){
					sortedList.add((double)name);
				  	sortedList.add(sorted.get(name)); 	 
				}	      
				
				int upperBound = 20;
				for(int i = 0; i < upperBound; i = i + 2) { //go through each docid returned as the result set to retrieve the required info
					if(sortedList.size() < 20) {
						upperBound = sortedList.size();
				    		
				    }
				        	    	 
					if(sortedList.size() == 0) {
						break;
					}
					        	 
					int docid = sortedList.get(i).intValue();
					String docno = meta.get(docid).getDocNo(); //get the docno from the docid at i 
					String strippedDoc = stripTags((getDoc(docno, meta)));
					ArrayList<snippet> sentences = getSentence(strippedDoc);
					snippet querySentenceTokens = new snippet(query);
					  		  	
					for(int j = 0; j < sentences.size(); j ++) {
						sentences.get(j).addC(computeC(querySentenceTokens.getTokens(), sentences.get(j).getTokens()));		
					}
					
					String snippet = "";
					int bound = 10;
					for(int j = 0; j < bound; j ++) {
						if(sentences.size() < 10) {
							bound = sentences.size();
					  	}
						if(sentences.get(j).getC() > 0) {
							snippet = snippet + sentences.get(j).getSentence() + newLine;
						}
					}
					  	 		
					String date = meta.get(docid).getDate();
					String headline = meta.get(docid).getHeadline();
					if(headline.equals("")) {
						int bound2 = 50;
						for(int j = 0; j < bound2; j ++) {
							if(snippet.length() < 50) {
						  		bound = snippet.length()-1;
						  	}	  		
						  	headline = headline + snippet.charAt(j);	
						}
							headline = headline + " . . . ";
				    }
					results = results + rank + " " + headline + "(" + date + ") " + newLine + snippet + " (" + docno + ")" + newLine;		
					rank++;
				  	
				}
				     
				System.out.println(results);
				results = "";
				System.out.println("Retrieval took: " + time.elapsedTime() + " seconds");  
				   
				boolean wantToSee = true;
				while(wantToSee) {
					System.out.println("Enter the rank of document you would like to see or enter N to type new query or enter Q to quit");
					if(queryInput.hasNextInt()) {
						int rankToSee = queryInput.nextInt();
						int docId = sortedList.get(getDocId(rankToSee)).intValue();
						String docno = meta.get(docId).getDocNo();
						String docToSee = getDoc(docno, meta);
						System.out.println(docToSee);
					}
					else {
						wantToSee = false;					   
					}
				 }
 	    	}
			
 	    	if(queryInput.nextLine().equals("Q") || queryInput.nextLine().equals("Quit")) {
				search = false;
			}	
		}
 	    
 	    System.out.println("Thanks for using this search engine!");
	    queryInput.close();
 	   	   
	}
	
	public static int getDocId(int rank) {
		int count = -2;
		int result = 0;
		for(int i = 0; i < 11; i++) {
			if(i == rank) {
				result = rank + count;
				break;
			}
			count++;	
		}
		return result;	
	}
	
	public static ArrayList<Integer> convertTokensToIDs(ArrayList<String> tokens, HashMap<String, Integer> lexicon) {
		ArrayList<Integer> tokenIDs = new ArrayList<Integer>();
		for(int i = 0 ; i < tokens.size(); i ++) {
			if(lexicon.containsKey(tokens.get(i))){
				tokenIDs.add(lexicon.get(tokens.get(i)));
			}
		}
		return tokenIDs;	
	}
	
	public static ArrayList<String> Tokenize(String text) {
		text = text.toLowerCase();
		ArrayList<String> tokens = new ArrayList<String>();
		int start = 0;
		int i;
		for(i = 0; i < text.length()-1; i++) {
			String c = Character.toString(text.charAt(i));
			if(!(c.matches("[A-Za-z0-9]+"))) {
				if(start != i) {
					String token = text.substring(start, i);
					tokens.add(token);
				}
				start = i + 1;
			}
		}
		if(start != i) {
			tokens.add(text.substring(start, i + 1));
		}
		return tokens;
	}
	
	//code from https://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
		private static HashMap sortByValues(HashMap map) { 
		       LinkedList list = new LinkedList(map.entrySet());
		       // Defined Custom Comparator here
		       Collections.sort(list, new Comparator() {
		            public int compare(Object o1, Object o2) {
		               return ((Comparable) ((Map.Entry) (o2)).getValue())
		                  .compareTo(((Map.Entry) (o1)).getValue());
		            }
		       });

		       // Here I am copying the sorted list in HashMap
		       // using LinkedHashMap to preserve the insertion order
		       HashMap sortedHashMap = new LinkedHashMap();
		       for (Iterator it = list.iterator(); it.hasNext();) {
		              Map.Entry entry = (Map.Entry) it.next();
		              sortedHashMap.put(entry.getKey(), entry.getValue());
		       } 
		       return sortedHashMap;
		  }
		
		public static double getAvgDocLength(HashMap<Integer, Extract> metadata) {
			int count = 0;
			double sum = 0;
			for (Integer docid: metadata.keySet()){
				 count++;
		  		 sum = sum + metadata.get(docid).getLength();
			}
			return sum/count;
		}
		
		public static double log(int x, int base){
			return (Math.log(x) / Math.log(base));
		}
		
		public static int docsInCollection(HashMap<Integer, Extract> metadata) {
			int count = 0;
			for (Integer docid: metadata.keySet()){
				 count++;
			}
			return count;
		}
			
		public static int getFrequencyOfTermInQuery(ArrayList<Integer> queryTokenID, int termi) {
			int count = 0;
			for(int j = 0; j < queryTokenID.size(); j++) {	
				if(queryTokenID.get(j).equals(termi)) {
					count++;
				}	
			}
			return count;
		}
		
		public static HashMap<Integer, Double> BM25(ArrayList<Integer> queryTokenID, HashMap<Integer, ArrayList<Integer>> invertedIndex, HashMap<Integer, Extract> metadata, double avgDocLength, int docInCollection){
			HashMap<Integer, Integer> docCount = new HashMap<Integer, Integer>();
			HashMap<Integer, Double> resultSet = new HashMap<Integer, Double>();
			ArrayList<Integer> postingsList = new ArrayList<Integer>();
			double sum = 0;		
		
			for(int i = 0; i < queryTokenID.size(); i ++) { 
				int freqOfTermInQuery = getFrequencyOfTermInQuery(queryTokenID, queryTokenID.get(i));
				postingsList = invertedIndex.get(queryTokenID.get(i)); //for a particular word, the docid and the number of times it appears
	        	int numberOfDocWithTermi = postingsList.size()/2;
	      
	        	for(int j = 0; j < postingsList.size(); j = j + 2) { //iterating through the the document ids that the term appears in	
	        		int frequencyOfTermj = postingsList.get(j + 1);
	        		int lengthOfDocj = metadata.get(postingsList.get(j)).getLength();
	        		double k = 1.2*(0.25 + 0.75*(lengthOfDocj/avgDocLength));
	        		double part1 = ((2.2*frequencyOfTermj)/(k + frequencyOfTermj))*((8*freqOfTermInQuery)/(7 + freqOfTermInQuery));
	        		double part2 = ((8*freqOfTermInQuery)/(7 + freqOfTermInQuery));
	        		double part3a = (docInCollection-numberOfDocWithTermi + 0.5)/(numberOfDocWithTermi + 0.5);
	        		double part3b = Math.log(part3a);
	        		double result = part1*part2*part3b;
	        		if(resultSet.containsKey(postingsList.get(j))) {
	        			resultSet.replace(postingsList.get(j), resultSet.get(postingsList.get(j))+ result);
	        		}
	        		else {
	        			resultSet.put(postingsList.get(j), result);
	        		}
	        	 }	  
			 }
			return resultSet;	
		}
		
		public static String getDoc(String docNo, HashMap<Integer, Extract> meta) throws IOException {
			String result = "";
			String newLine = System.getProperty("line.separator");
			for (Integer intId: meta.keySet()){	
				if(docNo.equals(meta.get(intId).getDocNo())) { //if looking for docno
					String folder = changeMMDDYYToYYMMDDFromDocNo(meta.get(intId).getDocNo());
			       	int doc = intId;
			 		FileReader fileReader = new FileReader("C:\\Users\\yash\\eclipse-workspace\\latimes\\" + folder + "\\" + doc + ".txt");
			 	    String line = "";       
			 	    BufferedReader br = new BufferedReader(fileReader);
			 	    while((line = br.readLine()) != null) {
			 	         result = result + line + newLine;
			 	    }   
			 	    br.close(); 	 
		        }                             
			}
			return result;
		}
		
		public static String changeMMDDYYToYYMMDDFromDocNo(String line) { //MMDDYY TO YYMMDD
			String mm = line.substring(2, 4);	
			String dd = line.substring(4, 6);
			String yy = line.substring(6, 8);
			String result = yy + mm + dd;
			return result;
		}
		
		public static double computeC(String[] queryTokens, String [] tokens){
			double c = 0;
			for(int i = 0; i < queryTokens.length; i ++) {
				for(int j = 0; j < tokens.length; j++) {
					if(queryTokens[i].equalsIgnoreCase(tokens[j])) {
						c++;
					}
				}
			}
			return c;
		}
		
		//code from stackoverflow for spliting doc into sentences on periods
		public static ArrayList<snippet> getSentence(String strippedDoc) {
			ArrayList<snippet> sentences = new ArrayList<snippet>();
			BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
			String source = strippedDoc;
			iterator.setText(source);
			int start = iterator.first();
			for (int end = iterator.next();
			    end != BreakIterator.DONE;
			    start = end, end = iterator.next()) {
				snippet snip = new snippet(source.substring(start,end));
				if(snip.getTokens().length < 5) {
					continue;
				}
				sentences.add(snip);
			}
			return sentences;
		}
		
		public static String stripTags(String doc) throws IOException {
			   String result = "";	
			   StringReader reader = new StringReader(doc);
			   BufferedReader br = new BufferedReader(reader);
			   String line;
			   line=br.readLine();
			   line=br.readLine();
			   line=br.readLine();
			   line=br.readLine();
			   line=br.readLine();
			   boolean executed = false;	   
			   while((line=br.readLine())!=null) {	   
				   if(startCollecting(line) || executed) {
					   executed = true;
					   if(containsTag(line)) {
						   continue;  			
					   }
					   else {
						   result = result + line;	
					   }
				   }   			    	    
			   }
			   return result;
		}
		
		public static boolean containsTag(String line) {	
			boolean result = false;
			if(line.contains("<DATELINE>") || line.contains("</DATELINE>") || line.contains("<DOCID>") || line.contains("<DOC>") || line.contains("<DATE>") || line.contains("<P>") || line.contains("<SECTION>") || line.contains("<LENGTH>")|| line.contains("<HEADLINE>")|| line.contains("<BYLINE>")|| line.contains("<TYPE>") || line.contains("</DOC>") || line.contains("</DATE>") || line.contains("</P>") || line.contains("</SECTION>") || line.contains("</LENGTH>")|| line.contains("</HEADLINE>")|| line.contains("</BYLINE>")|| line.contains("</TYPE>") || line.contains("<DOCNO>")) {	
				result = true;
			}
			return result;
		}
		
		public static boolean startCollecting(String line) {
			boolean result = false;
			if(line.contains("</LENGTH>")) {
				result = true;
			}
			return result;
		}	
}
