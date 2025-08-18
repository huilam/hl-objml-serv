package hl.objml.serv.api;

import java.io.File;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.common.http.HttpResp;
import hl.objml.serv.ObjMLServApi;
import hl.objml2.api.ObjMLInputParam;
import hl.objml2.common.FrameDetectedObj;
import hl.objml2.plugin.MLPluginConfigProp;
import hl.objml2.plugin.MLPluginFrameOutput;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.opencv.util.OpenCvUtil;
import hl.restapi.plugins.IServicePlugin;
import hl.restapi.service.RESTApiException;
import hl.restapi.service.RESTServiceReq;
import jakarta.servlet.http.HttpServletRequest;

public class ObjMLPlugin implements IServicePlugin{

	private static Object objLock 			= new Object();
	private static ObjMLServApi objMLServ 	= null;

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
				objMLServ.reScanPlugin();
			}
			
		}
		
		//////////////////////////////////
		
		String sAction = req.getUrlPathParam("action");
		if(sAction==null)
			sAction = "list";
		
		if("list".equalsIgnoreCase(sAction))
		{
			List<String> listPlugins = objMLServ.getListOfPlugins();
			if(listPlugins!=null)
			{
				JSONArray jArrMLNames = new JSONArray();
				for(String sMLName : listPlugins)
				{
					jArrMLNames.put(sMLName);
				}
				
				JSONObject jsonMlNames = new JSONObject();
				jsonMlNames.put("plugins", jArrMLNames);
				res.setContent_type_as_Json();
				res.setContent_data(jsonMlNames.toString());
			}
		}
		else if("detect".equalsIgnoreCase(sAction))
		{
			String sObjMlClassName 	= httpReq.getParameter("className");
			String sTestImgFileName = httpReq.getParameter("imageFileName");
			
			if((sTestImgFileName.indexOf("/")==-1)||(sTestImgFileName.indexOf("\\")==-1))
			{
				String sTestImgFolder = req.getConfigMap().get("objml.config.test-image.folder");
				sTestImgFileName = sTestImgFolder+"/"+sTestImgFileName;
			}
			
			ObjMLInputParam inputDetect = new ObjMLInputParam();
			
			if(new File(sTestImgFileName).isFile())
			{
				Mat matImg = OpenCvUtil.loadImage(sTestImgFileName);
				if(matImg!=null)
					inputDetect.setInput_image(matImg);
			}
			
			
			
			JSONObject jsonDetect = new JSONObject();
			
			MLPluginFrameOutput outputDetect = objMLServ.detectImage(sObjMlClassName, inputDetect);
			
			if(outputDetect!=null)
			{
				FrameDetectedObj detections = outputDetect.getFrameDetectedObj();
				if(detections.getAllDetectedObjs().size()>0)
				{
					Mat matOutput = outputDetect.getAnnotatedFrameImage();
					if(matOutput!=null)
					{
						String sJpgBase64 = OpenCvUtil.mat2base64Img(matOutput, "jpg");
						jsonDetect.put("image.jpg", sJpgBase64);
					}
				}
				
				
				jsonDetect.put("detections", detections.toJson());
				jsonDetect.put("meta", outputDetect.getFrameDetectionMeta().toJson());
			}
			
			
			res.setContent_type_as_Json();
			res.setContent_data(jsonDetect.toString());
			
		}
		else if("info".equalsIgnoreCase(sAction))
		{
			JSONObject jsonInfo = new JSONObject();
			
			String sObjMlClassName = httpReq.getParameter("className");
			if(sObjMlClassName!=null)
			{
				ObjDetBasePlugin plugin = objMLServ.getObjDetPlugin(sObjMlClassName);
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
		if(objMLServ==null)
		{
			synchronized (objLock) {
				if(objMLServ==null)
				{
			    	String sNativeLibFolder = req.getConfigMap().get("objml.config.native-lib.folder");
			    	String sPluginFolder = req.getConfigMap().get("objml.config.plugins.folder");
			    	
			    	objMLServ = new ObjMLServApi(sNativeLibFolder, sPluginFolder);
				}
			}
		}
		return (objMLServ!=null);
	}
	
	

}
