

import java.awt.List;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class Project {
	private static OWLOntology myOntology;
	private static OWLDataFactory df;
	private static OWLOntologyManager m;

	public static void main(String[] args) throws OWLOntologyCreationException {
		load();

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

	private static void load() throws OWLOntologyCreationException {
		m=OWLManager.createOWLOntologyManager();

		df = OWLManager.getOWLDataFactory();
		OWLOntologyLoaderConfiguration loaderConf = new OWLOntologyLoaderConfiguration();

		myOntology=m.loadOntologyFromOntologyDocument(
				new FileDocumentSource(new File("resources/mp2.owl")),
				loaderConf);
	}

	private static boolean doAction() throws OWLOntologyCreationException {
		System.out.println("\n------------------------------");
		System.out.println("Actions:");
		System.out.println("0 - Exit");
		System.out.println("1 - Show individuals");
		System.out.println("2 - Show specific individual");
		System.out.println("3 - Create new individual");
		System.out.println("4 - Show required properties");
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
		case 3:
			insertBurger();
			break;
		case 4:
			showRequired();
			break;
		default:
			System.out.println("Invalid input!");
			break;
		}

		return true;
	}

	private static void insertBurger() throws OWLOntologyCreationException {
		System.out.println("\n\n------------------------------");
		System.out.print("Class of individual: ");
		Scanner sc = new Scanner(System.in);
		String inClass = sc.nextLine();
		System.out.print("IriName of individual: ");
		String name = sc.nextLine();

		OWLClass cls = df.getOWLClass(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + inClass));

		OWLNamedIndividual newIndi = df.getOWLNamedIndividual(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + name));
		ArrayList<OWLOntologyChange> add = new ArrayList<OWLOntologyChange>();

		OWLClassAssertionAxiom classAssertion = df.getOWLClassAssertionAxiom(cls, newIndi);
		m.addAxiom(myOntology, classAssertion);

		OWLReasoner reasoner=new Reasoner.ReasonerFactory().createReasoner(myOntology);
		Set<OWLObjectPropertyExpression> props = getProperties(m, myOntology, reasoner, cls);

		for (OWLObjectPropertyExpression prop : props) {
			OWLClassExpression range = prop.getRanges(myOntology).iterator().next();
			System.out.print(prop.getNamedProperty().getIRI().getFragment() + " " +  range.asOWLClass().getIRI().getFragment() + ": ");

			OWLClass cls2 = df.getOWLClass(range.asOWLClass().getIRI());


			NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls2, false);
			System.out.print("(");
			int j = 0;
			for (OWLNamedIndividual i : instances.getFlattened()) {
				System.out.print("(" + j + ")");
				System.out.print(i.getIRI().getFragment());    
				if(instances.getFlattened().toArray().length > j + 1)
					System.out.print(",");
				j++;
			}
			System.out.print(")");
			System.out.print(" ");
			String action = sc.nextLine();
			if(action.equals("-"))
				continue;
			else {
				int index = Integer.parseInt(action);
				sc.nextLine();
				OWLNamedIndividual indi = (OWLNamedIndividual)instances.getFlattened().toArray()[index];
				OWLAxiom assertion = df.getOWLObjectPropertyAssertionAxiom(prop, newIndi, indi);
				add.add(new AddAxiom(myOntology, assertion));
			}

		}

		System.out.println();
		System.out.println("Add additional properties");
		boolean addProp = true;
		while(addProp) {
			addProp = addProperty(sc, newIndi, add, reasoner, addProp);
		}

		m.applyChanges(add);

		reasoner = save();
	}

	private static boolean addProperty(Scanner sc, OWLNamedIndividual newIndi, ArrayList<OWLOntologyChange> add,
			OWLReasoner reasoner, boolean addProp) {
		try {
			System.out.print("Property: ");
			String propertyName = sc.nextLine();
			if(propertyName.equals("-")) {
				addProp = false;
				return addProp;
			}
			else {
				System.out.print("Property Type (data/object): ");
				String propType = sc.nextLine();
				if(propType.equals("object")) {
					OWLObjectProperty prop = df.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + propertyName));
					OWLClassExpression range = prop.getRanges(myOntology).iterator().next();
					System.out.print(prop.getNamedProperty().getIRI().getFragment() + " " +  range.asOWLClass().getIRI().getFragment() + ": ");

					OWLClass cls2 = df.getOWLClass(range.asOWLClass().getIRI());


					NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls2, false);
					System.out.print("(");
					int j = 0;
					for (OWLNamedIndividual i : instances.getFlattened()) {
						System.out.print("(" + j + ")");
						System.out.print(i.getIRI().getFragment());    
						if(instances.getFlattened().toArray().length > j + 1)
							System.out.print(",");
						j++;
					}
					System.out.print(")");
					System.out.print(" ");
					String action = sc.nextLine();
					if(action.equals("-"))
						return addProp;
					else {
						int index = Integer.parseInt(action);
						sc.nextLine();
						OWLNamedIndividual indi = (OWLNamedIndividual)instances.getFlattened().toArray()[index];
						OWLAxiom assertion = df.getOWLObjectPropertyAssertionAxiom(prop, newIndi, indi);
						add.add(new AddAxiom(myOntology, assertion));
					}
				}
				else {
					System.out.print("Property Value: ");
					String value = sc.nextLine();

					OWLDataProperty prop = df.getOWLDataProperty(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + propertyName));
					OWLAxiom assertion = df.getOWLDataPropertyAssertionAxiom(prop, newIndi, value);
					add.add(new AddAxiom(myOntology, assertion));
				}
			}
		}
		catch(InputMismatchException ex) {
			System.out.println("Invalid input");
		}
		return addProp;
	}

	private static void showSpecific() throws OWLOntologyCreationException {
		System.out.println("\n\n------------------------------");
		System.out.print("Class of individual: ");
		Scanner sc = new Scanner(System.in);
		String inClass = sc.nextLine();
		System.out.print("Name of individual: ");
		String individual = sc.nextLine();

		OWLClass cls = df.getOWLClass(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + inClass));

		OWLReasoner reasoner=new Reasoner.ReasonerFactory().createReasoner(myOntology);
		NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, false);

		for (OWLNamedIndividual i : instances.getFlattened()) {
			if(i.getIRI().getFragment().equals(individual)) {
				System.out.println(i.getIRI());
				Set<OWLObjectPropertyAssertionAxiom> properties= myOntology.getObjectPropertyAssertionAxioms(i);
				for (OWLObjectPropertyAssertionAxiom ax: properties) {
					System.out.println(ax);
				}

				Set<OWLDataPropertyAssertionAxiom> dataProperties= myOntology.getDataPropertyAssertionAxioms(i);
				for (OWLDataPropertyAssertionAxiom ax: dataProperties) {
					System.out.println(ax);
				}

				System.out.print("Edit? (y/n): ");
				String edit = sc.nextLine();
				if(edit.equals("y")) {
					ArrayList<OWLOntologyChange> add = new ArrayList<OWLOntologyChange>();

					boolean doEdit = true;
					while(doEdit) {

						System.out.print("Property action? (add/delete): ");
						String action = sc.nextLine();
						if(action.equals("add")) {
							doEdit = addProperty(sc, i, add, reasoner, doEdit);
						}
						else if(action.equals("delete")) {
							System.out.print("Property: ");
							String propertyName = sc.nextLine();
							if(propertyName.equals("-")) {
								doEdit = false;
							}
							else {
								System.out.print("Property Type (data/object): ");
								String propType = sc.nextLine();
								if(propType.equals("object")) {
									OWLObjectProperty prop = df.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + propertyName));
									OWLClassExpression range = prop.getRanges(myOntology).iterator().next();
									System.out.print(prop.getNamedProperty().getIRI().getFragment() + " " +  range.asOWLClass().getIRI().getFragment() + ": ");

									OWLClass cls2 = df.getOWLClass(range.asOWLClass().getIRI());


									NodeSet<OWLNamedIndividual> instances1 = reasoner.getInstances(cls2, false);
									System.out.print("(");
									int j = 0;
									for (OWLNamedIndividual i1 : instances1.getFlattened()) {
										System.out.print("(" + j + ")");
										System.out.print(i1.getIRI().getFragment());    
										if(instances1.getFlattened().toArray().length > j + 1)
											System.out.print(",");
										j++;
									}
									System.out.print(")");
									System.out.print(" ");
									String action1 = sc.nextLine();
									if(action1.equals("-"))
										doEdit = false;
									else {
										int index = Integer.parseInt(action1);
										sc.nextLine();
										OWLNamedIndividual indi = (OWLNamedIndividual)instances1.getFlattened().toArray()[index];
										OWLAxiom assertion = df.getOWLObjectPropertyAssertionAxiom(prop, i, indi);
										m.removeAxiom(myOntology, assertion);
									}
								}
								else {
									System.out.print("Property Value: ");
									String value = sc.nextLine();

									OWLDataProperty prop = df.getOWLDataProperty(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + propertyName));
									OWLAxiom assertion = df.getOWLDataPropertyAssertionAxiom(prop, i, value);
									m.removeAxiom(myOntology, assertion);
								}
							}
						}
						else {
							doEdit = false;
						}
					}

					m.applyChanges(add);
					reasoner = save();
				}
			}
		}


	}

	private static OWLReasoner save() throws OWLOntologyCreationException {
		OWLReasoner reasoner;
		reasoner=new Reasoner.ReasonerFactory().createReasoner(myOntology);
		boolean consistent = reasoner.isConsistent();
		System.out.println("Is consistent? " + consistent);

		if(consistent == false) {
			System.out.println("Undoing change!");
			load();
		}
		else {
			try {
				m.saveOntology(myOntology);
			} catch (OWLOntologyStorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return reasoner;
	}

	private static void show() {
		System.out.println("\n\n------------------------------");
		System.out.print("Class of individuals: ");
		Scanner sc = new Scanner(System.in);
		String inClass = sc.nextLine();

		OWLClass cls = df.getOWLClass(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + inClass));

		System.out.println("My class is : " + cls.getIRI());                   
		System.out.println("-----------------------");

		OWLReasoner reasoner=new Reasoner.ReasonerFactory().createReasoner(myOntology);
		NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, false);
		System.out.println("The Individuals of the class : ");

		for (OWLNamedIndividual i : instances.getFlattened()) {
			System.out.println(i.getIRI().getFragment());             
		}
	}

	// Prints out the properties that instances must have
	private static Set<OWLObjectPropertyExpression> getProperties(
			OWLOntologyManager man, OWLOntology o,
			OWLReasoner reasoner, OWLClass cls) {
		System.out.println("Properties of " + cls);
		Set<OWLObjectPropertyExpression> properties = new HashSet<OWLObjectPropertyExpression>();
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
				properties.add(prop);

		}
		return properties;
	}

	private static void showRequired() {
		System.out.println("\n\n------------------------------");
		System.out.print("Classs: ");
		Scanner sc = new Scanner(System.in);
		String inClass = sc.nextLine();

		OWLClass cls = df.getOWLClass(IRI.create("http://www.semanticweb.org/max/ontologies/2017/5/MiniProjekt2#" + inClass));
		OWLReasoner reasoner=new Reasoner.ReasonerFactory().createReasoner(myOntology);
		printProperties(m, myOntology, reasoner, cls);
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
