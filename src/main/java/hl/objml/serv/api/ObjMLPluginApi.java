package hl.objml.serv.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.common.http.HttpResp;
import hl.objml.serv.ObjMLServ;
import hl.objml2.api.ObjMLInputParam;
import hl.objml2.common.FrameDetectedObj;
import hl.objml2.plugin.MLPluginConfigProp;
import hl.objml2.plugin.MLPluginFrameOutput;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.opencv.util.OpenCvUtil;
import hl.restapi.service.RESTServiceReq;
import jakarta.servlet.http.HttpServletRequest;

public class ObjMLPluginApi extends ObjMLBaseApi {

	
	@Override
	protected HttpResp doGET(RESTServiceReq aReq, HttpResp aRes, final String aAction, ObjMLServ aObjMLServ)
	{
		HttpServletRequest httpReq = aReq.getHttpServletReq();
		
		if("true".equalsIgnoreCase(httpReq.getParameter("rescan")))
		{
			aObjMLServ.reScanPlugin();
		}
		
		if("list".equalsIgnoreCase(aAction))
		{
			List<String> listPlugins = aObjMLServ.getListOfPlugins();
			if(listPlugins!=null)
			{
				JSONArray jArrMLNames = new JSONArray();
				for(String sMLName : listPlugins)
				{
					jArrMLNames.put(sMLName);
				}
				
				JSONObject jsonMlNames = new JSONObject();
				jsonMlNames.put("plugins", jArrMLNames);
				
				aRes.setContent_data(jsonMlNames);
				aRes.setHttp_status(200);
			}
		}
		else if("detect".equalsIgnoreCase(aAction))
		{	
			String sObjMlClassName 	= httpReq.getParameter("className");
			String sTestImgFileName = constructTestImageFileName(httpReq.getParameter("imageFileName"));
			
			ObjMLInputParam inputDetect = new ObjMLInputParam();
			
			if(new File(sTestImgFileName).isFile())
			{
				Mat matImg = OpenCvUtil.loadImage(sTestImgFileName);
				if(matImg!=null)
					inputDetect.setInput_image(matImg);
			}
			
			JSONObject jsonDetect = new JSONObject();
			MLPluginFrameOutput outputDetect = aObjMLServ.detectImage(sObjMlClassName, inputDetect);
				
			if(outputDetect!=null)
			{
				Mat matOutputImg = null;
				try {

					matOutputImg = outputDetect.getAnnotatedFrameImage();
					
					boolean isReturnOnlyImage = "image".equalsIgnoreCase(httpReq.getParameter("return"));
					if(isReturnOnlyImage && matOutputImg!=null)
					{
						try {
							byte[] byteArray = OpenCvUtil.mat2Bytes(matOutputImg, "jpg");
							aRes.setContent_bytes(byteArray);
							aRes.setHttp_status(200);
							aRes.setContent_type_as_ImageJPG();

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return aRes;
					}

					
					boolean isIncludeBase64Img = "true".equalsIgnoreCase(httpReq.getParameter("imageBase64"));
					FrameDetectedObj detections = outputDetect.getFrameDetectedObj();
					if(detections.getAllDetectedObjs().size()>0)
					{
						if(isIncludeBase64Img)
						{
							
							if(matOutputImg!=null)
							{
								String sJpgBase64 = OpenCvUtil.mat2base64Img(matOutputImg, "jpg");
								jsonDetect.put("image.jpg", sJpgBase64);
							}
						}
					}
					
					jsonDetect.put("detections", detections.toJson());
					jsonDetect.put("meta", outputDetect.getFrameDetectionMeta().toJson());
					jsonDetect.put("errors", outputDetect.getErrorsJson());
					
					aRes.setContent_data(jsonDetect);
					aRes.setHttp_status(200);
				}
				finally
				{
					if(matOutputImg!=null)
						matOutputImg.release();
				}
			}
			
		}
		else if("info".equalsIgnoreCase(aAction))
		{
			JSONObject jsonInfo = new JSONObject();
			
			String sObjMlClassName = httpReq.getParameter("className");
			if(sObjMlClassName!=null)
			{
				ObjDetBasePlugin plugin = aObjMLServ.getObjDetPlugin(sObjMlClassName);
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
			
			aRes.setContent_data(jsonInfo);
			aRes.setHttp_status(200);
		}
		
		return aRes;
	}
	
	@Override
	protected HttpResp doPOST(RESTServiceReq aReq, HttpResp aRes, String aAction, ObjMLServ aObjMLServ)
	{
		return aRes;
	}

}
