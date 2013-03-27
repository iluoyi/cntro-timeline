package edu.mayo.informatics.cntro.impl;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.main.CNTROConstants;
import edu.mayo.informatics.cntro.model.CNTROCls;
import edu.mayo.informatics.cntro.model.Duration;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.model.NormalizedDate;
import edu.mayo.informatics.cntro.model.TemporalOffset;
import edu.mayo.informatics.cntro.model.TemporalRelation;
import edu.mayo.informatics.cntro.model.Time;
import edu.mayo.informatics.cntro.model.TimeAssemblyMethod;
import edu.mayo.informatics.cntro.model.TimeInstant;
import edu.mayo.informatics.cntro.model.TimeInterval;
import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.queryIF.TemporalRelationType;
import edu.mayo.informatics.cntro.serviceIF.CNTROParser;
import edu.mayo.informatics.cntro.utils.CNTROMap;
import edu.mayo.informatics.cntro.utils.CNTROUtils;
import edu.mayo.informatics.cntro.utils.DateParser;
import edu.mayo.informatics.cntro.utils.EventsHolder;
import edu.mayo.informatics.cntro.utils.RelationsHolder;

public class CNTROOWLAPIParser implements CNTROParser 
{
	// Properties
	public OWLAnnotationProperty rdfLabel = null;
	public OWLObjectProperty hasValidTime = null;
	public OWLObjectProperty hasNoteTime = null;
	public OWLObjectProperty hasPeriod = null;
	public OWLObjectProperty temporalRelation = null;
	public OWLDataProperty hasOriginalTime = null;
	public OWLDataProperty hasNormalizedTime = null;
	public OWLDataProperty hasModality = null;
	
	public OWLObjectProperty after = null;
	public OWLObjectProperty before = null;
	public OWLObjectProperty meet = null;
	public OWLObjectProperty overlap = null;
	public OWLObjectProperty contain = null;
	public OWLObjectProperty during = null;
	public OWLObjectProperty equal = null;
	public OWLObjectProperty finish = null;
	public OWLObjectProperty start = null;
	
	public OWLObjectProperty continuesThrough = null;
	public OWLObjectProperty include = null;
	public OWLObjectProperty isIncluded = null;
	public OWLObjectProperty overlappedBy = null;
	public OWLObjectProperty initiate = null;
	public OWLObjectProperty simultaneous = null;
	public OWLObjectProperty terminate = null;
	public OWLObjectProperty sameas = null;
	
	public OWLObjectProperty temporalSubject = null;
	public OWLObjectProperty temporalObject = null;
	public OWLObjectProperty temporalPredicate = null;

	public OWLObjectProperty hasStartTime = null;
	public OWLObjectProperty hasEndTime = null;
	public OWLDataProperty hasDurationValue = null;
	public OWLObjectProperty hasOffset = null;
	public OWLAnnotationProperty hasTimeOffset = null;
	public OWLAnnotationProperty hasGranularity = null;
	public OWLObjectProperty hasDurationUnit = null;
	public OWLObjectProperty hasDuration = null;
	
	public OWLOntology ontology = null;
	public OWLOntologyManager manager = null;
	public OWLDataFactory df = null;
	
	public DateParser dateParser = new DateParser();
	public PelletReasoner reasoner = null;
	
	public Vector<OWLObjectProperty> temporalRelations = new Vector<OWLObjectProperty>();
	
	public boolean addNormalizedTimeValue = true;
	
	public EventsHolder eventsHolder = new EventsHolder();
	public RelationsHolder relationsHolder = new RelationsHolder();
	
	public boolean firstRun = true;

