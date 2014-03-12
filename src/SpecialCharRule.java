
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecialCharRule {
	
	public String apply(String token) {
		// TODO Auto-generated method stub
		try{
			//token = stream.next();
			List<String> endResult = new ArrayList<String>() ;
			String  result = token.replaceAll("[~!@#%^&\\*\\(\\)=+:;\\|\\>\\</\"\\[\\]\\}\\{]","");
			if(result.length() > 1){
				return result;
			}
			if(endResult.size() > 0){
				//stream.previous();
				//stream.set(endResult.toArray(new String[endResult.size()]));
				//stream.next();				
				endResult.toString();
				}
			else if(token.trim().isEmpty())
			{
				return token;
			}
		}
			catch(IndexOutOfBoundsException e){
				String s1q = "ajahsjdh";	
				}
		return token;
		}
	}

