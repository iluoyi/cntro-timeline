package edu.mayo.informatics.cntro.serviceIF;

import java.net.URL;

import org.semanticweb.owlapi.model.IRI;

public interface CNTROLoader 
{
	public Object load(IRI resource);
	public Object load(String filename);
	public Object load (URL url);
	public IRI getResourceIRI();
}