	public CNTROOWLAPIParser(OWLOntology ont)
	{
		if (ont == null)
		{
			System.out.println("!!!!!!!!!! Initialization Error!! ontology is null. Nothing will work !!!!!!!!!!!!!");
			return;
		}
		
		this.ontology = ont;
		df = ontology.getOWLOntologyManager().getOWLDataFactory();
		manager = ontology.getOWLOntologyManager();

		// load the ontology to the reasoner
		reasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner( this.ontology );

		if ((df == null)||(reasoner == null))
		{
			System.out.println("!!!!!!!!!! Initialization Error!! df/reasoner is null. Nothing will work !!!!!!!!!!!!!");
			return;
		}
		
		// Properties
		rdfLabel = df.getRDFSLabel();
		
		Hashtable<String, OWLObjectProperty> AllObjectProperties = new Hashtable<String, OWLObjectProperty>();
		Hashtable<String, OWLAnnotationProperty> AllAnnotationProperties = new Hashtable<String, OWLAnnotationProperty>();
		Hashtable<String, OWLDataProperty> AllDataProperties = new Hashtable<String, OWLDataProperty>();
		
		System.out.println("\n################################\n");
		Set<OWLObjectProperty> allObjectPros = ontology.getObjectPropertiesInSignature(true);
		
		for (OWLObjectProperty p : allObjectPros)
		{
			System.out.println("Object Property: " + p.getIRI());
			AllObjectProperties.put(getPropertyIdFromIRI(p.getIRI()), p);
		}
		
		System.out.println("\n################################\n");
		Set<OWLDataProperty> allDataPros = ontology.getDataPropertiesInSignature(true);
		
		for (OWLDataProperty dp : allDataPros)
		{
			System.out.println("Data Property: " + dp.getIRI());
			AllDataProperties.put(getPropertyIdFromIRI(dp.getIRI()), dp);
		}
		
		System.out.println("\n################################\n");
		Set<OWLAnnotationProperty> allAnnotPros = ontology.getAnnotationPropertiesInSignature();
		
		for (OWLAnnotationProperty ap : allAnnotPros)
		{
			System.out.println("Annotation Property: " + ap.getIRI());
			AllAnnotationProperties.put(getPropertyIdFromIRI(ap.getIRI()), ap);
		}

		System.out.println("\n################################\n");
		//All Object Properties
		hasValidTime = AllObjectProperties.get(CNTROConstants.CNTRO_HASVALIDTIME_PRP_NAME);
		hasNoteTime = AllObjectProperties.get(CNTROConstants.CNTRO_HASNOTETIME_PRP_NAME);
		hasPeriod = AllObjectProperties.get(CNTROConstants.CNTRO_HASNOTETIME_PRP_NAME);
		hasStartTime = AllObjectProperties.get(CNTROConstants.CNTRO_HASSTARTTIME_PRP_NAME);
		hasEndTime = AllObjectProperties.get(CNTROConstants.CNTRO_HASENDTIME_PRP_NAME);
		hasDuration = AllObjectProperties.get(CNTROConstants.CNTRO_HASDURATION_PRP_NAME);
		hasOffset = AllObjectProperties.get(CNTROConstants.CNTRO_HASOFFSET_PRP_NAME);
		hasDurationUnit = AllObjectProperties.get(CNTROConstants.CNTRO_HASDURATIONUNIT_PRP_NAME);
		
		temporalRelation = AllObjectProperties.get(CNTROConstants.CNTRO_TEMPORALRELATION_PRP_NAME);
		temporalSubject = AllObjectProperties.get(CNTROConstants.CNTRO_TR_TEMPORAL_SUBJECT_PRP_NAME);
		temporalObject = AllObjectProperties.get(CNTROConstants.CNTRO_TR_TEMPORAL_OBJECT_PRP_NAME);
		temporalPredicate = AllObjectProperties.get(CNTROConstants.CNTRO_TR_TEMPORAL_PREDICATE_PRP_NAME);
		
		after = AllObjectProperties.get(CNTROConstants.CNTRO_TR_AFTER_PRP_NAME);
		before = AllObjectProperties.get(CNTROConstants.CNTRO_TR_BEFORE_PRP_NAME);
		meet = AllObjectProperties.get(CNTROConstants.CNTRO_TR_MEET_PRP_NAME);
		overlap = AllObjectProperties.get(CNTROConstants.CNTRO_TR_OVERLAP_PRP_NAME);
		contain = AllObjectProperties.get(CNTROConstants.CNTRO_TR_CONTAIN_PRP_NAME);
		during = AllObjectProperties.get(CNTROConstants.CNTRO_TR_DURING_PRP_NAME);
		equal = AllObjectProperties.get(CNTROConstants.CNTRO_TR_EQUAL_PRP_NAME);
		finish = AllObjectProperties.get(CNTROConstants.CNTRO_TR_FINISH_PRP_NAME);
		start = AllObjectProperties.get(CNTROConstants.CNTRO_TR_START_PRP_NAME);
		
		temporalRelations.add(after);
		temporalRelations.add(before);
		temporalRelations.add(meet);
		temporalRelations.add(overlap);
		temporalRelations.add(contain);
		temporalRelations.add(during);
		temporalRelations.add(equal);
		temporalRelations.add(finish);
		temporalRelations.add(start);

		//All Data Properties
		hasDurationValue = AllDataProperties.get(CNTROConstants.CNTRO_HASDURATIONVALUE_PRP_NAME);
		hasOriginalTime = AllDataProperties.get(CNTROConstants.CNTRO_HASORIGINALTIME_PRP_NAME);
		hasNormalizedTime = AllDataProperties.get(CNTROConstants.CNTRO_HASNORMALIZEDTIME_PRP_NAME);
		hasModality = AllDataProperties.get(CNTROConstants.CNTRO_HASMODALITY_PRP_NAME);

		// All Annotation Properties
		hasTimeOffset = AllAnnotationProperties.get(CNTROConstants.CNTRO_HASTIMEOFFSET_PRP_NAME);
		hasGranularity = AllAnnotationProperties.get(CNTROConstants.CNTRO_HASGRANULARITY_PRP_NAME);
		
		System.out.println("Loaded All Properties!!");
		/*
		hasValidTime = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_HASVALIDTIME_PRP));
		hasNoteTime = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_HASNOTETIME_PRP));
		hasOriginalTime = df.getOWLDataProperty(IRI.create(CNTROConstants.CNTRO_HASORIGINALTIME_PRP));
		hasNormalizedTime = df.getOWLDataProperty(IRI.create(CNTROConstants.CNTRO_HASNORMALIZEDTIME_PRP));
		
		hasStartTime = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_HASSTARTTIME_PRP));
		hasEndTime = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_HASENDTIME_PRP));
		hasDuration = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_HASDURATION_PRP));
		hasDurationValue = df.getOWLDataProperty(IRI.create(CNTROConstants.CNTRO_HASDURATIONVALUE_PRP));
		hasOffset = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_HASOFFSET_PRP));
		hasTimeOffset = df.getOWLAnnotationProperty(IRI.create(CNTROConstants.CNTRO_HASTIMEOFFSET_PRP));
		hasDurationUnit = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_HASDURATIONUNIT_PRP));
		
		continuesThrough = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_CONTINUES_THROUGH_PRP));
		include = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_INCLUDE_PRP));
		isIncluded = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_IS_INCLUDED_PRP));
		overlappedBy = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_OVERLAPPED_BY_PRP));
		initiate = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_INITIATE_PRP));
		simultaneous = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_SIMULTANEOUS_PRP));
		terminate = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_TERMINATE_PRP));
		sameas = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_SAMEAS_PRP));
		
		temporalSubject = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_TEMPORAL_SUBJECT_PRP));
		temporalObject = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_TEMPORAL_OBJECT_PRP));
		temporalPredicate = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_TEMPORAL_PREDICATE_PRP));

		after = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_AFTER_PRP));
		before = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_BEFORE_PRP));
		meet = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_MEET_PRP));
		overlap = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_OVERLAP_PRP));
		contain = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_CONTAIN_PRP));
		during = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_DURING_PRP));
		equal = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_EQUAL_PRP));
		finish = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_FINISH_PRP));
		start = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TR_START_PRP));
		
		temporalRelation = df.getOWLObjectProperty(IRI.create(CNTROConstants.CNTRO_TEMPORALRELATION_PRP));
		
		for (OWLObjectProperty oe : allObjectPros)
		{
			// check if it coincides with any of the temporal relation types we have defined.
			if (getTemporalRelationTypeForProperty(oe) != null)
			{
				// See if temporal Relation has it
				if (!temporalRelations.contains(oe))
					temporalRelations.add(oe);
			}
		}
		*/
		
		/*
		temporalRelations.add(after);
		temporalRelations.add(before);
		temporalRelations.add(meet);
		temporalRelations.add(overlap);
		temporalRelations.add(contain);
		temporalRelations.add(during);
		temporalRelations.add(equal);
		temporalRelations.add(finish);
		temporalRelations.add(start);
		*/
		//temporalRelations.add(continuesThrough);
		//temporalRelations.add(include);
		//temporalRelations.add(isIncluded);
		
		//temporalRelations.add(overlappedBy);
		//temporalRelations.add(initiate);
		//temporalRelations.add(simultaneous);
		//temporalRelations.add(terminate);
		//temporalRelations.add(sameas);
	}
	
	private String getPropertyIdFromIRI(IRI propIRI)
	{
		if (propIRI == null)
			return null;
		
		String id = propIRI.toString();
		if (id.indexOf("#") != -1)
			id = (id.split("#"))[1];
		
		return id;
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
		
		OWLClass c = null;
		Set<OWLNamedIndividual> individuals = null;
		
		c = df.getOWLClass(IRI.create(CNTROConstants.CNTRO_TR_TEMPORAL_RELATION_STMT_CLS)); 

		individuals = reasoner.getInstances(c, false).getFlattened();
		for (OWLNamedIndividual ind : individuals)
		{
			if (ind != null)
			{
				//System.out.println("\n[####################################]\nProcessing TemporalRelationStatements....-->");
				//printOWLNamedIndividual(ind, this.ontology);
				String label = getAnnotationPropertyValue(ind, rdfLabel);
				//System.out.println("TRS=" + label);
				parseStatements(ind);
				//isReadingSmooth = ((evt != null) && isReadingSmooth);
			}
		}

		reasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner( this.ontology );
		
		c = df.getOWLClass(IRI.create(CNTROConstants.CNTRO_EVENT_CLS)); 

		individuals = reasoner.getInstances(c, false).getFlattened();
		for (OWLNamedIndividual ind : individuals)
		{
			if (ind != null)
			{
				//System.out.println("\n[####################################]\nProcessing Events....--> " + ind.toString());
				//printOWLNamedIndividual(ind, this.ontology);
				String label = getAnnotationPropertyValue(ind, rdfLabel);
				if (eventsHolder.getByLabel(label) == null)
				{
					Event evt = parseEvents(ind);
					isReadingSmooth = ((evt != null) && isReadingSmooth);
				}
				//else
				//	System.out.println("\nAlready processed:" + ind.toString());
			}
		}

		firstRun = false;
		
		c = df.getOWLClass(IRI.create(CNTROConstants.CNTRO_TR_TEMPORAL_RELATION_STMT_CLS)); 

		individuals = reasoner.getInstances(c, false).getFlattened();
		for (OWLNamedIndividual ind : individuals)
		{
			if (ind != null)
			{
				//System.out.println("\n[####################################]\nProcessing TemporalRelationStatements....-->");
				printOWLNamedIndividual(ind, this.ontology);
				String label = getAnnotationPropertyValue(ind, rdfLabel);
				//System.out.println("TRS=" + label);
				parseStatements(ind);
				//isReadingSmooth = ((evt != null) && isReadingSmooth);
			}
		}

		
		return isReadingSmooth;
	}

