import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public abstract class Job extends Process{
	private String name;
	private SE closeSE;

	public String getName(){
		return name;
	}

	public SE getCloseSE() {
		return closeSE;
	}

	public void begin(){
		GateMessage.sendTo(name, Message.Type.BEGIN, 0);
	}

	public void carryOn (){
		GateMessage.sendTo(name, Message.Type.CARRY_ON, 0);
	}

	public void end(){
		GateMessage.sendTo(name, Message.Type.END, 0);
	}

	public Job(Host host, String name, String[]args) {
		super(host,name,args);
		// Build the mailbox name from the PID and the host name. This might be 
		// useful to distinguish different Gate processes running on a same host
		this.name = Integer.toString(this.getPID()) + "@" + getHost().getName();
		if (host.getProperty("closeSE")!=null)
			this.closeSE = VIPServer.getSEbyName(host.getProperty("closeSE"));
	}
}
