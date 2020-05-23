package it.polito.tdp.Emergency.model;

import java.time.LocalTime;

public class Event implements Comparable<Event>{
	
	public enum EventType{
		
		ARRIVAL,         // arrivo del paziente
		TRIAGE,         // assegnato il codice colore e si sposta in sala d'attesa
		FREE_STUDIO,   //libero uno studio medico quindi si chiama un nuovo paziente della coda
		TREATED,      //paziente che e' stato curato e dimesso
		TIMEOUT,     // eccessiva attesa in sala d'aspetto
		TICK        // evento periodico per verificare se ci sono studi vuoti
		
	}
	        //dentro una singola giornata 
	private LocalTime time; 
	private EventType type;
	private Paziente paziente; //riferimento a quale paziente sia 
	
	/**
	 * @param time
	 * @param type
	 */
	public Event(LocalTime time, EventType type, Paziente p) {
		super();
		this.time = time;
		this.type = type;
		this.paziente=p; 
	}

	public LocalTime getTime() {
		return time;
	}

    public EventType getType() {
		return type;
	}

    public Paziente getPaziente() {
    	return this.paziente; 
    }

    //setTime() meglio non metterlo perche' se Event sta nella queue allora modifico un parametro
    // che lo renderebbe incorente rispetto all'ordinamento sulla queue
    
    
	//time 
	@Override
	public int compareTo(Event other) {
		
		return this.time.compareTo(other.time);
	}

	@Override
	public String toString() {
		return time + "  " + type + " per paziente " + paziente;
	} 
	
	
	
}