	public Event parseEvents(Object eventInstance) 
	{
		if ((eventInstance == null)||
			(!(eventInstance instanceof OWLNamedIndividual)))
			return null;
		
		OWLNamedIndividual owlInst = (OWLNamedIndividual) eventInstance;
				
		//System.out.println("\n<<<<<<<<<<<<<@@@@@@@@@@@@@@@@@@@@@@@@@>>>>>>>>>>>>\nProcessing Event \"" +  owlInst.getIRI() + "\"");
		String eventName= getAnnotationPropertyValue(owlInst, rdfLabel);
		
		Event retEvent = eventsHolder.getEventByLabel(eventName);
		
		if (retEvent != null)
			return retEvent;
		
		String eventCategory = eventName;
		
		Set<OWLClassExpression> eventTypes = owlInst.getTypes(this.ontology);
		
		if (eventTypes != null)
		{
			for (OWLClassExpression oe : eventTypes)
			{
				try
				{
					String typename = oe.asOWLClass().getIRI().getFragment();
					if (!CNTROConstants.CNTRO_EVENT_CLS_NAME.equals(typename))
						eventCategory = typename;
				}
				catch(Exception e)
				{
					continue;
				}
			}
		}
		
		retEvent = new Event(eventCategory);
		retEvent.description = eventName;
		retEvent.setClsId(eventName);
		eventsHolder.add(retEvent);
		
		readTimeProperties(retEvent, owlInst);
		readTemporalRelations(retEvent, owlInst);
		
		return retEvent;
	}

	public void parseStatements(Object stmtInstance) 
	{
		if ((stmtInstance == null)||
			(!(stmtInstance instanceof OWLNamedIndividual)))
			return;
		
		OWLNamedIndividual owlInst = (OWLNamedIndividual) stmtInstance;
		
		Event source = null;
		Event target = null;
		TemporalRelationType rel = null;
		Time time = null;

		String stmtName= getAnnotationPropertyValue(owlInst, rdfLabel);
		//System.out.println("Processing Statement:" + stmtName);
		
		Set<OWLNamedIndividual> subjects = getObjectPropertyValue(owlInst, temporalSubject);
		if ((subjects == null)||(subjects.isEmpty()))
		{
			//System.out.println("Subjects are Empty, Returning...");
			return;
		}
		
		OWLNamedIndividual sub = subjects.iterator().next();
		
		if (!firstRun)
		{
			if (sub != null)
			{
				String srclabel = getAnnotationPropertyValue(sub, rdfLabel);
				CNTROCls evt = eventsHolder.getByLabel(srclabel);
				if ((evt == null)||(!(evt instanceof Event)))
					return;
				source = (Event) evt;	
			}
		}
		
		Set<OWLNamedIndividual> objects = getObjectPropertyValue(owlInst, temporalObject);
		if ((objects == null)||(objects.isEmpty()))
		{
			//System.out.println("Objects are Empty, Returning...");
			return;
		}

		OWLNamedIndividual obj = objects.iterator().next();
		
		if (!firstRun)
		{
			if (obj != null)
			{
				String objlabel = getAnnotationPropertyValue(obj, rdfLabel);
				CNTROCls evt = eventsHolder.getByLabel(objlabel);
				if ((evt == null)||(!(evt instanceof Event)))
					return;
				target = (Event) evt;	
			}
		}
		
		Set<OWLNamedIndividual> predicates = getObjectPropertyValue(owlInst, temporalPredicate);
		if ((predicates == null)||(predicates.isEmpty()))
		{
			//System.out.println("Predicates are Empty, Returning...");
			return;
		}
		
		OWLNamedIndividual pred = predicates.iterator().next();
		
		if (pred != null)
		{
			String predlabel  = pred.getIRI().getFragment();
			rel = getTemporalRelationTypeForName(predlabel);	
		}

		
		//List<CNTROCls> times = processTargets(validTimes, owlInst);
		
		//if ((times != null)&&(!times.isEmpty()))
			//evt.eventTime = CNTROUtils.getTimeFromCNTROCls(times.get(0));
		
		if (firstRun)
		{
			OWLObjectProperty relationProp = getObjectPropertyForTemporalRelationType(rel);
			
			if ((sub != null)&&(obj != null)&&(relationProp != null))
			{
				OWLObjectPropertyAssertionAxiom opAssertion = df.getOWLObjectPropertyAssertionAxiom(relationProp, sub, obj);
				AddAxiom addOPAxiomChange = new AddAxiom(ontology, opAssertion);
				manager.applyChange(addOPAxiomChange);
			}
		}
		if (!firstRun)
		{
			//System.out.println("Subject:" + source.getClsId());
			//System.out.println("Object:" + target.getClsId());
			//System.out.println("Predicate:" + rel);

			Set<OWLNamedIndividual> offset = getObjectPropertyValue(owlInst, hasOffset);

			if ((offset == null)||(offset.isEmpty()))
			{
				// try to find out offset that might not have carried over 
				// in case of inferred (reverse direction) temporal relation
				
				try 
				{
					List<TemporalRelation> reverseRels = target.getTemporalRelationByRelation(source);
					Set<TemporalOffset> to = new HashSet<TemporalOffset>();

					for (TemporalRelation rtr : reverseRels)
					{
						if (rtr.offset != null)
						{
							addTemporalRelation(source, target, rel, rtr.offset);
							// Add same offset to all other events which might be same as target here.
							
							List<TemporalRelationType> types = new ArrayList<TemporalRelationType>();
							types.add(TemporalRelationType.EQUAL);
							types.add(TemporalRelationType.SAMEAS);
							types.add(TemporalRelationType.SIMULTANEOUS);
	
							List<TemporalRelation> sameEventsAsTarget = target.getTemporalRelationByRelationType(types);
							for (TemporalRelation se : sameEventsAsTarget)
							{
								if ((se.targetEvent != null)&&(!se.targetEvent.description.equals(target.description)))
								{
									List<TemporalRelation> sameRelationsWithSrc = se.targetEvent.getTemporalRelationByRelation(source);
									
									for (TemporalRelation smtr : sameRelationsWithSrc)
									{
										if (smtr.offset == null)
											smtr.offset = rtr.offset;
									}
								}
							}
						}
					}
				} 
				catch (CNTROException e) 
				{
					//System.out.println("Predicates are Empty, Returning...");
				}
			}
			else
			{
				OWLNamedIndividual duration = offset.iterator().next();
				if (duration != null)
				{
					TemporalOffset to = new TemporalOffset();
					//System.out.println("Offset:Duration:" + duration);
					String durlabel = getAnnotationPropertyValue(duration, rdfLabel);
					to.label = durlabel;
					
					Set<OWLLiteral> durationValues = getDataPropertyValue(duration, hasDurationValue);
					if ((durationValues != null)&&(!durationValues.isEmpty()))
					{
						to.value = Integer.parseInt(durationValues.iterator().next().getLiteral());
						//System.out.println("Duration Value=" + to.value);
					}
	
					Set<OWLNamedIndividual> durationUnits = getObjectPropertyValue(duration, hasDurationUnit);
					if ((durationUnits == null)||(durationUnits.isEmpty()))
					{
						//System.out.println("Duration Unit is Empty.");
					}
					else
						to.unit = CNTROUtils.getGranularityFromString(durationUnits.iterator().next().toString());
					
					//System.out.println("Duration Units=" + durationUnits.iterator().next().toString());
					
					addTemporalRelation(source, target, rel, to);
					
					// try to find out offset that might not have carried over 
					// in case of inferred (reverse direction) temporal relation
					try 
					{
						List<TemporalRelation> reverseRels = target.getTemporalRelationByRelation(source);

						for (TemporalRelation rtr : reverseRels)
						{
							if (rtr.offset == null)
								rtr.offset = to;
							
							List<TemporalRelationType> types = new ArrayList<TemporalRelationType>();
							types.add(TemporalRelationType.EQUAL);
							types.add(TemporalRelationType.SAMEAS);
							types.add(TemporalRelationType.SIMULTANEOUS);
	
							List<TemporalRelation> sameEventsAsTarget = target.getTemporalRelationByRelationType(types);
							for (TemporalRelation se : sameEventsAsTarget)
							{
								if ((se.targetEvent != null)&&(!se.targetEvent.description.equals(target.description)))
								{
									List<TemporalRelation> sameRelationsWithSrc = se.targetEvent.getTemporalRelationByRelation(source);
									
									for (TemporalRelation smtr : sameRelationsWithSrc)
									{
										if (smtr.offset == null)
											smtr.offset = rtr.offset;
									}
								}
							}
						}
					} 
					catch (CNTROException e) 
					{
						System.out.println("Warning: failed to find reverse relations. not harmful.");
					}

				}
			}
		}
		return;
	}

