package hl.objml.serv;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hl.common.FileUtil;
import hl.objml2.api.ObjMLApi;
import hl.objml2.api.ObjMLInputParam;
import hl.objml2.plugin.IObjDetectionPlugin;
import hl.objml2.plugin.MLPluginConfigKey;
import hl.objml2.plugin.MLPluginFrameOutput;
import hl.objml2.plugin.MLPluginMgr;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.opencv.util.OpenCvUtil;

public class ObjMLServApi {

	private String folderNativeLib 	= null;
	private String folderPlugins 	= null;
	
	private ObjMLApi objMlApi 	= null;
	private List<String> listMlPluginClassName = null;
	private Map<String, ObjDetBasePlugin> mapObjDetPluginCache = new HashMap<>();
	
	public ObjMLServApi(String aNativeLibPath, String aPluginFolder)
	{
		folderNativeLib = aNativeLibPath;
		folderPlugins = aPluginFolder;
		
		OpenCvUtil.initOpenCV(folderNativeLib);
		
		if(folderPlugins==null || folderPlugins.trim().equalsIgnoreCase(""))
			folderPlugins = ".";
		
		reScanPlugin();
	}
	
	public boolean reScanPlugin()
	{
		mapObjDetPluginCache.clear();
		File jarFolder = new File(folderPlugins);
    	File jarFiles[] = getPluginJarsPath(jarFolder);
    	
    	MLPluginMgr objmlMgr = new MLPluginMgr();
    	objmlMgr.setCustomPluginConfigKey(
				getCustomPluginConfigKey("objml-plugin.properties", "objml."));
    	objmlMgr.addPluginPaths(jarFiles);
    	
    	objMlApi = new ObjMLApi(objmlMgr);
    	listMlPluginClassName = objMlApi.listPluginClassNames();
		return true;
	}
	
	public ObjDetBasePlugin getObjDetPlugin(String aObjMlClassName)
	{
		ObjDetBasePlugin plugin = mapObjDetPluginCache.get(aObjMlClassName);
		if(plugin==null)
		{
			plugin = objMlApi.initPlugin(aObjMlClassName);
			mapObjDetPluginCache.put(aObjMlClassName, plugin);
		}
		
		return plugin;
	}
	
	public List<String> getListOfPlugins()
	{
		if(listMlPluginClassName==null)
		{
			listMlPluginClassName = objMlApi.listPluginClassNames();
		}
		return listMlPluginClassName;
	}
	
	public MLPluginFrameOutput detectImage(String aMLPluginClassName, ObjMLInputParam aObjMLInputParam)
	{
		ObjDetBasePlugin plugin = getObjDetPlugin(aMLPluginClassName);
		return objMlApi.detectFrame(plugin, aObjMLInputParam);
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
    	System.out.println("Scanning "+aPluginFolder.getAbsolutePath()+" ... bundles discovered : "+fPluginJars.length);
    	System.out.println();
    	
    	return fPluginJars;
	}
}
