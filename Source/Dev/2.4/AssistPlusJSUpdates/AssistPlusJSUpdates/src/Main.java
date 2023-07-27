import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
	public static void main(String[] args) {


		Console console = null;
		try {
			console = System.console();

			String tomcatURL = console.readLine("Please enter complete Tomcat URL for AssistPlus: ");
//			String tomcatURL = "http://agile934.xavor.com:8090/AssistPlus/";
			System.out.println("Updating JS files");


			File file=new File("agileAssist.dev.js");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String dataLine="",fileContent="";

			while ((dataLine = br.readLine()) != null) {
				if(dataLine.contains("$homeDirectory=$")) {
					dataLine=dataLine.replace("$homeDirectory=$", "homeDirectory=\""+tomcatURL+"\"");
				}
				if(dataLine.contains("$arg.url.OptOut$")) {
					dataLine=dataLine.replace("$arg.url.OptOut$", "arg.url=\""+tomcatURL+"/OptOut\"");
				}
				if(dataLine.contains("$arg.url.GetAssistText$")) {
					dataLine=dataLine.replace("$arg.url.GetAssistText$", "arg.url=\""+tomcatURL+"/GetAssistText\"");
				}
				fileContent = fileContent + dataLine + System.lineSeparator();
			}
			br.close();
			FileWriter writer = new FileWriter("agileAssist.dev.js");
			writer.write(fileContent);
			writer.close();
			
			
			System.out.println("Updated agileAssist.dev.js successfully.");
			
			
			File minJs=new File("core-agile-min.js");
			BufferedReader br1 = new BufferedReader(new FileReader(minJs));
			String dataLine1="",fileContent1="";

			while ((dataLine = br1.readLine()) != null) {
					dataLine=dataLine.replace("$ADDASSISTPLUSURL$", 
							"script|type|append|head|jQuery|text|javascript|js|agileAssist.dev|"+tomcatURL+"|src");
					fileContent1=fileContent1+dataLine;
				
			}
			br1.close();
			FileWriter writer1 = new FileWriter("core-agile-min.js");
			writer1.write(fileContent1);
			writer1.close();
			System.out.println("Updated core-agile-min.js successfully.");
			System.out.println("Process completed miss");

		} catch(Exception ex) {
			ex.printStackTrace();
		}



	}
}
