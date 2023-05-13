package sbml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;


public class MySBML_isoda {
	static {
		try {
			System.loadLibrary("sbmlj");
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML(args[0]);
		Model model = d.getModel();
		ReadSBML_isoda rsbml = new ReadSBML_isoda();
		rsbml.createReactionData(model);
		rsbml.print(System.in);
		
		SimulateSBML_isoda simulate = new SimulateSBML_isoda();
		Map<String,Double> value = new HashMap<>();
		double step=1.0;
		double i;
		int j;
		FileWriter filewriter = null;
		try{
			  File file = new File("/Users/isoda/eclipse-workspace/Educ2022/euller.csv");
			  filewriter = new FileWriter(file);
			}catch(IOException e){
			  System.out.println(e);
			}
		//set Initial value
		value = rsbml.setInitialValue(value);
		
		//Repeat by step
		for(i=0;i<4000+step;i = i+step) {
			// Write on file
			if(i==0) {
				filewriter.write("time");
				for (j=0;j<rsbml.getNumspecies();j++) {
					filewriter.write("," + rsbml.getSpecies(j));
				}
				filewriter.write("\n");
			}
			filewriter.write(Double.toString(i));
			filewriter = rsbml.writeValue(value,filewriter);
			filewriter.write("\n");
			value = simulate.simEuler(rsbml,value,step);
		}
		
		filewriter.close();
		
		//Reset Initial value
		value = rsbml.setInitialValue(value);
		try{
			  File file = new File("/Users/isoda/eclipse-workspace/Educ2022/runge.csv");
			  filewriter = new FileWriter(file);
			}catch(IOException e){
			  System.out.println(e);
			}
		for(i=0;i<4000+step;i = i+step) {
			//Write on file
			//System.out.println("Step:" + i);
			if(i==0) {
				filewriter.write("time");
				for (j=0;j<rsbml.getNumspecies();j++) {
					filewriter.write("," + rsbml.getSpecies(j));
				}
				filewriter.write("\n");
			}
			filewriter.write(Double.toString(i));
			filewriter = rsbml.writeValue(value,filewriter);
			filewriter.write("\n");
			value = simulate.simRK(rsbml,value,step);
		}
		
		filewriter.close();
		System.out.println("End!!");
	}
	
}