	private void readTimeProperties(Event evt, OWLNamedIndividual owlInst)
	{
		Set<OWLNamedIndividual> validTimes = getObjectPropertyValue(owlInst, hasValidTime);
		List<CNTROCls> times = processTargets(validTimes, owlInst);
		
		if ((times != null)&&(!times.isEmpty()))
			evt.eventTime = CNTROUtils.getTimeFromCNTROCls(times.get(0)); 
		
		Set<OWLNamedIndividual> noteTimes = getObjectPropertyValue(owlInst, hasNoteTime);
		times = processTargets(noteTimes, owlInst);
		if ((times != null)&&(!times.isEmpty()))
			evt.noteTime = CNTROUtils.getTimeFromCNTROCls(times.get(0));
		
		Set<OWLNamedIndividual> durations = getObjectPropertyValue(owlInst, hasDuration);
		
		if ((durations != null)&&(!durations.isEmpty()))
		{
			Duration dur = processDuration(durations, owlInst);
			
			if (dur != null)
			{
				TimeInterval tintv = new TimeInterval(TimeAssemblyMethod.INFERRED);
				
				if (evt.eventTime != null)
					tintv.setStartTime(evt.eventTime);
				
				tintv.setDuration(dur);
				evt.eventTime = tintv;
			}
		}
	}
	
	private void readTemporalRelations(Event evt, OWLNamedIndividual owlInst)
	{
		if (owlInst == null)
			return;
		
		if (evt == null)
			return;
		
		Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objectPropertiesOfInst = owlInst.getObjectPropertyValues(this.ontology);
		//System.out.println("Properties:" + objectPropertiesOfInst.size());

		for (OWLObjectPropertyExpression ope : objectPropertiesOfInst.keySet())
		{
			OWLObjectProperty currentProperty = ope.asOWLObjectProperty();
			
			if (!temporalRelations.contains(currentProperty))
			{
				//System.out.println("Ignored event's property:" + currentProperty.getIRI());
				continue;
			}
			
			Set<OWLNamedIndividual> relations = getObjectPropertyValue(owlInst, currentProperty);
			
			if (relations != null)
			{
				TemporalRelationType relType = getTemporalRelationTypeForProperty(currentProperty);
				populateRelations(relations, owlInst, evt, relType);
			}
			
		}
		
		Set<OWLIndividual> sameAsInstances = owlInst.getSameIndividuals(this.ontology);
		
		if (sameAsInstances != null)
		{
			for (OWLIndividual sameAsInst : sameAsInstances)
			{
				if (sameAsInst != null)
				{
					Set<OWLNamedIndividual> sameAsNamedInst = sameAsInst.getIndividualsInSignature();
					
					for (OWLNamedIndividual oni : sameAsNamedInst)
					{
						String sameAsEvtName = getAnnotationPropertyValue(oni, rdfLabel);
						
						CNTROCls sameAsEvt = eventsHolder.getByLabel(sameAsEvtName);
						
						if ((sameAsEvt == null)||(!(sameAsEvt instanceof Event)))
							continue;
						
						Event sae = (Event) sameAsEvt;
						
						if (sae != null)
						{
							Time toSetTime = null;
							try
							{
								if (evt.getTime(false) == null)
									toSetTime = sae.getTime(false);
							}
							catch(Exception e)
							{
								System.out.println("Warning: failed to find time for Same As relation with event:" + sae.description);
							}
							
							addTemporalRelation(evt, sae, TemporalRelationType.SAMEAS, toSetTime);
						}
					}
				}
			}
		}
		
		/*
		for (OWLObjectProperty currentProperty : objectPropertiesOfInst)
		{
			if (!temporalRelations.contains(currentProperty))
				System.out.println("Ignored event's property:" + currentProperty.getIRI());
			
			Set<OWLNamedIndividual> relations = getObjectPropertyValue(owlInst, currentProperty);
			
			if (relations != null)
			{
				TemporalRelationType relType = getTemporalRelationTypeForProperty(currentProperty);
				populateRelations(relations, owlInst, evt, relType);
			}
		}
		*/
		/*
		for (int i=0; i < temporalRelations.size(); i++)
		{
			OWLObjectProperty currentProperty = temporalRelations.elementAt(i);
			Set<OWLNamedIndividual> relations = getObjectPropertyValue(owlInst, currentProperty);
		
			if (relations != null)
			{
				TemporalRelationType relType = getTemporalRelationTypeForProperty(currentProperty);
				populateRelations(relations, owlInst, evt, relType);
			}
		}
		*/
	}
	
	private boolean containsType(Set<OWLClassExpression> typeCollection, String type)
	{
		if (typeCollection == null)
			return false;
		
		for (OWLClassExpression ce :typeCollection)
		{
			if (ce.toString().indexOf(type) != -1)
				return true;
		}
			
		return false;
	}
	
