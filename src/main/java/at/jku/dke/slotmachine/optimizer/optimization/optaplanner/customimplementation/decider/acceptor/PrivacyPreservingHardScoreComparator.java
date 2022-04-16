package at.jku.dke.slotmachine.optimizer.optimization.optaplanner.customimplementation.decider.acceptor;

import at.jku.dke.slotmachine.optimizer.optimization.optaplanner.FlightPlanningEntity;
import org.optaplanner.core.impl.localsearch.decider.acceptor.ConstraintValidator;

import java.util.Collection;

public class PrivacyPreservingHardScoreComparator<Solution_> implements ConstraintValidator<Solution_> {

    public PrivacyPreservingHardScoreComparator(){
        // utility class
    }

    static boolean validateHardConstraints(Collection<FlightPlanningEntity> entities) {
        return true;
    }

    @Override
    public boolean satisfiesConstraints(Collection<?> entities) {
        for(Object e : entities) {
            if(e instanceof FlightPlanningEntity flightPlanningEntity){
                // make sure, that the scheduled time is before or at the assigned time
                if(flightPlanningEntity.getWrappedFlight().getScheduledTime().isAfter(flightPlanningEntity.getSlot().getTime())) {
                    return false;
                }
            }else return false;
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
