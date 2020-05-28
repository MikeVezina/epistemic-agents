package epistemic.agent;

import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;
import jason.runtime.RuntimeServices;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MockAgArch extends AgArch {

    public MockAgArch() {
        super();
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public AgArch getFirstAgArch() {
        return super.getFirstAgArch();
    }

    @Override
    public AgArch getNextAgArch() {
        return super.getNextAgArch();
    }

    @Override
    public List<String> getAgArchClassesChain() {
        return super.getAgArchClassesChain();
    }

    @Override
    public void insertAgArch(AgArch arch) {
        super.insertAgArch(arch);
    }

    @Override
    public void createCustomArchs(Collection<String> archs) throws Exception {
        super.createCustomArchs(archs);
    }

    @Override
    public void reasoningCycleStarting() {
        super.reasoningCycleStarting();
    }

    @Override
    public void reasoningCycleFinished() {
        super.reasoningCycleFinished();
    }

    @Override
    public TransitionSystem getTS() {
        return super.getTS();
    }

    @Override
    public void setTS(TransitionSystem ts) {
        super.setTS(ts);
    }

    @Override
    public Collection<Literal> perceive() {
        return super.perceive();
    }

    @Override
    public void checkMail() {
        super.checkMail();
    }

    @Override
    public void act(ActionExec action) {
        super.act(action);
    }

    @Override
    public void actionExecuted(ActionExec act) {
        super.actionExecuted(act);
    }

    @Override
    public boolean canSleep() {
        return super.canSleep();
    }

    @Override
    public void wake() {
        super.wake();
    }

    @Override
    public void wakeUpSense() {
        super.wakeUpSense();
    }

    @Override
    public void wakeUpDeliberate() {
        super.wakeUpDeliberate();
    }

    @Override
    public void wakeUpAct() {
        super.wakeUpAct();
    }

    @Override
    public RuntimeServices getRuntimeServices() {
        return super.getRuntimeServices();
    }

    @Override
    public String getAgName() {
        return super.getAgName();
    }

    @Override
    public void sendMsg(Message m) throws Exception {
        super.sendMsg(m);
    }

    @Override
    public void broadcast(Message m) throws Exception {
        super.broadcast(m);
    }

    @Override
    public boolean isRunning() {
        return super.isRunning();
    }

    @Override
    public void setCycleNumber(int cycle) {
        super.setCycleNumber(cycle);
    }

    @Override
    public void incCycleNumber() {
        super.incCycleNumber();
    }

    @Override
    public int getCycleNumber() {
        return super.getCycleNumber();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int compareTo(AgArch o) {
        return super.compareTo(o);
    }

    @Override
    public Map<String, Object> getStatus() {
        return super.getStatus();
    }
}
