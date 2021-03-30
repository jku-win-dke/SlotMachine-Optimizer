package at.jku.dke.slotmachine.optimizer.domain;

import at.jku.dke.slotmachine.optimizer.frameworks.Run;

import java.util.List;

/***
 *
 */
public class Optimization {
    private List<Flight> flightList;
    private List<Slot> slotList;
    private Class<Run> optimization; // specify the application used to run the optimization.

}
