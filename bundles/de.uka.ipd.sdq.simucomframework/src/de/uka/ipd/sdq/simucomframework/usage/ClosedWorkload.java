package de.uka.ipd.sdq.simucomframework.usage;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Class used for executing a closed workload. The class creates as many users as specified and
 * executes them simultaniously
 *
 * @author Steffen Becker
 *
 */
public class ClosedWorkload implements ICancellableWorkloadDriver {

    private final int population;
    private final IClosedWorkloadUserFactory userFactory;
    private final Queue<ClosedWorkloadUser> users;
    /** is null, unless a new think time has been set. */
    private String newThinkTime = null;

    /**
     * Constructor of the closed workload driver
     *
     * @param userFactory
     *            Factory used to create the users
     * @param population
     *            Number of users in the system
     */
    public ClosedWorkload(final IClosedWorkloadUserFactory userFactory, final int population) {
        super();
        this.userFactory = userFactory;
        this.population = population;
        this.users = new LinkedList<ClosedWorkloadUser>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ipd.sdq.simucomframework.usage.IWorkloadDriver#run()
     */
    @Override
    public void run() {
        startUsers(population);
    }

    @Override
    public void cancel() {
        this.setPopulation(0);
    }

    @Override
    public IUserFactory getUserFactory() {
        return this.userFactory;
    }

    public void setPopulation(final int newPopulation) {
        if (users.size() > newPopulation) {
            stopUsers(users.size() - newPopulation);
        } else if (users.size() < newPopulation) {
            startUsers(newPopulation - users.size());
        }
    }

    private void stopUsers(final int count) {
        for (int i = 0; i < count; i++) {
            final ClosedWorkloadUser user = users.poll();
            user.requestStop();
        }
    }

    private void startUsers(final int count) {
        for (int i = 0; i < count; i++) {
            final ClosedWorkloadUser user = (ClosedWorkloadUser) userFactory.createUser();
            user.startUserLife();
            this.users.add(user);
        }
    }

    public void setThinkTime(final String newThinkTime) {
        userFactory.setThinkTimeSpec(newThinkTime);
        for (ClosedWorkloadUser user : users) {
            user.setThinkTime(newThinkTime);
        }
    }
}
