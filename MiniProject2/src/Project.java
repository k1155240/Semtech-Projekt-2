

import java.io.File;
import java.text.MessageFormat;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class Project {
	private static OWLOntology myOntology;
	private static OWLReasoner reasoner;
	private static OWLDataFactory df;
	private static OWLOntologyManager m;
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		m=OWLManager.createOWLOntologyManager();
		
		df = OWLManager.getOWLDataFactory();
		  OWLOntologyLoaderConfiguration loaderConf = new OWLOntologyLoaderConfiguration();

		myOntology=m.loadOntologyFromOntologyDocument(
				  new FileDocumentSource(new File("resources/mp2.owl")),
				  loaderConf);
		reasoner=new Reasoner.ReasonerFactory().createReasoner(myOntology);

		  
		System.out.println("\n\n------------------------------");
		System.out.println("SemTech Mini Project 2");
		System.out.println("Authors: Stefan | Max");
		System.out.println("------------------------------");

		boolean run = true;
		while(run) {
			try {
				run = doAction();
			}
			catch(InputMismatchException ex) {
					System.out.println("Invalid input");
			}
		}
	}

	private static boolean doAction() {
		System.out.println("\n------------------------------");
		System.out.println("Actions:");
		System.out.println("0 - Exit");
		System.out.println("1 - Show individuals");
		System.out.println("2 - Show specific individual");
		System.out.println("3 - Create new burger");
		System.out.println("------------------------------");

		System.out.println("------------------------------");
		System.out.print("What do you want to do: ");

		Scanner sc = new Scanner(System.in);
		int action = sc.nextInt();
		sc.nextLine();

		System.out.println("------------------------------");
		System.out.println("You chose " + action);
		System.out.println("------------------------------");

		switch (action) {
		case 0:
			System.out.println("Exiting!");
			return false;
		case 1:
			show();
			break;
		case 2:
			showSpecific();
			break;
		case 4:
			insertBurger();
			break;
		default:
			System.out.println("Invalid input!");
			break;
		}

		return true;
	}
	
	private static void insertBurger() {

		
	}

	private static void showSpecific() {
		System.out.println("\n\n------------------------------");
		System.out.print("Class of individual: ");
		Scanner sc = new Scanner(System.in);
		String inClass = sc.nextLine();
		System.out.print("Name of individual: ");
		String individual = sc.nextLine();

		OWLClass cls = df.getOWLClass(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + inClass));

        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, false);
    
        for (OWLNamedIndividual i : instances.getFlattened()) {
        	if(i.getIRI().getFragment().equals(individual)) {
              System.out.println(i.getIRI());
              Set<OWLObjectPropertyAssertionAxiom> properties= myOntology.getObjectPropertyAssertionAxioms(i);
              for (OWLObjectPropertyAssertionAxiom ax: properties) {
            	    System.out.println(ax);
            	}
        	}
        }
	}

	private static void show() {
		System.out.println("\n\n------------------------------");
		System.out.print("Class of individuals: ");
		Scanner sc = new Scanner(System.in);
		String inClass = sc.nextLine();
		
		OWLClass cls = df.getOWLClass(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + inClass));

        System.out.println("My class is : " + cls.getIRI());                   
        System.out.println("-----------------------");
        
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, false);
        System.out.println("The Individuals of the class : ");
    
        for (OWLNamedIndividual i : instances.getFlattened()) {
              System.out.println(i.getIRI().getFragment());             
        }
	}
	
	// Prints out the properties that instances must have
	private static void printProperties(
		OWLOntologyManager man, OWLOntology o,
		OWLReasoner reasoner, OWLClass cls) {
		System.out.println("Properties of " + cls);
		for (OWLObjectPropertyExpression prop :
		o.getObjectPropertiesInSignature()) {
		// To test if an instance of A MUST have a p-filler,
		// check for the satisfiability of A and not (some p Thing)
		// if this is unsatisfiable, then a p-filler is necessary
		OWLClassExpression restriction =
		df.getOWLObjectSomeValuesFrom(prop, df.getOWLThing());
		OWLClassExpression intersection =
		df.getOWLObjectIntersectionOf(cls,
		df.getOWLObjectComplementOf(restriction));
		if (!reasoner.isSatisfiable(intersection))
		System.out.println("Instances of "
		+ cls + " must have " + prop);
		}
	}
}
