import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.text.Normalizer;
import java.util.StringTokenizer;

public class AccentRule {
	public String apply(String token) {
		// TODO Auto-generated method stub
		if(1 == 1) {
			//String token;			
			try{
			if (1 == 1) {
				//token = stream.next();
				//if(!Normalizer.isNormalized(token, Normalizer.Form.NFD)){
					String s = token;
					s = Normalizer.normalize(s,Normalizer.Form.NFKD);
					s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
				    if(s.compareTo(token) != 0 ){
						s = s.toLowerCase();
						if(Character.isUpperCase(token.charAt(0)))
							s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
						token = s;
					}
				//}
			}
			}
			catch(IndexOutOfBoundsException e){
				String s = "askjdhakj";
			}
		}
		return token;
	}
}
