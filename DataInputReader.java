
import java.io.File;
import java.util.Scanner;

public interface DataInputReader {
	String[] getDataFrom(Object loc);
}

class InputReaderConcrete implements DataInputReader{
	@Override
	public String[] getDataFrom(Object loc) {
		String path = (String)loc;
		String output[] = null;
		Scanner sc = null;
		try{
			File f = new File(path);
			sc = new Scanner(f);
			String content = sc.useDelimiter("\\Z").next();
			output = content.split("\\s+");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			sc.close();
		}
		return output;
	}
}

class AvoidWordReaderConcrete implements DataInputReader{
	@Override
	public String[] getDataFrom(Object loc) {
		String path = (String)loc;
		String output[] = null;
		Scanner sc = null;
		try{
			File f = new File(path);
			sc = new Scanner(f);
			String content = sc.useDelimiter("\\Z").next();
			output = content.split(",");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			sc.close();
		}
		return output;
	}
}