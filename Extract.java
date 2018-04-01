import java.io.Serializable;

public class Extract implements Serializable{ //serialization was learned from this link https://www.geeksforgeeks.org/serialization-in-java/
	
	public String docNo;
	public String headline;
	public String date;
    public int length;
	
	public Extract(String docNo, String date, String headline, int length) {
		
		this.docNo = docNo;
		this.headline = headline;
		this.date = date; 
        this.length = length;
                
		
	}
	
	public void setDocNo(String docNo){
	    this.docNo = docNo;
	}
	
	public void setHeadline(String headline){
		this.headline = headline;
	}
	
	public void setDate(String date){
		this.date = date; 
	}
        
        public void setLength(int length){
		this.length = length;
	}

	
	public String getDocNo(){
	   return docNo;
	}
	
	public String getHeadline(){
		return headline;
	}
	
	public String getDate(){
		return this.date;
	}
        
    public int getLength(){
		return this.length;
	}

}
