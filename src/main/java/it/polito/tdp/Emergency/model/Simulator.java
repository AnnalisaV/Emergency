package it.polito.tdp.Emergency.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import it.polito.tdp.Emergency.model.Event.EventType;
import it.polito.tdp.Emergency.model.Paziente.codiceColore;

public class Simulator {

	//PARAMETRI DI SIMULAZIONE 
	private int numStudiMedici; 
	private Duration T_ARRIVAL=Duration.ofMinutes(5); //intervallo fra pazienti  /*valori impostati per default*/
	private int NUMERO_PAZIENTI=150; 
	
	//tempi di cura del paziente
	private final Duration DURATION_TRIAGE= Duration.ofMinutes(5); 
	private final Duration DURATION_WHITE= Duration.ofMinutes(10);
	private final Duration DURATION_YELLOW= Duration.ofMinutes(15); 
	private final Duration DURATION_RED= Duration.ofMinutes(30);
	
    //tempi di tiemout per ogni codice
	private final Duration TIMEOUT_WHITE= Duration.ofMinutes(90);
	private final Duration TIMEOUT_YELLOW= Duration.ofMinutes(60); 
	private final Duration TIMEOUT_RED= Duration.ofMinutes(30); 

	private final LocalTime oraInizio= LocalTime.of(8,0); 
	private final LocalTime oraFine = LocalTime.of(20,0); 
	
	private final Duration TICK_TIME= Duration.ofMinutes(5); //ogni quanto scatta questo tick per vedere la disponibilita' degli studi
	
	
	public void setNumStudiMedici(int numStudiMedici) {
		this.numStudiMedici = numStudiMedici;
	}
	
	public void setT_ARRIVAL(Duration t_ARRIVAL) {
		T_ARRIVAL = t_ARRIVAL;
	}

    public void setNumeroPazienti(int numeroPazienti) {
		this.NUMERO_PAZIENTI = numeroPazienti;
	}
	
	//MODELLO DEL MONDO = STATO DEL SISTEMA
    private List<Paziente> pazienti; 
    private codiceColore coloreAssegnato;  //ultimo utilizzato
   private /* List*/PriorityQueue<Paziente> attesa;  //sottoinsieme di quelli in pronto ma gia' passati al 
                                                     //Triagequesta non e' una coda di eventi ma 
                                                 //semplicemente mi permette di capire quale sia da passare

   private int studiLiberi; 
	//OUTPUT 
	private int pazientiTot; 
	private int dimessi; 
	private int aCasa; //quelli che se ne sono andati a casa perche' hanno aspettato troppo
	private int morti; 
	

	public int getPazientiTot() {
		return pazientiTot;
	}

	public int getDimessi() {
		return dimessi;
	}

	public int getaCasa() {
		return aCasa;
	}

	public int getMorti() {
		return morti;
	}

	//CODA EVENTI 
	private PriorityQueue<Event> queue; 
	
	
	public void inizializzazione() {
		// costruzione delle strutture dati e pulizia
		this.queue= new PriorityQueue<Event>();  //oppure this.queue.clear(); 
		this.pazienti= new ArrayList<>(); 
		this.attesa= new PriorityQueue<Paziente>(); 
		
		this.pazientiTot=0; 
		this.dimessi=0; 
		this.morti=0; 
		this.aCasa=0; 
		this.coloreAssegnato= codiceColore.WHITE; 
		this.studiLiberi=this.numStudiMedici; 
		
		
		//generazione Event iniziali
		int nPaz=0; 
		LocalTime arrivo= this.oraInizio; 
		
		while(nPaz<this.NUMERO_PAZIENTI && arrivo.isBefore(this.oraFine)) {
			Paziente p = new Paziente (arrivo, codiceColore.UNKNOWN);
			this.pazienti.add(p); 
			
			//evento che arriva questo paziente 
			Event e = new Event(arrivo, EventType.ARRIVAL, p);
			this.queue.add(e); 
			
			nPaz++; 
			arrivo= arrivo.plus(T_ARRIVAL); 
			
		}
		
		
	}
	
	public void run() {
		
		while(!this.queue.isEmpty()) { // così considero tutti quelli che arrivano fino all'ora di chiusura
			                           // e poi li gestisco anche oltre l'orario (logico per un pronto)
			                           // se invece oltre l'orario di chiusura volessi mandarli via e non gestirli
			                          // while(!this.queue.isEmpty() && tempoEvento e <oraChiusura)
			Event e= this.queue.poll();
			System.out.println(e); //a scopo debug
			this.processEvent(e);
		}
		
	}
	
