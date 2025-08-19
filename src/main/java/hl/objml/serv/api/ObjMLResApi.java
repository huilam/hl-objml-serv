package hl.objml.serv.api;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.common.http.HttpResp;
import hl.objml.serv.ObjMLServ;
import hl.opencv.util.OpenCvUtil;
import hl.restapi.service.RESTServiceReq;
import jakarta.servlet.http.HttpServletRequest;

public class ObjMLResApi extends ObjMLBaseApi{

	
	@Override
	protected HttpResp doGET(RESTServiceReq aReq, HttpResp aRes, final String aAction, ObjMLServ aObjMLServ)
	{
		HttpServletRequest httpReq = aReq.getHttpServletReq();
		
		String sResourceType = aReq.getUrlPathParam("type");
		
		if("image".equalsIgnoreCase(sResourceType))
		{
			if("list".equalsIgnoreCase(aAction))
			{
				JSONArray jArrImgFileNames = new JSONArray();
				for(String sFileName : listTestImageFileNames())
				{
					jArrImgFileNames.put(sFileName);
				}
				
				JSONObject jsonTestImgFiles = new JSONObject();
				jsonTestImgFiles.put("imageFileNames", jArrImgFileNames);
				aRes.setContent_data(jsonTestImgFiles);
				aRes.setHttp_status(200);
				return aRes;
				
			} 
			else if("view".equalsIgnoreCase(aAction))
			{
				String sTestImgFileName = constructTestImageFileName(httpReq.getParameter("imageFileName"));
				if(new File(sTestImgFileName).isFile())
				{
					boolean isReturnOnlyImage = "image".equalsIgnoreCase(httpReq.getParameter("return"));
					Mat matImg = null;
					try {
						matImg = OpenCvUtil.loadImage(sTestImgFileName);
	
						if(isReturnOnlyImage)
						{
							try {
								byte[] byteArray = OpenCvUtil.mat2Bytes(matImg, "jpg");
								aRes.setContent_bytes(byteArray);
								aRes.setHttp_status(200);
								aRes.setContent_type_as_ImageJPG();
		
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else
						{
							String sJpgBase64 = OpenCvUtil.mat2base64Img(matImg, "jpg");
							JSONObject jsonImg = new JSONObject();
							jsonImg.put("image.jpg", sJpgBase64);
							aRes.setContent_data(jsonImg);
							aRes.setHttp_status(200);
						}
					}
					finally
					{
						if(matImg!=null)
							matImg.release();
					}
				}
			}
		}
		
		return aRes;
	}

}
