package sbml;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.sbml.libsbml.ListOfSpeciesReferences;
import org.sbml.libsbml.Reaction;

public class SimulateSBML_isoda {
	Map<String,Double> result = new HashMap<>();
	String[] calc;
	public Map<String,Double> simEuler(ReadSBML_isoda rsbml,Map<String,Double> value,double step){
		//Repeat by reaction or species
		double x;
		int i,j;
		ListOfSpeciesReferences lor,lop;
		double stoichiometry;
		Reaction reaction;
		// result = value
		result.putAll(value);
		System.out.println(result.values());
		for(i=0;i<rsbml.getNum_reactions();i++) {
			Deque<Double> stack = new ArrayDeque<Double>();
			//Get correct node 
			reaction = rsbml.getReaction(i);
			lor =reaction.getListOfReactants();
			lop = reaction.getListOfProducts();
			stack = rsbml.scanTree(reaction.getKineticLaw().getMath(), stack,value,reaction.getKineticLaw().getListOfParameters());
			double dxdt = stack.removeFirst();
			//each products reactants
			for(j=0;j<reaction.getNumProducts();j++) {
				x = result.get(lop.get(j).getSpecies());
				stoichiometry = reaction.getProduct(j).getStoichiometry();
				x = x + dxdt * stoichiometry * step;
				result.put(lop.get(j).getSpecies(), x);
			}
			for(j=0;j<reaction.getNumReactants();j++) {
				x = result.get(lor.get(j).getSpecies());
				stoichiometry = reaction.getReactant(j).getStoichiometry();
				//System.out.println(reaction.getProduct(j).getSpecies());
				x = x - dxdt * stoichiometry * step;
				result.put(lor.get(j).getSpecies(), x);
			}
		}
		value.putAll(result);
		return value;
	}
	
	public Map<String,Double> simRK(ReadSBML_isoda rsbml,Map<String,Double> value,double step) {
		//Repeat by reaction or species
		Map<String,Double> init = new HashMap<>();
		Map<String,Double> result = new HashMap<>();
		init.putAll(value);
		int i,j,r;
		ListOfSpeciesReferences lor,lop;
		Map<String,Double> k0 = new HashMap<>();
		k0= rsbml.set0(k0);
		Map<String,Double> k1 = new HashMap<>();
		k1= rsbml.set0(k1);
		Map<String,Double> k2 =new HashMap<>();
		k2= rsbml.set0(k2);
		Map<String,Double> k3 = new HashMap<>();
		k3= rsbml.set0(k3);
		double stoichiometry;
		Reaction reaction;
		result.putAll(value);
		for(r=0;r<4;r++) {
			//System.out.println("r :" + r);
			//rsbml.print(value);
			for(i=0;i<rsbml.getNum_reactions();i++) {
				Deque<Double> stack = new ArrayDeque<Double>();
				//Get correct node 
				reaction = rsbml.getReaction(i);
				lor = reaction.getListOfReactants();
				lop = reaction.getListOfProducts();
				stack = rsbml.scanTree(reaction.getKineticLaw().getMath(), stack,value,reaction.getKineticLaw().getListOfParameters());
				double dxdt = stack.removeFirst();
				//each products reactants
				for(j=0;j<reaction.getNumProducts();j++) {
					stoichiometry = reaction.getProduct(j).getStoichiometry();
					if(r==0) {
						k0.put(lop.get(j).getSpecies(),k0.get(lop.get(j).getSpecies()) + dxdt * stoichiometry);
					}
					else if(r==1) {
						k1.put(lop.get(j).getSpecies(),k1.get(lop.get(j).getSpecies()) + dxdt * stoichiometry);
					}
					else if (r==2) {
						k2.put(lop.get(j).getSpecies(),k2.get(lop.get(j).getSpecies()) + dxdt * stoichiometry);
					}
					else {
						k3.put(lop.get(j).getSpecies(),k3.get(lop.get(j).getSpecies()) + dxdt * stoichiometry);
					}
					
				}
				for(j=0;j<reaction.getNumReactants();j++) {
					stoichiometry = reaction.getReactant(j).getStoichiometry();
					if(r==0) {
						k0.put(lor.get(j).getSpecies(),k0.get(lor.get(j).getSpecies()) - dxdt * stoichiometry);
					}
					else if(r==1) {
						k1.put(lor.get(j).getSpecies(),k1.get(lor.get(j).getSpecies()) - dxdt * stoichiometry);
					}
					else if (r==2) {
						k2.put(lor.get(j).getSpecies(),k2.get(lor.get(j).getSpecies()) - dxdt * stoichiometry);
					}
					else {
						k3.put(lor.get(j).getSpecies(),k3.get(lor.get(j).getSpecies()) - dxdt * stoichiometry);
					}
				}
			}
			if(r==0) {
				value = rsbml.runge_step(k0,step/2,init, value);
			}
			else if(r==1) {
				value = rsbml.runge_step(k1,step/2,init, value);
			}
			else if (r==2) {
				value = rsbml.runge_step(k2,step,init, value);
			}
		}
		result = rsbml.runge(k0,k1,k2,k3,step,init, result);
		value.putAll(result);
		return value;
	}
	
	
}
