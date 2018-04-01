import java.util.ArrayList;

public class snippet { //give
	
	public double c;
	public double d;
	public double k;
	public double l;
	public double score;
	public String fullSentence;
	public String [] tokensOfString;
	
	public snippet(String fullSentence) {
	
		this.fullSentence = fullSentence;
		this.tokensOfString = this.fullSentence.split("\\s+");
		
	}
	
	
	public String printTokens() {
		
		String result = "";
		
		for(int i = 0; i < this.tokensOfString.length; i ++) {
			
			result = result + " " + tokensOfString[i];
			
		}
		return result;
	}
	
	public String[] getTokens() {
		return this.tokensOfString;
	}
	
	public String getSentence() {
		return this.fullSentence;
	}
	
	public void addC(double c) {
		this.c = c;
	}
	
	public void addD(double d) {
		this.d = d;
	}
	
	public void addK(double k) {
		this.k = k;
	}
	
	public void addL(double l) {
		this.l = l;
	}
	
	public double getScore() {
		return this.score;
		
	}
	
	public void computeScore() {
		this.score = c + d + k + l;
	}
	
	public double getC() {
		return this.c;
	}
}