	private void processEvent(Event e) {
		Paziente paz = e.getPaziente(); //lo faccio qui una volta per tutte
		
		switch(e.getType()) {
		case ARRIVAL: 
			//arriva un nuovo paziente e lo mando in Triage
			this.pazientiTot++; 
			Event nuovo= new Event(e.getTime().plus(DURATION_TRIAGE), EventType.TRIAGE, paz); 
			break; 
			
		case TRIAGE :
			//associa al paziente un codice, mette in sala d'attesa e schedula eventuali Timeout
			paz.setColore(assegnaCodiceColore()); // qui lo assegno in modo sequenziale, ciclico
			
			this.attesa.add(paz); 
			
			if(paz.getColore()==codiceColore.WHITE) {
				this.queue.add(new Event(e.getTime().plus(TIMEOUT_WHITE), EventType.TIMEOUT, paz)); 
				}
			else if(paz.getColore()==codiceColore.YELLOW) {
				this.queue.add(new Event(e.getTime().plus(TIMEOUT_YELLOW), EventType.TIMEOUT, paz)); 
				}
			else if(paz.getColore()==codiceColore.RED) {
				this.queue.add(new Event(e.getTime().plus(TIMEOUT_RED), EventType.TIMEOUT, paz)); 
				}
			
			break; 
			
		case FREE_STUDIO : 
			Paziente prossimo= attesa.poll(); 
			if (prossimo !=null) {
				//esiste un paziente da guardare
				this.studiLiberi--; 
				 //e ne schedulo l'uscita dallo studio 
				if(prossimo.getColore()==codiceColore.WHITE) {
					this.queue.add(new Event(e.getTime().plus(DURATION_WHITE), EventType.TREATED, prossimo)); 
					}
				else if(prossimo.getColore()==codiceColore.YELLOW) {
					this.queue.add(new Event(e.getTime().plus(DURATION_YELLOW), EventType.TREATED, prossimo)); 
					}
				else if(prossimo.getColore()==codiceColore.RED) {
					this.queue.add(new Event(e.getTime().plus(DURATION_RED), EventType.TREATED, prossimo)); 
					}
				
			}
			
			break; 
			
		case TREATED:
			//paziente che esce perche' curato e si libera uno studio
			this.dimessi++; 
			this.studiLiberi++; 
			                         //lo chiamo immediatamente         //non so quale sara' il nuovo paziente
			this.queue.add(new Event(e.getTime(), EventType.FREE_STUDIO, null )); 
			break; 
		case TIMEOUT :
			// questo paziente cambia codiceColore e quindi la sua posizione in listaAttesa cambia 
			
			//in ogni caso li tolgo dalla listaAttesa (per riposizionarlo eventualmente)
			this.attesa.remove(paz);
			switch(paz.getColore()) {
			
			case WHITE : //va a casa 
				this.aCasa++; 
				break; 
			case YELLOW : //diventa red
				paz.setColore(codiceColore.RED);
				this.attesa.add(paz); 
				this.queue.add(new Event(e.getTime().plus(DURATION_RED), EventType.TIMEOUT, paz)); 
				break; 
			case RED :  //black, morto
				this.morti++; 
				break; 
			
			}
			break; 
			
		case TICK: 
			if (this.studiLiberi>0) {
				//c'e' almeno uno studio libero
				//allora un paziente puo' entare subito
				this.queue.add(new Event(e.getTime(), EventType.FREE_STUDIO, null)); 
			}
			//si autoimposta il tick
			this.queue.add(new Event(e.getTime().plus(TICK_TIME), EventType.TICK, null)); 
			break; 
			
		}
		
		
		
	}

	//Generazione del CodiceColore per il paziente
	private codiceColore assegnaCodiceColore() {
	    codiceColore nuovo= coloreAssegnato; //usa questo
	    //lo cambio, aggiorno
	    if (this.coloreAssegnato== codiceColore.WHITE) {
	    	coloreAssegnato=codiceColore.YELLOW; 
	    }
	    else if (this.coloreAssegnato== codiceColore.YELLOW) {
	    	coloreAssegnato=codiceColore.RED; 
	    }
	    else if (this.coloreAssegnato== codiceColore.RED) {
	    	coloreAssegnato=codiceColore.BLACK; 
	    }
	    else coloreAssegnato= codiceColore.WHITE; 
	    
	    
	    return nuovo; 
		
	}
}
