package it.polito.tdp.Emergency.model;

import java.time.LocalTime;

/**
 * Rappresenta le informazioni su ciascun paziente nel sistema
 * @author Fulvio
 *
 */
public class Paziente implements Comparable<Paziente>{
	
	private LocalTime oraArrivo; 
	
	//Le possibilita' di codice colore
	public enum codiceColore{
		WHITE, 
		YELLOW, 
		RED, 
		BLACK, 
		UNKNOWN // quando deve ancora finire il Triage
	}
	
	private codiceColore colore;

	
	/**
	 * @param oraArrivo
	 * @param colore
	 */
	public Paziente(LocalTime oraArrivo, codiceColore colore) {
		super();
		this.oraArrivo = oraArrivo;
		this.colore = colore;
	}


	public LocalTime getOraArrivo() {
		return oraArrivo;
	}


	//non si modifica durante la simulazione 
	/*public void setOraArrivo(LocalTime oraArrivo) {
		this.oraArrivo = oraArrivo;
	} */

	public codiceColore getColore() {
		return colore;
	}


	public void setColore(codiceColore colore) {
		this.colore = colore;
	}


	//come sono gestiti in Lista d'attesa? sulla base dei codici
	@Override
	public int compareTo(Paziente other) {
		
       //stesso codice, allora in vase al tempo di arrivo
		if (this.colore==other.colore) {
			return this.oraArrivo.compareTo(other.oraArrivo); 
		}
		 
		//codici diversi
		else if (this.colore==codiceColore.RED) {
			return -1; // negativo così viene prima
		} else if (other.colore==codiceColore.RED) {
			return +1; // lo metto dopo
		}
		
		else if (this.colore==codiceColore.YELLOW) {
			return -1; 
		}
		else if (other.colore==codiceColore.YELLOW) {
			return +1; 
		}
		 //ho esaurito tutti i casi possibili 
		//qui non ci devo arrivare quindi
		throw new RuntimeException("Comparator<Paziente> failed"); 
	
	}


	@Override
	public String toString() {
		return oraArrivo + ", codice " + colore;
	} 
	
	
	
	
}