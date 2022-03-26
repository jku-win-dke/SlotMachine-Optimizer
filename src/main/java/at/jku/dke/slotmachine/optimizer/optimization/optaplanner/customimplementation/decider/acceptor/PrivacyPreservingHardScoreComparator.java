package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.acceptor;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPlanningEntity;

import java.util.Collection;

public class PrivacyPreservingHardScoreComparator {

    private PrivacyPreservingHardScoreComparator(){
        // utility class
    }

    static boolean validateHardConstraints(Collection<FlightPlanningEntity> entities) {
        for(FlightPlanningEntity e1 : entities) {
            // make sure, that the scheduled time is before or at the assigned time
            if(e1.getWrappedFlight().getScheduledTime().isAfter(e1.getSlot().getTime())) {
                return false;
            }
            /*
            for(FlightPlanningEntity e : entities) {
                if(!e1.equals(e) && e.getSlot() != null && e1.getSlot() != null && e.getSlot().equals(e1.getSlot())) {
                    return false;
                }
            }
             */
        }
        return true;
    }

}
