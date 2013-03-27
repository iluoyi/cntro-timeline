package edu.mayo.informatics.cntro.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.main.CNTROConstants;
import edu.mayo.informatics.cntro.model.CNTROCls;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.model.TemporalRelation;
import edu.mayo.informatics.cntro.model.Time;
import edu.mayo.informatics.cntro.queryIF.TemporalRelationType;
import edu.mayo.informatics.cntro.serviceIF.CNTROParser;
import edu.mayo.informatics.cntro.utils.CNTROMap;
import edu.mayo.informatics.cntro.utils.CNTROUtils;
import edu.mayo.informatics.cntro.utils.DateParser;
import edu.mayo.informatics.cntro.utils.EventsHolder;
import edu.mayo.informatics.cntro.utils.RelationsHolder;

public class CNTROJenaAPIParser implements CNTROParser 
{
	public OntModel ontModel = null;
	
	public DateParser dateParser = new DateParser();
	public boolean addNormalizedTimeValue = true;
	
	public EventsHolder eventsHolder = new EventsHolder();;
	public RelationsHolder relationsHolder = new RelationsHolder();

	public Property hasValidTime = null;
	public Property hasNoteTime = null;
	public Property hasOriginalTime = null;
	public Property hasNormalizedTime = null;
	
	public Property after = null;
	public Property before = null;
	public Property continuesThrough = null;
	public Property include = null;
	public Property isIncluded = null;
	public Property overlap = null;
	public Property temporalRelation = null;

	public Property overlappedBy = null;
	public Property initiate = null;
	public Property simultaneous = null;
	public Property terminate = null;
	public Property sameas = null;

	public Property hasStartTime = null;
	public Property hasEndTime = null;
	public Property hasDuration = null;
	public Property hasDurationUnit = null;