	private Set<OWLClassExpression> getCNTROSuperClasses(OWLClassExpression currentClassExpression)
	{
		if (currentClassExpression == null)
			return null;
		
		OWLClass currentClass = currentClassExpression.asOWLClass();
		
		String curi = currentClass.getIRI().toString();
		if ((curi.indexOf("Thing") != -1)||
			(curi.indexOf(CNTROConstants.CNTRO_EVENT_CLS) != -1)||
			(curi.indexOf(CNTROConstants.CNTRO_TIMEINSTANCE_CLS) != -1)||
			(curi.indexOf(CNTROConstants.CNTRO_TIMEINTERVAL_CLS) != -1)||
			(curi.indexOf(CNTROConstants.CNTRO_TIMEPERIOD_CLS) != -1)||
			(curi.indexOf(CNTROConstants.CNTRO_TIMEPHASE_CLS) != -1))
		{
				// No need to find super classes
				Set<OWLClassExpression> itself = new HashSet<OWLClassExpression>();
				itself.add(currentClassExpression);
				return itself;
		}
		
		//System.out.println("Trying to find superclasses of " + currentClass.getIRI());
		Set<OWLClassExpression> superClasses = currentClass.getSuperClasses(ontology);
		
		Set<OWLClassExpression> importedscls = currentClass.getSuperClasses(ontology.getImports());
		superClasses.addAll(importedscls);
		
		//System.out.println("SuperClasses:" + superClasses.size());
		
		//for (OWLClassExpression sc : importedscls)
		//{
		//	System.out.println("\tSuperClass: " + sc);
		//}
			
		if (containsType(superClasses, "Thing")||
			containsType(superClasses, CNTROConstants.CNTRO_EVENT_CLS)||
			containsType(superClasses, CNTROConstants.CNTRO_TIMEINSTANCE_CLS)||
			containsType(superClasses, CNTROConstants.CNTRO_TIMEINTERVAL_CLS)||
			containsType(superClasses, CNTROConstants.CNTRO_TIMEPERIOD_CLS)||
			containsType(superClasses, CNTROConstants.CNTRO_TIMEPHASE_CLS))
		{
			return superClasses;
		}
		else
		{
			for (OWLClassExpression sc1 : superClasses)
			{
				if (sc1.asOWLClass().isOWLClass())
				{
					Set<OWLClassExpression> moreSuperClasses = getCNTROSuperClasses(sc1);
					if (moreSuperClasses != null)
						superClasses.addAll(moreSuperClasses);
					
					if (containsType(moreSuperClasses, "Thing")||
							containsType(moreSuperClasses, CNTROConstants.CNTRO_EVENT_CLS)||
							containsType(moreSuperClasses, CNTROConstants.CNTRO_TIMEINSTANCE_CLS)||
							containsType(moreSuperClasses, CNTROConstants.CNTRO_TIMEINTERVAL_CLS)||
							containsType(moreSuperClasses, CNTROConstants.CNTRO_TIMEPERIOD_CLS)||
							containsType(moreSuperClasses, CNTROConstants.CNTRO_TIMEPHASE_CLS))
						{
							return superClasses;
						}
				}
			}
		}
		
		return superClasses;
	}
	
	private List<CNTROCls> processTargets(Set<OWLNamedIndividual> objProp, OWLNamedIndividual owlInst)
	{
		List<CNTROCls> retTargets = new ArrayList<CNTROCls>();
		
		if ((objProp == null)||(objProp.isEmpty()))
			return null;
		
		for (OWLNamedIndividual target : objProp)
		{
			// Get all the super classes of target object
			// to figure out its temporal type : Event, Time instant or ,....
			Set<OWLClassExpression> superClasses = null;
			Set<OWLClassExpression> oiTypes = target.getTypes(ontology);
			for (OWLClassExpression oce : oiTypes)
			{
				superClasses = getCNTROSuperClasses(oce);
				if ((superClasses != null)&&(!superClasses.isEmpty()))
					break;
			}
			
			String labelAsTime = getAnnotationPropertyValue(target, rdfLabel);
			TimeInstant tiFromLabel = getTimeInstantFromLabel(target);
			
			// If this is an event
			if (containsType(superClasses, CNTROConstants.CNTRO_EVENT_CLS))
			{
					Event event = this.eventsHolder.getEventByLabel(labelAsTime);
					
					if (event == null)
						event = this.parseEvents(target);
					
					if (event == null)
						continue;
					
					retTargets.add(event);
			}
			else
			{
				// if this is a time instant
				if (containsType(superClasses, CNTROConstants.CNTRO_TIMEINSTANCE_CLS))
				{
					TimeInstant tinst = new TimeInstant();
					
					Set<OWLLiteral> originalTimeValues = getDataPropertyValue(target, hasOriginalTime);
					if ((originalTimeValues != null)&&(!originalTimeValues.isEmpty()))
					{
						for (Iterator<OWLLiteral> dataPropertyValueItr = originalTimeValues.iterator(); dataPropertyValueItr.hasNext();)
						{
							OWLLiteral originalTimeValue = dataPropertyValueItr.next();
							String originalTime = originalTimeValue.getLiteral();
							tinst.granularity = CNTROUtils.getGranularityFromDateString(originalTime);
							tinst.setOriginalTime(originalTime);
							
							if (addNormalizedTimeValue)
							{
								NormalizedDate nd = normalize(originalTime, target);
								
								if (nd != null)
								{
									tinst.setNormalizedTime(nd.normalizedDate);
									tinst.granularity = nd.getNormalizedGranularity();
								}
							}
						}
					}
						
					if (tiFromLabel != null)
						CNTROUtils.copyTimeInstantValuesIfNull(tiFromLabel, tinst);
						
					retTargets.add(tinst);
				}
				else
				{
					if (containsType(superClasses, CNTROConstants.CNTRO_TIMEINTERVAL_CLS))
					{
						TimeInterval tintv = new TimeInterval(TimeAssemblyMethod.ASSERTED);
							
						Set<OWLNamedIndividual> startTime = getObjectPropertyValue(target, hasStartTime);
						List<CNTROCls> times = processTargets(startTime, target);
							
						if ((times != null)&&(!times.isEmpty()))
							tintv.setStartTime(CNTROUtils.getTimeFromCNTROCls(times.get(0)));
							
						Set<OWLNamedIndividual> endTime = getObjectPropertyValue(target, hasEndTime);
						times = processTargets(endTime, target);
						if ((times != null)&&(!times.isEmpty()))
							tintv.setEndTime(CNTROUtils.getTimeFromCNTROCls(times.get(0))); 
							
						Set<OWLNamedIndividual> durations = getObjectPropertyValue(target, hasDuration);
						tintv.setDuration(processDuration(durations, target));
							
						retTargets.add(tintv);
					}
					else
					{
						if (containsType(superClasses, CNTROConstants.CNTRO_TIMEPERIOD_CLS))
						{
							System.out.println("TIME PERIOD TYPE HANDLING NOT IMPLEMENTED YET!!");
						}
						else
						{
							if (containsType(superClasses, CNTROConstants.CNTRO_TIMEPHASE_CLS))
							{
								System.out.println("TIME PHASE TYPE HANDLING NOT IMPLEMENTED YET!!");
							}
						}
					}
				}
			}
			
			
			// Add offset here from annotation properties of this relation
			/*
			String labelAsTime2 = getAnnotationPropertyValue(owlInst, rdfLabel);
			Set<OWLIndividual> duras = owlInst.getObjectPropertyValues(hasDuration, this.ontology);
			
			String ds = "";
			for (OWLIndividual dur :duras)
				ds = getAnnotationPropertyValue(dur.asOWLNamedIndividual(), rdfLabel);
			
			OWLObjectPropertyAssertionAxiom annotsI = df.getOWLObjectPropertyAssertionAxiom(before, owlInst, target);
			OWLAxiom oa = annotsI.getNNF();

			Object at = annotsI.getSimplified();
			Set<OWLAnnotation> vala = annotsI.getAnnotations();
					for(OWLAnnotation va : vala)
					{
						System.out.println("here is the value" + va.getValue().toString());
					}
			String offsetValue = getAnnotationPropertyValue(owlInst, hasTimeOffset);
			
			if (offsetValue != null)
				System.out.println("here");
				*/
		}
		
		return retTargets;
	}

