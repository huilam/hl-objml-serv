package hl.objml.serv;

import java.io.File;
import java.util.List;

import hl.common.FileUtil;
import hl.objml2.api.ObjMLApi;
import hl.objml2.plugin.MLPluginConfigKey;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.opencv.util.OpenCvUtil;

public class ObjMLServApi {

	private ObjMLApi objMlApi 	= null;
	private List<String> listMlPluginClassName = null;

	public ObjMLServApi(String aPluginFolder)
	{
		OpenCvUtil.initOpenCV();
		
		if(aPluginFolder==null || aPluginFolder.trim().equalsIgnoreCase(""))
			aPluginFolder = ".";
		
		File jarFolder = new File(aPluginFolder);
    	File jarFiles[] = getPluginJarsPath(jarFolder);
    	
    	MLPluginMgr objmlMgr = new MLPluginMgr();
    	objmlMgr.setCustomPluginConfigKey(
				getCustomPluginConfigKey("objml-plugin.properties", "objml."));
    	objmlMgr.addPluginPaths(jarFiles);
    	
    	objMlApi = new ObjMLApi(objmlMgr);
	}
	
	public boolean reScanPlugin()
	{
		objMlApi.reScanPlugins();
		return true;
	}
	
	public ObjDetBasePlugin initPlugin(String aObjMlClassName)
	{
		return  objMlApi.initPlugin(aObjMlClassName);
	}
	
	public List<String> getListOfPlugins()
	{
		if(listMlPluginClassName==null)
		{
			listMlPluginClassName = objMlApi.listPluginClassNames();
		}
		return listMlPluginClassName;
	}
	
	//////////
	private static MLPluginConfigKey getCustomPluginConfigKey(String aPropFileName, String aPropPrefix)
	{
		MLPluginConfigKey customPluginConfig = new MLPluginConfigKey();
    	
		/*** Custom configuration for properties ***/
    	customPluginConfig.setProp_filename(aPropFileName);
    	customPluginConfig.setPropkey_prefix(aPropPrefix);
    	
    	return customPluginConfig;
	}

	private static File[] getPluginJarsPath(File aPluginFolder)
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
}
