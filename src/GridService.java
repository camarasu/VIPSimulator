/*
 * Copyright (c) Centre de Calcul de l'IN2P3 du CNRS
 * Contributor(s) : Frédéric SUTER (2015)

 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package.
 */
import java.util.Vector;

import org.simgrid.msg.Host;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;

public abstract class GridService extends Process {
	protected String name;
	protected Vector<Process> mailboxes;

	protected String findAvailableMailbox(long retryAfter) {
		while (true) {
			for (Process listener : this.mailboxes) {
				String mailbox = listener.getName();
				if (Task.listen(mailbox)) {
					Msg.verb("Send a message to : " + mailbox
							+ " which is listening");
					return mailbox;
				}
			}
			try {
				Msg.verb("All the listeners are busy. Wait for " + retryAfter
						+ "ms and try again");
				Process.sleep(retryAfter);
			} catch (HostFailureException e) {
				e.printStackTrace();
			}
		}
	}

	public String getName() {
		return name;
	}

	public GridService(Host host, String name, String[] args) {
		super(host, name, args);
		this.name = getHost().getName();
		this.mailboxes = new Vector<Process>();
	}

	public void kill() {
		for (Process p : mailboxes)
			p.kill();
	}
}
