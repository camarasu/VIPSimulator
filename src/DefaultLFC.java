/*
 * Copyright (c) Centre de Calcul de l'IN2P3 du CNRS
 * Contributor(s) : Frédéric SUTER (2015)

 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package.
 */
import org.simgrid.msg.Host;
import org.simgrid.msg.MsgException;

public class DefaultLFC extends LFC {
	// In a simulation deployment file, there is a single host identified as the default Logical File Catalog service. 
	// The sole special behavior of this process is to set a global variable to the host that runs it.
	public DefaultLFC(Host host, String name, String[] args) {
		super(host, name, args);
	}

	public void main(String[] args) throws MsgException {
		VIPServer.setDefaultLFC(this);
		super.main(args);
	}
}
