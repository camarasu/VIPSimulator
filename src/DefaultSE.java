import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;

public class DefaultSE extends SE{
	// In a simulation deployment file, there is a single host identified as the
	// default Storage Element. 
	// The sole special behavior of this process is to set a global variable to
	// the name of the host that runs it.

	public DefaultSE(Host host, String name, String[] args) {
		super(host, name, args);
	}

	public void main(String[] args) {
		VIPSimulator.setDefaultSE(this.hostName);
		super.main(args);
	}
}