	public Vector<Property> temporalRelations = new Vector<Property>();

	
	public CNTROJenaAPIParser(OntModel model)
	{
		if (model == null)
		{
			System.out.println("!!!!!!!!!! Initialization Error!! model is null. Nothing will work !!!!!!!!!!!!!");
			return;
		}
		
		this.ontModel = model;
		
		hasValidTime = ontModel.getObjectProperty(CNTROConstants.CNTRO_HASVALIDTIME_PRP);
		hasNoteTime = ontModel.getObjectProperty(CNTROConstants.CNTRO_HASNOTETIME_PRP);
		hasOriginalTime = ontModel.getDatatypeProperty(CNTROConstants.CNTRO_HASORIGINALTIME_PRP);
		hasNormalizedTime = ontModel.getDatatypeProperty(CNTROConstants.CNTRO_HASNORMALIZEDTIME_PRP);
		
		hasStartTime = ontModel.getObjectProperty(CNTROConstants.CNTRO_HASSTARTTIME_PRP);
		hasEndTime = ontModel.getObjectProperty(CNTROConstants.CNTRO_HASENDTIME_PRP);
		hasDuration = ontModel.getObjectProperty(CNTROConstants.CNTRO_HASDURATION_PRP);
		hasDurationUnit = ontModel.getDatatypeProperty(CNTROConstants.CNTRO_HASDURATIONUNIT_PRP);
		
		after = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_AFTER_PRP);
		before = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_BEFORE_PRP);
		continuesThrough = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_CONTINUES_THROUGH_PRP);
		include = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_INCLUDE_PRP);
		isIncluded = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_IS_INCLUDED_PRP);
		overlap = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_OVERLAP_PRP);
		overlappedBy = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_OVERLAPPED_BY_PRP);
		initiate = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_INITIATE_PRP);
		simultaneous = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_SIMULTANEOUS_PRP);
		terminate = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_TERMINATE_PRP);
		sameas = ontModel.getObjectProperty(CNTROConstants.CNTRO_TR_SAMEAS_PRP);
		
		temporalRelations.add(after);
		temporalRelations.add(before);
		temporalRelations.add(continuesThrough);
		temporalRelations.add(include);
		temporalRelations.add(isIncluded);
		temporalRelations.add(overlap);
		temporalRelations.add(overlappedBy);
		temporalRelations.add(initiate);
		temporalRelations.add(simultaneous);
		temporalRelations.add(terminate);
	}
	
	public Collection<Event> getEvents()
	{
		return this.eventsHolder.getAllEvents();
	}
	
	public int getEventCount()
	{
		return this.eventsHolder.size();
	}
	
	public CNTROMap getEventMap()
	{
		return this.eventsHolder;
	}

	public CNTROMap getRelationMap()
	{
		return this.relationsHolder;
	}

	public boolean parse()
	{
		boolean isReadingSmooth = true;
		
		OntClass event = ontModel.getOntClass(CNTROConstants.CNTRO_EVENT_CLS); 
		Iterator<?> instances = event.listInstances();
		
		while(instances.hasNext()) 
		{
		    Individual ind = (Individual) instances.next();
		    StmtIterator sitr = ind.listProperties();
		    CNTROUtils.printIterator(sitr, "TEST WITH JENA : " + ind.toString(), null, true);
		    
		    String label = ind.getLabel(null);
		    
		    if (eventsHolder.getByLabel(label) == null)
			{
				Event evt = parse(ind);
				isReadingSmooth = ((evt != null) && isReadingSmooth);
			}
			else
				System.out.println("\nAlready processed:" + ind.toString());
		}

		return isReadingSmooth;
	}
	
	public Event parse(Object eventInstance) 
	{
		if ((eventInstance == null)||
			(!(eventInstance instanceof Individual)))
			return null;
		
		Individual ind = (Individual) eventInstance;
		
		String label = ind.getLabel(null);
		
		Event retEvent = eventsHolder.getEventByLabel(label);
		
		if (retEvent != null)
			return retEvent;
		
		retEvent = new Event(null);
		retEvent.description = label;
		retEvent.setClsId(label);
		eventsHolder.add(retEvent);
		
		List<RDFNode> validTimes = getPropertyValues(ind, hasValidTime);
		List<CNTROCls> times = processTargets(validTimes, ind);
		
		if ((times != null)&&(!times.isEmpty()))
			retEvent.eventTime = CNTROUtils.getTimeFromCNTROCls(times.get(0)); 
		
		List<RDFNode> noteTimes = getPropertyValues(ind, hasNoteTime);
		times = processTargets(noteTimes, ind);
		if ((times != null)&&(!times.isEmpty()))
			retEvent.noteTime = CNTROUtils.getTimeFromCNTROCls(times.get(0));
		
		for (int i=0; i < temporalRelations.size(); i++)
		{
			Property currentProperty = temporalRelations.elementAt(i);
			List<RDFNode> temporalRelValues = getPropertyValues(ind, currentProperty);
			
			if ((temporalRelValues == null)||(temporalRelValues.isEmpty()))
				continue;
			
			TemporalRelationType relType = getTemporalRelationTypeForProperty(currentProperty);
			populateRelations(temporalRelValues, ind, retEvent, relType);
		}
		
		return retEvent;
	}
	
	
	private List<CNTROCls> processTargets(List<RDFNode> values, Individual ind)
	{
		List<CNTROCls> retTargets = new ArrayList<CNTROCls>();
		
		if ((values == null)||(values.isEmpty()))
			return null;
				
		for (Iterator<RDFNode> valuesItr = values.iterator(); valuesItr.hasNext();)
		{
			RDFNode nd = valuesItr.next();
			if (nd == null)
				continue;
			
			RDFDatatype dtp = Node.getType(nd.toString());
			System.out.println("TYPE:" + dtp);
			System.out.println("TYPE:" + dtp.getURI());
			// Here we need to find out what is the type of node 'nd' and then proceed. Stuck here ....
			System.out.println("node TYPE:" + nd.asResource().getURI());
			OntClass event = ontModel.getOntClass(CNTROConstants.CNTRO_EVENT_CLS);
			System.out.println("test:" + nd.canAs(event.getClass()));
			System.out.println(nd.getClass());
			

			
			/*
			Set<OWLClassExpression> oiTypes = nd.asResource().getTypes(ontology);
			if ((oiTypes == null)||(oiTypes.isEmpty()))
				continue;
			
			String labelAsTime = getAnnotationPropertyValue(nd, rdfLabel);
			TimeInstant tiFromLabel = getTimeInstantFromLabel(nd);
			
			for (Iterator<OWLClassExpression> objectPropertyTypeItr = oiTypes.iterator(); objectPropertyTypeItr.hasNext();)
			{
				OWLClassExpression owlInstanceTypeObj = objectPropertyTypeItr.next();
				if ((owlInstanceTypeObj != null)&&(owlInstanceTypeObj.toString().contains(CNTROConstants.CNTRO_EVENT_CLS)))
				{
					Event event = this.eventsHolder.getEventByLabel(labelAsTime);
					
					if (event == null)
						event = this.parse(nd);
					
					if (event == null)
						continue;
					
					retTargets.add(event);
				}
				else
				{
					if ((owlInstanceTypeObj != null)&&(owlInstanceTypeObj.toString().contains(CNTROConstants.CNTRO_TIMEINSTANCE_CLS)))
					{
						TimeInstant tinst = new TimeInstant();
						
						Set<OWLLiteral> originalTimeValues = getDataPropertyValue(nd, hasOriginalTime);
						if ((originalTimeValues != null)&&(!originalTimeValues.isEmpty()))
						{
							for (Iterator<OWLLiteral> dataPropertyValueItr = originalTimeValues.iterator(); dataPropertyValueItr.hasNext();)
							{
								OWLLiteral originalTimeValue = dataPropertyValueItr.next();
								String originalTime = originalTimeValue.getLiteral();
								tinst.granularity = CNTROUtils.getGranularityFromDateString(originalTime);
								tinst.originalTime = originalTime;
								
								if (addNormalizedTimeValue)
									tinst.normalizedTime = normalize(originalTime, nd);
							}
						}
						
						if (tiFromLabel != null)
							CNTROUtils.copyTimeInstantValuesIfNull(tiFromLabel, tinst);
						
						retTargets.add(tinst);
					}
					else
					{
						if ((owlInstanceTypeObj != null)&&(owlInstanceTypeObj.toString().contains(CNTROConstants.CNTRO_TIMEINTERVAL_CLS)))
						{
							TimeInterval tintv = new TimeInterval();
							
							Set<OWLNamedIndividual> startTime = getObjectPropertyValue(nd, hasStartTime);
							List<CNTROCls> times = processTargets(startTime, nd);
							
							if ((times != null)&&(!times.isEmpty()))
								tintv.startTime = CNTROUtils.getTimeFromCNTROCls(times.get(0));
							
							Set<OWLNamedIndividual> endTime = getObjectPropertyValue(nd, hasEndTime);
							times = processTargets(endTime, nd);
							if ((times != null)&&(!times.isEmpty()))
								tintv.endTime = CNTROUtils.getTimeFromCNTROCls(times.get(0)); 
							
							Set<OWLNamedIndividual> durations = getObjectPropertyValue(nd, hasDuration);
							tintv.duration = processDuration(durations, nd);
							
							retTargets.add(tintv);
						}
						else
						{
							if ((owlInstanceTypeObj != null)&&(owlInstanceTypeObj.toString().contains(CNTROConstants.CNTRO_TIMEPERIOD_CLS)))
							{
								System.out.println("TIME PERIOD TYPE HANDLING NOT IMPLEMENTED YET!!");
							}
							else
							{
								if ((owlInstanceTypeObj != null)&&(owlInstanceTypeObj.toString().contains(CNTROConstants.CNTRO_TIMEPHASE_CLS)))
								{
									System.out.println("TIME PHASE TYPE HANDLING NOT IMPLEMENTED YET!!");
								}
							}
						}
					}
				}
			}
			*/
		}
		
		return retTargets;
	}
	
	private void  populateRelations(List<RDFNode> relations, 
									Individual ind, 
									Event sourceEvent, 
									TemporalRelationType relType)
	{
		List<CNTROCls> targetTimes = processTargets(relations, ind);
		
		if ((targetTimes != null)&&(!targetTimes.isEmpty()))
		{
			for (int i=0; i < targetTimes.size(); i++)
			{
				TemporalRelation tr = null;
				CNTROCls trg = targetTimes.get(i);
		
				if (trg == null)
					continue;
		
				Event trgEvt = null;
				Time trgTime = CNTROUtils.getTimeFromCNTROCls(trg);
		
				if (trg instanceof Event)
				{
					trgEvt = (Event) trg;
					try {
						trgTime = trgEvt.getTime(false);
					} catch (CNTROException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		
				if ((trgTime == null)&&(trgEvt == null))
					continue;
		
				tr = new TemporalRelation(sourceEvent, relType, trgTime, trgEvt);
		
				if (tr != null)
				{
					sourceEvent.addTemporalRelation(tr);
					this.relationsHolder.add(tr);
				}
			}
		}
	}

	private List<RDFNode> getPropertyValues(Individual ind, Property prop)
	{
		List<RDFNode> vals = new ArrayList<RDFNode>();
		
		if ((ind == null)||(prop == null))
			return vals;
		
		NodeIterator nodeI = ind.listPropertyValues(prop);
		
		if ((nodeI == null)||(!nodeI.hasNext()))
			return vals;
		
		System.out.println("Ind:" + ind);
		System.out.println("Property:" + prop);
		while (nodeI.hasNext())
		{
			RDFNode node = nodeI.next();
			vals.add(node);
			if (node.isResource())
			{
				Resource res = node.asResource();
				System.out.println("Resource:" + res.toString());
			}
			else
			{
				System.out.println("Literal:" + node.asLiteral().getValue().toString());
			}
		}
		
		return vals;
	}
	
	public TemporalRelationType getTemporalRelationTypeForProperty(Property property)
	{
		if (property == after) return TemporalRelationType.AFTER;
		if (property == before) return TemporalRelationType.BEFORE;
		if (property == continuesThrough) return TemporalRelationType.CONTINUES_THROUGH;
		if (property == include) return TemporalRelationType.INCLUDE;
		if (property == initiate) return TemporalRelationType.INITIATE;
		if (property == isIncluded) return TemporalRelationType.IS_INCLUDED;
		if (property == overlap) return TemporalRelationType.OVERLAP;
		if (property == overlappedBy) return TemporalRelationType.OVERLAPPED_BY;
		if (property == simultaneous) return TemporalRelationType.SIMULTANEOUS;
		if (property == sameas) return TemporalRelationType.SIMULTANEOUS;
		if (property == terminate) return TemporalRelationType.TERMINATE;
		
		return null;
	}
}
