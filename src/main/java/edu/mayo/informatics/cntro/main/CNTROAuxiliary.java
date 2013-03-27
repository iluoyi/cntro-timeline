package edu.mayo.informatics.cntro.main;


import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import com.hp.hpl.jena.ontology.OntModel;

import edu.mayo.informatics.cntro.impl.CNTROJenaAPIParser;
import edu.mayo.informatics.cntro.impl.CNTROLoaderImpl;
import edu.mayo.informatics.cntro.impl.CNTROOWLAPIParser;
import edu.mayo.informatics.cntro.queryIF.ParserType;
import edu.mayo.informatics.cntro.serviceIF.CNTROLoader;
import edu.mayo.informatics.cntro.serviceIF.CNTROParser;

public class CNTROAuxiliary 
{
	public String uri = "resources/ts1.owl";
	public ParserType parsertype = ParserType.OWLAPI;
	
	public OWLOntology ontology_ = null;
	public OntModel model_ = null;
	
	public IRI iri = null;
	
	public CNTROParser parser_ = null;
	
	public CNTROAuxiliary(String ontUri)
	{
		this.uri = ontUri;
		init();
	}
	
	public CNTROAuxiliary(IRI iri)
	{
		this.iri = iri;
		init();
	}
	
	private void init()
	{
	}

	public void loadOntology()
	{
		CNTROLoader loader = new CNTROLoaderImpl();
		((CNTROLoaderImpl)loader).parsertype = this.parsertype;
		switch(this.parsertype)
		{
			case OWLAPI:
				if (this.iri != null)
					ontology_ = (OWLOntology)loader.load(this.iri);
				else
					ontology_ = (OWLOntology)loader.load(uri);
				break;
			case JENA:
				if (this.iri != null)
					model_ = (OntModel) loader.load(this.iri);
				else
					model_ = (OntModel) loader.load(uri);
				break;
			default: System.out.println("Incorrect Parser Type: Has to be either OWL API or Jena.");
				return;
		}
	}
	
	public void parse()
	{
		parser_ = null;
		switch(this.parsertype)
		{
			case OWLAPI:
				if (ontology_ == null)
					System.out.println("Ontology is Null! exiting...");
				else
				{
					parser_ = new CNTROOWLAPIParser(ontology_);
					parser_.parse();
				}
				break;
			case JENA:
				if (model_ == null)
					System.out.println("Ont Model is Null! exiting...");
				else
				{
					parser_ = new CNTROJenaAPIParser(model_);
					parser_.parse();
				}
				break;
			default: System.out.println("Incorrect Parser Type: Has to be either OWL API or Jena.");
				return;
		}
		System.out.println("Events Loaded=" + parser_.getEventCount());
	}
	
	/*
	public void testit()
	{
		testitWithJena();
		testitWithOWLAPI();
	}
	public void testitWithJena()
	{
		// load the ontology with its imports and no reasoning
		OntModel model = ModelFactory.createOntologyModel(org.mindswap.pellet.jena.PelletReasonerFactory.THE_SPEC );
		try {
			model.read((new File(this.uri)).toURL().toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// load the model to the reasoner
		model.prepare();
		
		// create property and resources to query the reasoner
		OntClass event = model.getOntClass(CNTROConstants.CNTRO_EVENT_CLS);
		
		// get all instances of event class
		Iterator<?> i = event.listInstances();
		
		CNTROUtils.printIterator(i, "TEST WITH JENA (Instances)", null, true);
		
		Iterator<?> i2 = event.listInstances();
		while( i2.hasNext() ) 
		{
		    Individual ind = (Individual) i2.next();
		    
		    // get the info about this specific individual
		    StmtIterator sitr = ind.listProperties();
		    CNTROUtils.printIterator(sitr, "TEST WITH JENA : " + ind.toString(), null, true);
		}
	}
	
	public void testitWithOWLAPI()
	{
		// load the ontology to the reasoner
		PelletReasoner lreasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner( ontology );
		
		// create property and resources to query the reasoner
		OWLClass eventCls = df.getOWLClass(IRI.create(CNTROConstants.CNTRO_EVENT_CLS));
		
		// get all instances of Person class
		Set<OWLNamedIndividual> individuals = lreasoner.getInstances( eventCls, false ).getFlattened();
		for(OWLNamedIndividual ind : individuals) 
		{
			Set<OWLNamedIndividual> beforeList = lreasoner.getObjectPropertyValues(ind, this.before).getFlattened();
			CNTROUtils.printIterator(beforeList.iterator(), "TEST WITH OWLAPI (before) :" + ind.toString(), null, true);

			Set<OWLNamedIndividual> afterList = lreasoner.getObjectPropertyValues(ind, this.after).getFlattened();
			CNTROUtils.printIterator(afterList.iterator(), "TEST WITH OWLAPI (after) :" + ind.toString(), null, true);
		}
	}

	*/
}
