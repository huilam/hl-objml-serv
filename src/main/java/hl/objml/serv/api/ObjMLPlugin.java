package hl.objml.serv.api;

import java.io.File;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import hl.common.FileUtil;
import hl.common.http.HttpResp;
import hl.objml2.api.ObjMLApi;
import hl.objml2.plugin.MLPluginConfigKey;
import hl.objml2.plugin.MLPluginConfigProp;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.opencv.util.OpenCvUtil;
import hl.restapi.plugins.IServicePlugin;
import hl.restapi.service.RESTApiException;
import hl.restapi.service.RESTServiceReq;
import jakarta.servlet.http.HttpServletRequest;

public class ObjMLPlugin implements IServicePlugin{

	private static Object objLock 		= new Object();
	private static ObjMLApi objMlApi 	= null;
	private static List<String> listMlPluginClassName = null;

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
				objMlApi.reScanPlugins();
			}
			
			listMlPluginClassName = objMlApi.listPluginClassNames();
			
		}
		
		//////////////////////////////////
		
		String sAction = req.getUrlPathParam("action");
		if(sAction==null)
			sAction = "list";
		
		if("list".equalsIgnoreCase(sAction))
		{
			if(listMlPluginClassName!=null)
			{
				JSONArray jArrMLNames = new JSONArray();
				for(String sMLName : listMlPluginClassName)
				{
					jArrMLNames.put(sMLName);
				}
				
				JSONObject jsonMlNames = new JSONObject();
				jsonMlNames.put("objmlPlugins", jArrMLNames);
				res.setContent_data(jsonMlNames.toString());
			}
		}
		else if("detail".equalsIgnoreCase(sAction))
		{
			JSONObject jsonInfo = new JSONObject();
			
			String sObjMlClassName = httpReq.getParameter("className");
			if(sObjMlClassName!=null)
			{
				ObjDetBasePlugin plugin = objMlApi.initPlugin(sObjMlClassName);
				if(plugin!=null)
				{
					jsonInfo.put("name", plugin.getPluginName());
					
					JSONObject jsonProp = new JSONObject();
					MLPluginConfigProp prop = plugin.getPluginProps();
					for(Object oKey : prop.keySet())
					{
						String sVal = (String) prop.get(oKey);
						if("objml.mlmodel.source".equalsIgnoreCase(oKey.toString()))
						{
							sVal = new File(sVal).getName();
						}
						jsonProp.put(oKey.toString(), sVal);
					}
					
					jsonInfo.put("props", jsonProp);
					
				}
				
			}
			else
			{
				jsonInfo.put("error","invalid className");
			}
			
			res.setContent_type_as_Json();
			res.setContent_data(jsonInfo.toString());
		}
		
		return res;
	}
	
	
	private boolean init(RESTServiceReq req)
	{
		if(objMlApi==null)
		{
			OpenCvUtil.initOpenCV();
			synchronized (objLock) {
				if(objMlApi==null)
				{
			    	listMlPluginClassName = null;
			    	
			    	String sPluginFolder = req.getConfigMap().get("objml.config.plugins.folder");
			    	if(sPluginFolder==null)
			    		sPluginFolder = ".";
			    		
			    	File jarFolder = new File(sPluginFolder);
			    	File jarFiles[] = getPluginJarsPath(jarFolder);
			    	
			    	MLPluginMgr objmlMgr = new MLPluginMgr();
			    	objmlMgr.setCustomPluginConfigKey(
							getCustomPluginConfigKey("objml-plugin.properties", "objml."));
			    	objmlMgr.addPluginPaths(jarFiles);
			    	
			    	objMlApi = new ObjMLApi(objmlMgr);
				}
			}
		}
		
		return (objMlApi!=null);
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