	public String getAnnotationPropertyValue(OWLNamedIndividual pI, OWLAnnotationProperty annProperty)
	{
		if ((pI == null)||
			(ontology == null)||
			(annProperty == null))
			return null;

		Set<OWLAnnotation> annotations = pI.getAnnotations(ontology, annProperty);

		for (OWLAnnotation ann : annotations) 
		{
			if ((ann.getValue() != null)&&
				(!CNTROUtils.isNull(ann.getValue().toString())))
			{
				String value = ann.getValue().toString();
				return CNTROUtils.getStringValueWithinQuotes(value);
			}
		}

		return null;
	}
	
	public Set<OWLNamedIndividual> getObjectPropertyValue(OWLNamedIndividual pI, OWLObjectProperty objProperty)
	{
		if ((pI == null)||(objProperty == null))
			return null;

		Set<OWLNamedIndividual> propList = reasoner.getObjectPropertyValues(pI, objProperty).getFlattened();
		
		/*
		Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objProperties = pI.getObjectPropertyValues(ontology);

		if (objProperties.containsKey(objProperty))
		{
			//System.out.println("Found property:" + objProperty.toString());
			return objProperties.get(objProperty);
		}

		return null;
		*/
		
		return propList;
	}
	
	public Set<OWLLiteral> getDataPropertyValue(OWLNamedIndividual pI, 
			   OWLDataProperty dataProperty)
	{
		if ((pI == null)||(dataProperty == null))
			return null;

		//Set<OWLLiteral> dataProperties = reasoner.getDataPropertyValues(pI, dataProperty);
		
		Map<OWLDataPropertyExpression, Set<OWLLiteral>> dataProperties = pI.getDataPropertyValues(ontology);

		if (dataProperties.containsKey(dataProperty))
		{
			//System.out.println("Found data property:" + dataProperty.toString());
			return dataProperties.get(dataProperty);
		}

		return null;
		
		
		//return dataProperties;
	}
	
	public Date NormalizeTimeComponents(OWLNamedIndividual ind) 
	{
		Date normDate = null;
		Set<OWLNamedIndividual> objProp = getObjectPropertyValue(ind, hasValidTime);
		
		if ((objProp == null)||(objProp.isEmpty()))
			return null;
		
		for (Iterator<OWLNamedIndividual> objectPropertyItr = objProp.iterator(); objectPropertyItr.hasNext();)
		{
			OWLNamedIndividual oi = objectPropertyItr.next();
			if (oi == null)
				continue;
			
			Set<OWLClassExpression> oiTypes = oi.getTypes(ontology);
			if ((oiTypes == null)||(oiTypes.isEmpty()))
				continue;
			for (Iterator<OWLClassExpression> objectPropertyTypeItr = oiTypes.iterator(); objectPropertyTypeItr.hasNext();)
			{
				OWLClassExpression owlInstanceTypeObj = objectPropertyTypeItr.next();

				if ((owlInstanceTypeObj != null)&&(owlInstanceTypeObj.toString().contains(CNTROConstants.CNTRO_TIMEINSTANCE_CLS)))
				{
					Set<OWLLiteral> originalTimeValues = getDataPropertyValue(oi, hasOriginalTime);
					for (Iterator<OWLLiteral> dataPropertyValueItr = originalTimeValues.iterator(); dataPropertyValueItr.hasNext();)
					{
						OWLLiteral originalTimeValue = dataPropertyValueItr.next();
						String originalTime = originalTimeValue.getLiteral();
						Date normalizedDate = dateParser.parse(originalTime, new ParsePosition(0));
						if (normalizedDate == null)
						{
							//System.out.println("Normalized Date is null for:" + originalTime);
							continue;
						}
						normDate = normalizedDate;
						String normalizedTime = dateParser.normaltoUSDateString(normalizedDate);
						
						if (CNTROUtils.isNull(normalizedTime))
							continue;
						
						//System.out.println("Original Time:" + originalTime);
						//System.out.println("Normalized Time:" + normalizedTime);
						
						OWLDataPropertyAssertionAxiom assertion = df.getOWLDataPropertyAssertionAxiom(hasNormalizedTime, ind, normalizedTime);
						AddAxiom addAxiomChange = new AddAxiom(ontology, assertion);
						manager.applyChange(addAxiomChange);
					}
				}
			}
		}
		
		return normDate;
	}
	
	private Duration processDuration(Set<OWLNamedIndividual> objProp, OWLNamedIndividual owlInst)
	{
		Duration retDuration = null;
		
		if ((objProp == null)||(objProp.isEmpty()))
			return null;
				
		for (OWLNamedIndividual oi : objProp)
		{
			if (oi == null)
				continue;
			
			Set<OWLClassExpression> oiTypes = oi.getTypes(ontology);
			if ((oiTypes == null)||(oiTypes.isEmpty()))
				continue;
			
			TimeInstant tiFromLabel = getTimeInstantFromLabel(oi);
			
			for (OWLClassExpression owlInstanceTypeObj : oiTypes)
			{
				if (owlInstanceTypeObj == null)
					continue;
				
				if (owlInstanceTypeObj.toString().contains(CNTROConstants.CNTRO_DURATION_CLS))
				{
					retDuration = new Duration();

					boolean durationUnitSet = false;
					Set<OWLNamedIndividual> durationUnits = getObjectPropertyValue(oi, hasDurationUnit);
					if ((durationUnits != null)&&(!durationUnits.isEmpty()))
					{
						for (OWLNamedIndividual du : durationUnits)
						{
							String dv = du.getIRI().toString();
							retDuration.unit = CNTROUtils.getGranularityFromString(dv);
							durationUnitSet = true;
						}
					}

					boolean durationValueSet = false;
					Set<OWLLiteral> durationValues = getDataPropertyValue(oi, hasDurationValue);
					if ((durationValues != null)&&(!durationValues.isEmpty()))
					{
						for (Iterator<OWLLiteral> dataPropertyValueItr = durationValues.iterator(); dataPropertyValueItr.hasNext();)
						{
							OWLLiteral durationValue = dataPropertyValueItr.next();
							String dv = durationValue.getLiteral();
							try
							{
								retDuration.value = CNTROUtils.getNumericValueFromString(dv);
								durationValueSet = true;
							}
							catch(Exception e)
							{
								
							}
						}
					}
					
					if (tiFromLabel != null)
					{
						if ((!durationUnitSet)||(retDuration.unit == Granularity.UNKNOWN))
							retDuration.unit = CNTROUtils.getGranularityFromString(tiFromLabel.label);
						
						if (!durationValueSet)
						{
							try
							{
								long nv = CNTROUtils.getNumericValueFromString(tiFromLabel.label);
								retDuration.value =  nv;
							}
							catch(Exception e)
							{
								
							}
						}
						
						if (CNTROUtils.isNull(retDuration.label))
							retDuration.label = tiFromLabel.label;
					}	
				}
			}
		}
		
		return retDuration;
	}
	
