package edu.mayo.informatics.cntro.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.mayo.informatics.cntro.queryIF.ParserType;
import edu.mayo.informatics.cntro.serviceIF.CNTROLoader;

public class CNTROLoaderImpl implements CNTROLoader 
{
	public IRI iri = null;
	public ParserType parsertype = ParserType.OWLAPI;
	
	public Object load(String filename)
	{
		File file = new File(filename);
		
		try {
			iri = IRI.create(file.toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return load(iri);
	}
	
	public Object load(IRI resource) 
	{
		try 
		{
			switch (this.parsertype)
			{
				case OWLAPI:
					OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
					OWLOntology ontology = manager.loadOntology(resource);
					return ontology;
				case JENA:
					OntModel model = ModelFactory.createOntologyModel(org.mindswap.pellet.jena.PelletReasonerFactory.THE_SPEC );
					model.read(resource.toString());
					model.prepare();
					return model;
			}
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Object load(URL url) 
	{
		IRI iri = IRI.create(url.getPath());
		return load(iri);
	}

	public IRI getResourceIRI() {
		// TODO Auto-generated method stub
		return this.iri;
	}
}
