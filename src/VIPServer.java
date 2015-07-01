import java.util.Iterator;
import java.util.Vector;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;
import org.simgrid.msg.HostFailureException;

public class VIPServer extends Process {

	// Worker node management for registration and termination 
	private String mailbox;
	private Vector<String> gateWorkers = new Vector<String>();
	private int endedGateWorkers = 0;

	private long totalParticleNumber = 0;

	public String getMailbox(){
		return this.mailbox;
	}

	private void setMailbox(){
		this.mailbox = Integer.toString(this.getPID()) + "@" +
				getHost().getName();
	}

	public Vector<String> getGateWorkers() {
		return gateWorkers;
	}

	public void setGateWorkers(Vector<String> gateWorkers) {
		this.gateWorkers = gateWorkers;
	}

	public VIPServer(Host host, String name, String[]args) {
		super(host,name,args);
		
	}

	public void main(String[] args) throws HostFailureException {
		Msg.info("A new simulation starts!");
		boolean stop=false;
		// Build the mailbox name from the PID and the host name. This might be 
		// useful to distinguish different Gate processes running on a same host
		setMailbox();
		// TODO what is below is very specific to GATE
		// Added to temporarily improve the realism of the simulation
		// Have to be generalized at some point.
		// WARNING: From log inspection, it seems that workers do not all get 
		// the input files from the default SE.
		LCG.crInput(getMailbox(),"gate.sh.tar.gz", 73043,
				VIPSimulator.getDefaultSE(), VIPSimulator.getDefaultLFC());
		LCG.crInput(getMailbox(),"opengate_version_7.0.tar.gz", 376927945,
				VIPSimulator.getDefaultSE(), VIPSimulator.getDefaultLFC());
		LCG.crInput(getMailbox(),"file-14539084101429.zip", 514388,
				VIPSimulator.getDefaultSE(), VIPSimulator.getDefaultLFC());

		// Wait for slaves to register
		while(!stop){
			// Use of some simulation magic here, every worker knows the 
			// mailbox of the VIP server
			GateMessage message = GateMessage.process("VIPServer");

			switch (message.getType()){
			case GATE_CONNECT:
				getGateWorkers().add(message.getMailbox());

				Msg.debug(getGateWorkers().size() +
						" worker(s) registered out of " +
						VIPSimulator.numberOfGateJobs);

				GateMessage start = new GateMessage(GateMessage.Type.GATE_START,
						"VIPServer");
				start.emit(message.getMailbox());
				break;
			case GATE_PROGRESS:
				totalParticleNumber += message.getParticleNumber();
				Msg.info (totalParticleNumber +
						" particles have been computed. "+
						VIPSimulator.totalParticleNumber + " are expected.");
				if (totalParticleNumber < VIPSimulator.totalParticleNumber){
					GateMessage again = 
							new GateMessage(GateMessage.Type.GATE_CONTINUE);
					Msg.info("Sending a '" + again.getType().toString() +
							"' message to '" + message.getMailbox() +"'");
					again.emit(message.getMailbox());
				} else {
					GateMessage endGate = 
							new GateMessage(GateMessage.Type.GATE_STOP);
					Msg.info("Sending a '" + endGate.getType().toString() +
							"' message to '" + message.getMailbox() +"'");
					endGate.emit(message.getMailbox());
					endedGateWorkers++;
				}

				if (endedGateWorkers == VIPSimulator.numberOfGateJobs){
					Msg.info("Exiting the interaction loop with " + 
							endedGateWorkers + " ended jobs");
					stop=true;
				}

				break;
			default:
				break;

			}
		}


		// sleep sosTime so that tasks have the time to finish before shutting 
		// down the LFCs and SEs
		Process.sleep(VIPSimulator.sosTime);

		Msg.info("Server waited for " + VIPSimulator.sosTime/1000 +" seconds." +
				" It's time to shutdown the system.");

		// Shutting down all the LFCs
		Iterator<String> itLFC = VIPSimulator.lfcList.iterator();
		while (itLFC.hasNext()){
			Message.sendTo(itLFC.next(), Message.Type.FINALIZE);
		}

		// Shutting down all the SEs
		Iterator<String> itSE = VIPSimulator.seList.iterator();
		while (itSE.hasNext()){
			Message.sendTo(itSE.next(), Message.Type.FINALIZE);
		}
	}
}