	private void  populateRelations(Set<OWLNamedIndividual> relations, 
														OWLNamedIndividual owlInst, 
														Event sourceEvent, 
														TemporalRelationType relType)
	{
		List<CNTROCls> targetTimes = processTargets(relations, owlInst);
		
		if ((targetTimes != null)&&(!targetTimes.isEmpty()))
		{
			for (int i=0; i < targetTimes.size(); i++)
			{
				CNTROCls trg = targetTimes.get(i);
				
				if (trg == null)
					continue;
				
				Event trgEvt = null;
				Time trgTime = CNTROUtils.getTimeFromCNTROCls(trg);
				
				if (trg instanceof Event)
				{
					trgEvt = (Event) trg;
					try
					{
						trgTime = trgEvt.getTime(false);
					}
					catch(Exception e)
					{
						//System.out.println("Failed to get Time for event(while populating relations):" + trgEvt.description);
					}
				}
				
				if ((trgTime == null)&&(trgEvt == null))
					continue;
				
				TemporalOffset to = null;
				boolean added = false;
				Set<OWLObjectPropertyAssertionAxiom> assertions = this.ontology.getObjectPropertyAssertionAxioms(owlInst);
				for (OWLObjectPropertyAssertionAxiom oax : assertions)
				{
					if (oax.getObject() == null)
						continue;

					OWLNamedIndividual targetEvent = oax.getObject().asOWLNamedIndividual();
					String eventName = getAnnotationPropertyValue(targetEvent, rdfLabel);
						
					if (!eventName.equalsIgnoreCase(trg.getClsId()))
						continue;
					
					
					Set<OWLAnnotation> vala = oax.getAnnotations(hasTimeOffset);
					for(OWLAnnotation va : vala)
					{
						if (va == null)
							continue;
						
						OWLAnnotationValue oav = va.getValue();
						
						if (oav == null)
							continue;
							
						String timeOffsetLabel = oav.toString();
						if (oav instanceof OWLNamedIndividual)
							timeOffsetLabel = getAnnotationPropertyValue((OWLNamedIndividual) oav, rdfLabel);
						else
							if (oav instanceof OWLIndividual)
								timeOffsetLabel = getAnnotationPropertyValue((((OWLIndividual) oav).asOWLNamedIndividual()), rdfLabel);
							else
							{
								try
								{
									OWLNamedIndividual referredResource = df.getOWLNamedIndividual(IRI.create(timeOffsetLabel));
									timeOffsetLabel = getAnnotationPropertyValue(referredResource, rdfLabel);
								}
								catch(Exception e){}
							}
						
						to = new TemporalOffset();
						to.label = timeOffsetLabel;
						
						try
						{
							to.value = CNTROUtils.getNumericValueFromString(timeOffsetLabel);
							to.unit = CNTROUtils.getGranularityFromString(timeOffsetLabel);
						}
						catch(Exception e)
						{
							
						}
						
						if (trgTime instanceof TimeInterval)
							((TimeInterval)trgTime).setDuration(to);
						else
							if ((trgTime == null)||(trgTime instanceof TimeInstant))
							{
								TimeInterval titemp = new TimeInterval(trgTime, null, to, TimeAssemblyMethod.INFERRED);
								trgTime = titemp;
							}
					}
					
					addTemporalRelation(sourceEvent, trgEvt, relType, trgTime);

					if (to != null)
						addTemporalRelation(sourceEvent, trgEvt, relType, to);
					
					added = true;
				}
				
				if (!added)
					addTemporalRelation(sourceEvent, trgEvt, relType, trgTime);
			}
		}
	}

	private void addTemporalRelation(Event source, Event target, TemporalRelationType relation, Time time)
	{
		if ((source == null)||
			(target == null)||
			(relation == null))
			return;
		
		if (source.description.equals(target.description))
		{
			//System.out.println("Skipping temporal relation (may be inferred): as source and events are same.[" + source.description + "]");
			return;
		}

		TemporalRelation tr = new TemporalRelation(source, relation, time, target);
		
		if (tr != null)
		{
			boolean found = false;
			
			Vector<TemporalRelation> sourceEvtrelations = source.getTemporalRelations();
			for (int i=0; i < sourceEvtrelations.size(); i++)
				if (sourceEvtrelations.elementAt(i).getClsId().equals(tr.getClsId()))
					found = true;
			
			if ((sourceEvtrelations.isEmpty())||(!found))
				source.addTemporalRelation(tr);

			this.relationsHolder.add(tr);
		}
	}

	private void addTemporalRelation(Event source, Event target, TemporalRelationType relation, TemporalOffset offset)
	{
		if ((source == null)||
			(target == null)||
			(relation == null))
			return;
		
		if (source.description.equals(target.description))
		{
			//System.out.println("Skipping temporal relation (may be inferred): as source and events are same.[" + source.description + "]");
			return;
		}
		
		TemporalRelation tr = new TemporalRelation(source, relation, null, target, offset);
		
		if (tr != null)
		{
			/*
			boolean found = false;
			int index = -1;
			
			Vector<TemporalRelation> sourceEvtrelations = source.getTemporalRelations();
			for (int i=0; i < sourceEvtrelations.size(); i++)
				if (sourceEvtrelations.elementAt(i).getClsId().equals(tr.getClsId()))
				{
					found = true;
					index = i;
				}

			
			if ((sourceEvtrelations.isEmpty())||(!found))
			*/
			tr.offset = offset;
			source.addTemporalRelation(tr);

			this.relationsHolder.add(tr);
		}
	}

	public TemporalRelationType getTemporalRelationTypeForProperty(OWLObjectProperty property)
	{
		String iri = property.getIRI().toString();
		
		if (iri.indexOf("after") != -1) return TemporalRelationType.AFTER;
		if (iri.indexOf("before") != -1) return TemporalRelationType.BEFORE;
		if (iri.indexOf("meet") != -1) return TemporalRelationType.MEET;
		if (iri.indexOf("overlap") != -1) return TemporalRelationType.OVERLAP;
		if (iri.indexOf("contain") != -1) return TemporalRelationType.CONTAIN;
		if (iri.indexOf("during") != -1) return TemporalRelationType.DURING;
		if (iri.indexOf("equal") != -1) return TemporalRelationType.EQUAL;
		if (iri.indexOf("finish") != -1) return TemporalRelationType.FINISH;
		if (iri.indexOf("start") != -1) return TemporalRelationType.START;
		
		if (property == continuesThrough) return TemporalRelationType.CONTINUES_THROUGH;
		if (property == include) return TemporalRelationType.INCLUDE;
		if (property == initiate) return TemporalRelationType.INITIATE;
		if (property == isIncluded) return TemporalRelationType.IS_INCLUDED;
		if (property == overlappedBy) return TemporalRelationType.OVERLAPPED_BY;
		if (property == simultaneous) return TemporalRelationType.SIMULTANEOUS;
		if (property == terminate) return TemporalRelationType.TERMINATE;
		if (property == sameas) return TemporalRelationType.SAMEAS;
		
		return null;
	}

	public OWLObjectProperty getObjectPropertyForTemporalRelationType(TemporalRelationType type)
	{
		String toSearch = "nothing";
		switch(type)
		{
			case AFTER: toSearch = "after"; break;
			case BEFORE: toSearch = "before"; break;
			case MEET: toSearch = "meet"; break;
			case OVERLAP: toSearch = "overlap"; break;
			case CONTAIN: toSearch = "contain"; break;
			case DURING: toSearch = "during"; break;
			case EQUAL: toSearch = "equal"; break;
			case FINISH: toSearch = "finish"; break;
			case START: toSearch = "start"; break;
			
			case CONTINUES_THROUGH: return continuesThrough;
			case INCLUDE: return include;
			case INITIATE: return initiate;
			case IS_INCLUDED: return isIncluded;
			case OVERLAPPED_BY: return overlappedBy;
			case SIMULTANEOUS: return simultaneous;
			case TERMINATE: return terminate;
			case SAMEAS: return sameas;
		}

		for (OWLObjectProperty op :this.temporalRelations)
			if (op.getIRI().toString().indexOf(toSearch) != -1)
				return op;
				
				
		return null;
	}
	
