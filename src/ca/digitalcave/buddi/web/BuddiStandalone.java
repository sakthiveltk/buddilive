package ca.digitalcave.buddi.web;

import java.io.File;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class BuddiStandalone {

	/**
	 * Start the embedded Jetty server for testing / standalone use.
	 */
	public static void main(String[] args) throws Exception {
		final Server server = new Server(8080);
		final URL warUrl = new File("WebContent").toURI().toURL(); 
		final WebAppContext context = new WebAppContext(warUrl.toExternalForm(), "/");
		
		context.setClassLoader(BuddiStandalone.class.getClassLoader());
		server.setHandler(context);
		
		server.start();
		server.join();
	}
}
