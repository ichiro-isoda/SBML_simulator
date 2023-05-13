package sbml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.ListOfSpeciesReferences;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;

public class ReadSBML_isoda {
	private ListOfReactions list_r;
	private long num_reactions;
	private ListOfSpecies list_s;
	private long num_species;
	public ASTNode node;
	
	public void createReactionData(Model model) {
		list_r = model.getListOfReactions();
		num_reactions = model.getNumReactions();
		list_s = model.getListOfSpecies();
		num_species = model.getNumSpecies();
	}
	
	public void print(InputStream in) {
		Scanner scan = new Scanner(in);
		Reaction reaction = list_r.get(scan.nextLine());
		scan.close();
		KineticLaw law = reaction.getKineticLaw();
		System.out.print("Kinetic Law: "+law.getFormula()+"\n");
		node = law.getMath();
		ListOfSpeciesReferences list_reactants = reaction.getListOfReactants();
		ListOfSpeciesReferences list_products = reaction.getListOfProducts();
		long num_products = reaction.getNumProducts();
		long num_reactants = reaction.getNumReactants();
		long i=0;
		System.out.println("\nReactants:");
		for (i=0;i<num_products;i++) {
			System.out.println(list_reactants.get(i).getSpecies());
		}
		System.out.println("\nProducts:");
		for (i=0;i<num_reactants;i++) {
			System.out.println(list_products.get(i).getSpecies());
		}
	}
	
	public Deque<Double> scanTree(ASTNode node,Deque<Double> stack,Map<String,Double> value,ListOfParameters list_p) {
		double value1,value2;
		if(node.getLeftChild() != null) {
			stack = scanTree(node.getLeftChild(),stack,value,list_p);
		}
		if(node.getRightChild() != null) {
			stack = scanTree(node.getRightChild(),stack,value,list_p);
		}
		
		//if(node.isOperator() || node.getName() == "power") {
		if(node.isNumber()){
			if(node.isReal()) {
				stack.addFirst((double)node.getReal());
			}
			else if(node.isInteger()) {
				stack.addFirst((double)node.getInteger());
			}
		}
		else if(node.getType() != 260 ) {
			value2 = (double) stack.removeFirst();
			value1 = (double) stack.removeFirst();
			if(node.getOperatorName() != null) {
				if(node.getOperatorName().equals("times")) {
					stack.addFirst(value1*value2);
				}
				else if(node.getOperatorName().equals("plus")) {
					stack.addFirst(value1+value2);
				}
				else if(node.getOperatorName().equals("minus")) {
					stack.addFirst(value1-value2);
				}
				else if(node.getOperatorName().equals("divide")) {
					stack.addFirst(value1/value2);
				}
			}
			else { //if(node.getName() == "power") {
				stack.addFirst(Math.pow(value1,value2));
			}
		
		}
		else {
			if(this.judgePorS(node.getName()) == "parameters") {
				stack.addFirst(list_p.get(node.getName()).getValue());
			}
			else {
				stack.addFirst(value.get(list_s.get(node.getName()).getId()));
			}
		}
		return stack;
	}
	
	public String judgePorS(String name) {
		long i;
		for(i=0;i<num_species;i++) {
			if(list_s.get(name) != null) {
				return "species";
			}
		}
		return "parameters";
	}
	
	
	public Map<String,Double> setInitialValue(Map<String,Double> value) {
		int i;
		System.out.println("Initialize");
		for (i=0;i<this.num_species;i++) {
			//Amount or Concentration
			System.out.println(list_s.get(i).getId());
			System.out.println(list_s.get(i).getInitialAmount());
			value.put(this.list_s.get(i).getId(),this.list_s.get(i).getInitialAmount());
		}
		System.out.println("Initialize_end");
		return value;
	}
	
	public long getNum_reactions() {
		return this.num_reactions;
	}
	
	public Reaction getReaction(int i) {
		Reaction reaction;
		reaction = this.list_r.get(i);
		return reaction;
	}
	
	public Map<String,Double> set0(Map<String,Double> k){
		int i;
		for (i=0;i<this.num_species;i++) {
			k.put(this.list_s.get(i).getId(),0.0);
		}
		return k;
	}
	
	public 	Map<String,Double> runge(Map<String,Double>  k0,Map<String,Double>  k1,Map<String,Double>  k2,Map<String,Double>  k3,double step ,Map<String,Double> init, Map<String,Double> result){
		int i;
		double tmp;
		Map<String,Double> value = new HashMap<>();
		for (i=0;i<this.num_species;i++) {
			//Amount or Concentration
			tmp = init.get(this.list_s.get(i).getId()) + (k0.get(this.list_s.get(i).getId()) + k1.get(this.list_s.get(i).getId()) *2 + k2.get(this.list_s.get(i).getId()) *2 + k3.get(this.list_s.get(i).getId())) * step /6;
			value.put(this.list_s.get(i).getId(),tmp);
		}
		result.putAll(value);
		return result;
	}
	public FileWriter writeValue(Map<String,Double> value,FileWriter filewriter) throws IOException {
		int i;
		for (i=0;i<this.num_species;i++) {
			//Amount or Concentration
			filewriter.write("," + value.get(this.list_s.get(i).getId() ));
		}
		return filewriter;
	}
	public Map<String,Double> runge_step(Map<String,Double> k,double step,Map<String,Double> init, Map<String,Double> result){
		int i;
		Map<String,Double> value = new HashMap<>();
		for (i=0;i<this.num_species;i++) {
			value.put(this.list_s.get(i).getId(), init.get(this.list_s.get(i).getId()) + k.get(this.list_s.get(i).getId()) * step);
		}
		result.putAll(value);
		return result;
	}
	public void print(Map<String,Double> value) {
		int i;
		for (i=0;i<this.num_species;i++) {
			System.out.print(this.list_s.get(i).getId() + ":");
			System.out.println(value.get(this.list_s.get(i).getId()));
		}
	}
	public long getNumspecies() {
		return this.num_species;
	}
	public String getSpecies(int j) {
		return this.list_s.get(j).getId();
	}
}
