package edu.csus.ecs.pc2.api.implementation;

import java.util.Vector;

import edu.csus.ecs.pc2.api.IRun;
import edu.csus.ecs.pc2.api.listener.IRunEventListener;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.IRunListener;
import edu.csus.ecs.pc2.core.model.JudgementRecord;
import edu.csus.ecs.pc2.core.model.Run;
import edu.csus.ecs.pc2.core.model.RunEvent;

/**
 * Run Listener implementation. 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class RunListenerList {

    private Vector<IRunEventListener> listenerList = new Vector<IRunEventListener>();

    private IInternalContest contest = null;
    
    private IInternalController controller = null;

    private void fireRunListener(RunEvent runEvent) {
        for (int i = 0; i < listenerList.size(); i++) {

            IRun run = new RunImplementation(runEvent.getRun(), contest, controller);
            
            boolean finalJudgementCycle = isFinalJudgementCycle(runEvent.getRun());

            switch (runEvent.getAction()) {
                case ADDED:
                    listenerList.elementAt(i).runSubmitted(run);
                    break;

                case DELETED:
                    listenerList.elementAt(i).runDeleted(run);
                    break;

                case CHANGED:
                    if (runEvent.getDetailedAction() != null) {
                        switch (runEvent.getDetailedAction()) {
                            case CHECKEDOUT_RUN:
                            case CHECKEDOUT_REJUDGE_RUN:
                                listenerList.elementAt(i).runCheckedOut(run, finalJudgementCycle);
                                break;
                            default:
                                listenerList.elementAt(i).runUpdated(run, finalJudgementCycle);
                                break;

                        }
                    } else {
                        listenerList.elementAt(i).runJudged(run, finalJudgementCycle);
                    }
                    break;

                case RUN_AVAILABLE:
                    listenerList.elementAt(i).runJudgingCanceled(run, finalJudgementCycle);
                    break;

                case RUN_COMPILING:
                    listenerList.elementAt(i).runCompiling(run, finalJudgementCycle);
                    break;

                case RUN_EXECUTING:
                    listenerList.elementAt(i).runExecuting(run, finalJudgementCycle);
                    break;

                case RUN_VALIDATING:
                    listenerList.elementAt(i).runValidating(run, finalJudgementCycle);
                    break;
                    
                case CHECKEDOUT_RUN:
                    listenerList.elementAt(i).runCheckedOut(run, finalJudgementCycle);
                    break;

                case CHECKEDOUT_REJUDGE_RUN:
                case RUN_HELD:
                case RUN_NOT_AVAILABLE:
                case RUN_REVOKED:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Is this run in the final judgement cycle?.
     * 
     * @param run
     *            - run to check
     * @return true if in the final judgement cycle, false if in the preliminary judging cycle.
     */
    private boolean isFinalJudgementCycle(Run run) {
        JudgementRecord record = run.getJudgementRecord();
        if (record != null) {
            return !record.isPreliminaryJudgement();
        } else {
            return false;
        }
    }

    public void addRunListener(IRunEventListener runEventListener) {
        listenerList.add(runEventListener);
    }

    public IInternalContest getContest() {
        return contest;
    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.contest = inContest;
        this.controller = inController;
        contest.addRunListener(new Listener());
    }

    public void removeRunListener(IRunEventListener runEventListener) {
        listenerList.remove(runEventListener);

    }

    /**
     * Run listener implemenation.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    class Listener implements IRunListener {

        public void runAdded(RunEvent event) {
            fireRunListener(event);
        }

        public void runChanged(RunEvent event) {
            fireRunListener(event);
        }

        public void runRemoved(RunEvent event) {
            fireRunListener(event);
        }

        public void refreshRuns(RunEvent event) {
            fireRunListener(event);
            
        }
    }
}