	public TemporalRelationType getTemporalRelationTypeForName(String rel)
	{
		if (rel == null)
			return null;
		
		if (rel.equals(CNTROConstants.CNTRO_TR_AFTER_PRP_NAME)) return TemporalRelationType.AFTER;
		if (rel.equals(CNTROConstants.CNTRO_TR_BEFORE_PRP_NAME)) return TemporalRelationType.BEFORE;
		if (rel.equals(CNTROConstants.CNTRO_TR_OVERLAP_PRP_NAME)) return TemporalRelationType.OVERLAP;
		if (rel.equals(CNTROConstants.CNTRO_TR_MEET_PRP_NAME)) return TemporalRelationType.MEET;
		if (rel.equals(CNTROConstants.CNTRO_TR_CONTAIN_PRP_NAME)) return TemporalRelationType.CONTAIN;
		if (rel.equals(CNTROConstants.CNTRO_TR_DURING_PRP_NAME)) return TemporalRelationType.DURING;
		if (rel.equals(CNTROConstants.CNTRO_TR_START_PRP_NAME)) return TemporalRelationType.START;
		if (rel.equals(CNTROConstants.CNTRO_TR_FINISH_PRP_NAME)) return TemporalRelationType.FINISH;
		if (rel.equals(CNTROConstants.CNTRO_TR_EQUAL_PRP_NAME)) return TemporalRelationType.EQUAL;
		
		if (rel.equals(CNTROConstants.CNTRO_TR_CONTINUES_THROUGH_PRP_NAME)) return TemporalRelationType.CONTINUES_THROUGH;
		if (rel.equals(CNTROConstants.CNTRO_TR_INCLUDE_PRP_NAME)) return TemporalRelationType.INCLUDE;
		if (rel.equals(CNTROConstants.CNTRO_TR_INITIATE_PRP_NAME)) return TemporalRelationType.INITIATE;
		if (rel.equals(CNTROConstants.CNTRO_TR_IS_INCLUDED_PRP_NAME)) return TemporalRelationType.IS_INCLUDED;
		if (rel.equals(CNTROConstants.CNTRO_TR_OVERLAPPED_BY_PRP_NAME)) return TemporalRelationType.OVERLAPPED_BY;
		if (rel.equals(CNTROConstants.CNTRO_TR_SIMULTANEOUS_PRP_NAME)) return TemporalRelationType.SIMULTANEOUS;
		if (rel.equals(CNTROConstants.CNTRO_TR_TERMINATE_PRP_NAME)) return TemporalRelationType.TERMINATE;
		if (rel.equals(CNTROConstants.CNTRO_TR_SAMEAS_PRP_NAME)) return TemporalRelationType.SAMEAS;
		
		return null;
	}

	private TimeInstant getTimeInstantFromLabel(OWLNamedIndividual oi)
	{
		TimeInstant ti = null;
		String labelAsTime = getAnnotationPropertyValue(oi, rdfLabel);
		
		if (!CNTROUtils.isNull(labelAsTime))
		{
			ti = new TimeInstant();
			ti.label = labelAsTime;
			ti.granularity = CNTROUtils.getGranularityFromDateString(labelAsTime);
			
			if ((addNormalizedTimeValue)&&(ti.getOriginalTime() == null))
			{
				NormalizedDate nd = normalize(labelAsTime, oi);
				
				if (nd != null)
				{
					ti.setNormalizedTime(nd.normalizedDate);
					ti.granularity = nd.getNormalizedGranularity();
				}
			}
		}
		
		return ti;
	}
	
	public NormalizedDate normalize(String dateString, OWLNamedIndividual owlInst)
	{
		if (CNTROUtils.isNull(dateString))
			return null;
		
		Date normalizedDate = dateParser.parse(dateString, new ParsePosition(0));
		
		//if (normalizedDate == null)
		//	System.out.println("Normalized Date is null for string:" + dateString);
		
		if (normalizedDate == null)
			return null;
	
		String normalizedTime = normalizedDateString(normalizedDate);
		if ((!CNTROUtils.isNull(normalizedTime))&&(owlInst != null))
		{
			OWLDataPropertyAssertionAxiom assertion = 
				df.getOWLDataPropertyAssertionAxiom(hasNormalizedTime, owlInst, normalizedTime);
			AddAxiom addAxiomChange = new AddAxiom(ontology, assertion);
			manager.applyChange(addAxiomChange);
		}
		
		NormalizedDate nd = new NormalizedDate();
		nd.normalizedDate = normalizedDate;
		nd.calendarGranularity = dateParser.getDateGranularity();
		
		return nd;
	}
	
	public String normalizedDateString(Date dt)
	{
		String normalizedTime = dateParser.normaltoUSDateString(dt);
		
		//if (CNTROUtils.isNull(normalizedTime))
		//	System.out.println("Normalized Date String is null for date:" + dt);
	
		return normalizedTime;
	}
	
	public void printOWLNamedIndividual(OWLNamedIndividual ind, OWLOntology ontology)
	{
		//System.out.println(ind);
		if (ind != null)
		{
			System.out.println("\nAnnotation Properties\n--------------------");
			
			Set<OWLAnnotation> annotations = ind.getAnnotations(ontology);
			
			for (OWLAnnotation ann : annotations) 
			{
				if ((ann.getValue() != null)&&
					(!CNTROUtils.isNull(ann.getValue().toString())))
				{
					String value = ann.getValue().toString();
					System.out.println(ann.getProperty() + "=" + CNTROUtils.getStringValueWithinQuotes(value));
				}
			}
			
			 // This code works and correct. Just commented out to save console space
			 
			System.out.println("\nObject Properties\n--------------------");
			Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objProperties = ind.getObjectPropertyValues(ontology);
			
			for (Iterator<OWLObjectPropertyExpression> oitr = objProperties.keySet().iterator(); oitr.hasNext();)
			{
				OWLObjectPropertyExpression oexp = oitr.next();
				System.out.println("" + oexp + "=" + objProperties.get(oexp));
			}
			
			System.out.println("\nData Properties\n--------------------");
			Map<OWLDataPropertyExpression, Set<OWLLiteral>> dataProperties = ind.getDataPropertyValues(ontology);
			for (Iterator<OWLDataPropertyExpression> ditr = dataProperties.keySet().iterator(); ditr.hasNext();)
			{
				OWLDataPropertyExpression dexp = ditr.next();
				System.out.println("" + dexp + "=" + dataProperties.get(dexp));
			}
			
			
			for (int i = 0; i < temporalRelations.size(); i++)
			{
				Set<OWLNamedIndividual> propList = reasoner.getObjectPropertyValues(ind, temporalRelations.elementAt(i)).getFlattened();
				CNTROUtils.printIterator(propList.iterator(), "[" + i + "]=" + temporalRelations.elementAt(i).toString()+ "", null, true);
			}
			
		}
	}
}
