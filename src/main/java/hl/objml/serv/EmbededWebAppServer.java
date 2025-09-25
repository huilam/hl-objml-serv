package hl.objml.serv;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class EmbededWebAppServer {
	
	private static Tomcat tomcat = null;;

	public static void run(String aWebAppContextRoot, int aWebAppPort) throws LifecycleException
	{
		try {
	    	tomcat = new Tomcat();
	        tomcat.setPort(aWebAppPort);
	
	        // Create the default connector (required in embedded use)
	        tomcat.getConnector();
	
	        // Point to your exploded webapp directory (must contain WEB-INF/web.xml)
	        String docBase = new File("src/main/webapp").getAbsolutePath();
	
	        // Context path of your app (e.g., http://localhost:8080/hl-objml-serv/)
	        Context ctx = tomcat.addWebapp(aWebAppContextRoot, docBase);
	
	        // (Optional) If your web.xml is not under WEB-INF/web.xml, you can set it explicitly:
	        // ctx.setConfigFile(new File("path/to/your/web.xml").toURI().toURL());
	
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
