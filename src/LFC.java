import java.util.Vector;
import java.util.Iterator;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public class LFC extends Process {
	
	public String hostName;
	public Vector<LFCFile> fileList;
	
	public LFC(Host host, String name, String[]args) {
		super(host,name,args);
		this.hostName = this.getHost().getName();
		this.fileList = new Vector<>();
	}
	
	public void main(String[] args) {
		boolean stop = false;
		Msg.debug("Register LFC on "+ this.hostName);
		VIPSimulator.seList.add(this.getHost());
		
		while (!stop){
			Message message = Message.process(hostName);
			
			switch(message.type){
			case CR_INPUT:
				// Register the information sent in the message into the LFC by adding a new File
				addFile(message.logicalFileName, message.logicalFileSize, message.SEName);
				Msg.info("LFC '"+ this.hostName + "' registered file '" + 
						message.logicalFileName + "', of size " + 
						message.logicalFileSize + ", stored on SE '" + 
						message.SEName + "'");
				break;
			case REGISTER_FILE:
				handleRegisterFile(message);
				break;
			case ASK_FILE_INFO:
				handleAskFileInfo(message);
				break;
			case FINALIZE:
				Msg.info("Goodbye!");
				stop = true;
				break;
			default:
				break;				
			}
		}
	}

	public void handleRegisterFile(Message message) {
		addFile(message.logicalFileName, message.logicalFileSize, message.SEName);
		
		Message registerAck = new Message(Message.Type.REGISTER_ACK);
		registerAck.emit(message.issuerHost.getName());
		Msg.debug("LFC '"+ this.hostName + "' sent back an ack to '" + message.issuerHost.getName() + "'");	
	}

	public void handleAskFileInfo(Message message) {
		String logicalFileName = message.logicalFileName;
		
		String SEName = getSEName(logicalFileName);
		long logicalFileSize = getLogicalFileSize(logicalFileName);
		
		if(SEName == null){	
			Msg.error("File '" + logicalFileName + "' is stored on no SE. Exiting with status 1");
		    System.exit(1);
		}
		
		Message replySEName = new Message(Message.Type.SEND_FILE_INFO, SEName, logicalFileSize);
		
		replySEName.emit(message.issuerHost.getName());
		Msg.debug("LFC '"+ this.hostName + "' sent SE name '" + SEName + "' back to '" + message.issuerHost.getName() + "'");

	}

	public void addFile(String logicalFileName, long logicalFileSize, String seName){
		LFCFile newFile = new LFCFile(logicalFileName, logicalFileSize, seName);
		this.fileList.add(newFile);
	}
	
	public String getSEName (String logicalFileName){
		String SEName = null;
		Iterator<LFCFile> it = this.fileList.iterator();
		
		while (it.hasNext() && SEName == null){
			LFCFile current = it.next();
			if (current.logicalFileName == logicalFileName){
				SEName = current.SEName;
			}
		}
		
		if (SEName == null)
			Msg.error("Logical file '" + logicalFileName + "' not found on LFC '" + this.hostName + "'");
		
		return SEName;
	}
	
	public long getLogicalFileSize (String logicalFileName){
		long logicalFileSize = 0;
		Iterator<LFCFile> it = this.fileList.iterator();
		
		while (it.hasNext() && logicalFileSize == 0){
			LFCFile current = it.next();
			if (current.logicalFileName == logicalFileName){
				logicalFileSize = current.logicalFileSize;
			}
		}
		
		if (logicalFileSize == 0)
			Msg.error("Logical file '" + logicalFileName + "' not found on LFC '" + this.hostName + "'");
		
		return logicalFileSize;
	}
	
	public String getLogicalFileList () {
		String fileList = "";
		Iterator<LFCFile> it = this.fileList.iterator();
		while (it.hasNext())
			fileList.concat(it.next().logicalFileName + ",");
		//removing last comma
		if (fileList.charAt(fileList.length()-1)==',')
			fileList = fileList.substring(0, fileList.length()-1);
		
		return fileList;
	}
}