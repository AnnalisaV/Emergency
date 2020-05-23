package it.polito.tdp.Emergency.model;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class TestSimulator {

	public static void main(String[] args) {
		Simulator sim = new Simulator() ;
		sim.setNumStudiMedici(2);
		sim.setT_ARRIVAL(Duration.ofMinutes(3));
		sim.inizializzazione();
		sim.run();
		
		System.out.println("** STATISTICHE **") ;
		//System.out.format("Studi medici: %d\n", sim.get);
		System.out.format("Pazienti:     %d\n", sim.getPazientiTot());
		System.out.format("Dimessi:      %d\n", sim.getDimessi());
		System.out.format("Morti:        %d\n", sim.getMorti());
		System.out.format("Abbandonano:  %d\n", sim.getaCasa());
	}

}
