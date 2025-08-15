package hl.objml.serv.api;

import java.io.File;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import hl.common.FileUtil;
import hl.common.http.HttpResp;
import hl.objml2.plugin.MLPluginConfigKey;
import hl.objml2.plugin.MLPluginConfigProp;
import hl.objml2.plugin.MLPluginMgr;
import hl.opencv.util.OpenCvUtil;
import hl.restapi.plugins.IServicePlugin;
import hl.restapi.service.RESTApiException;
import hl.restapi.service.RESTServiceReq;
import jakarta.servlet.http.HttpServletRequest;

public class RestApiPlugin implements IServicePlugin{

	private static Object objLock = new Object();
	private static MLPluginMgr mgr = null;
	private static Map<String, MLPluginConfigProp> mapMLPluginJavaClassName = null;

	@Override
	public HttpResp handleException(RESTServiceReq req, HttpResp res, RESTApiException ex) throws RESTApiException {
		// TODO Auto-generated method stub
		return res;
	}

	@Override
	public HttpResp postProcess(RESTServiceReq req, HttpResp res) throws RESTApiException {
		
		HttpServletRequest httpReq = req.getHttpServletReq();
		
		if(init(req))
		{
			if("true".equalsIgnoreCase(httpReq.getParameter("reload")))
			{
				if(mgr!=null)
				{
					mapMLPluginJavaClassName = mgr.scanForPluginJavaClassName();
				}
			}
			else if(mapMLPluginJavaClassName==null)
			{
				mapMLPluginJavaClassName = mgr.scanForPluginJavaClassName();
			}
			
		}
		
		//////////////////////////////////
		
		if(mapMLPluginJavaClassName!=null)
		{
			JSONArray jArrMLNames = new JSONArray();
			for(Object objMlName : mapMLPluginJavaClassName.keySet())
			{
				jArrMLNames.put(objMlName.toString());
			}
			
			JSONObject jsonMlNames = new JSONObject();
			jsonMlNames.put("objmlPlugins", jArrMLNames);
			res.setContent_data(jsonMlNames.toString());
		}
		
		return res;
	}
	
	
	private boolean init(RESTServiceReq req)
	{
		if(mgr==null)
		{
			synchronized (objLock) {
				if(mgr==null)
				{
					OpenCvUtil.initOpenCV();
			    	mapMLPluginJavaClassName = null;
			    	
			    	String sPluginFolder = req.getConfigMap().get("config.plugins.folder");
			    	File jarFolder = new File(sPluginFolder);
			    	File jarFiles[] = getPluginJarsPath(jarFolder);
			    	
			    	mgr = new MLPluginMgr();
					mgr.setCustomPluginConfigKey(getCustomPluginConfigKey("objml-plugin.properties", "objml."));
					mgr.addPluginPaths(jarFiles);
				}
			}
		}
		
		if(mgr!=null && (mapMLPluginJavaClassName==null || mapMLPluginJavaClassName.size()==0))
		{
			mapMLPluginJavaClassName = mgr.scanForPluginJavaClassName();
		}
		
		return (mgr!=null);
	}
	
	
	protected static MLPluginConfigKey getCustomPluginConfigKey(String aPropFileName, String aPropPrefix)
	{
		MLPluginConfigKey customPluginConfig = new MLPluginConfigKey();
    	
		/*** Custom configuration for properties ***/
    	customPluginConfig.setProp_filename(aPropFileName);
    	customPluginConfig.setPropkey_prefix(aPropPrefix);
    	
    	return customPluginConfig;
	}

	protected static File[] getPluginJarsPath(File aPluginFolder)
	{
		File[] fPluginJars = new File[]{};
		
		if(aPluginFolder!=null && aPluginFolder.isDirectory())
		{
			fPluginJars =  FileUtil.getFilesWithExtensions(aPluginFolder, new String[]{".jar",".zip"});
    	
		}
    	System.out.println();
    	System.out.println("plugin bundles discovered : "+fPluginJars.length);
    	
    	return fPluginJars;
	}
	
	protected static File[] getTestImageFiles(String aFolder, String[] aImgExts)
	{
		File folderImages = new File(aFolder);
		
		if(folderImages.isDirectory())
		{
			return FileUtil.getFilesWithExtensions(
					folderImages, 
					aImgExts);
		}
		else
		{
			return new File[] {};
		}
	}

}
