package hl.objml.serv;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

public class EmbededWebAppServer {
	
	private static Tomcat tomcat = null;
	private static File webappHome = null;

	public static void run(String aWebAppContextRoot, int aWebAppPort) throws LifecycleException
	{
		try {
	    	tomcat = new Tomcat();
	        tomcat.setPort(aWebAppPort);
	
	        // Create the default connector (required in embedded use)
	        tomcat.getConnector();
	
	        File folderWebApp = new File("src/main/webapp");
	        if(webappHome!=null && webappHome.isDirectory())
	        	folderWebApp = webappHome;
	        // Point to your exploded webapp directory (must contain WEB-INF/web.xml)
	        String docBase = folderWebApp.getAbsolutePath();
	
	        // Context path of your app (e.g., http://localhost:8080/hl-objml-serv/)
	        Context ctx = tomcat.addWebapp(aWebAppContextRoot, docBase);
	

	        // ========================================================
	        // CLASSPATH CONFIGURATION START
	        // ========================================================
	        
	        // 1. Initialize the WebResourceRoot for this context
	        WebResourceRoot resources = new StandardRoot(ctx);

	        // 2. Define the path to your compiled classes (e.g., Maven's target/classes)
	        // Change this path to wherever your .class files are actually generated
	        File additionWebInfClasses = new File("src/main/resources/lib-native"); 

	        // 3. Map your physical directory to the webapp's virtual /WEB-INF/classes
	        resources.addPreResources(new DirResourceSet(
	                resources, 
	                "/WEB-INF/classes", 
	                additionWebInfClasses.getAbsolutePath(), 
	                "/"
	        ));

	        // 4. Apply the modified resources configuration to the context
	        ctx.setResources(resources);
	
	        tomcat.start();
	        tomcat.getServer().await();
		}finally
		{
			if(tomcat!=null)
			{
		        tomcat.stop();
		        tomcat.destroy();
			}
		}
	}
	
    public static void main(String[] args) throws Exception {
    	run("/hl-objml-serv",8080);
    }

}
