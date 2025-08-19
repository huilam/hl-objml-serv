package hl.objml.serv.api;

import java.util.Map;

import org.json.JSONObject;

import hl.common.http.HttpResp;
import hl.objml.serv.ObjMLServ;
import hl.restapi.plugins.IServicePlugin;
import hl.restapi.service.RESTApiException;
import hl.restapi.service.RESTServiceReq;
import jakarta.servlet.http.HttpServletRequest;

public class ObjMLBaseApi implements IServicePlugin{

	protected static String _nativeLibFolder 	= null;
	protected static String _pluginFolder 		= null;
	protected static String _testImageFolder 	= null;
	
	private static Object objLock 			= new Object();
	private static ObjMLServ _objMLServ 	= null;

	@Override
	public HttpResp handleException(RESTServiceReq req, HttpResp res, RESTApiException ex) throws RESTApiException {
		
		JSONObject jsonError = new JSONObject();
		jsonError.put("error", ex.toString());
		res.setContent_data(jsonError.toString());
		res.setContent_type_as_Json();
		return res;
	}

	@Override
	public HttpResp postProcess(RESTServiceReq req, HttpResp res) throws RESTApiException {
		
		if(init(req))
		{
			HttpServletRequest httpReq = req.getHttpServletReq();
			
			String sAction = req.getUrlPathParam("action");
			if(sAction==null)
				sAction = "list";
			
			if("GET".equalsIgnoreCase(httpReq.getMethod()))
				res = doGET(req, res, sAction, _objMLServ);
			else if("POST".equalsIgnoreCase(httpReq.getMethod()))
				res = doPOST(req, res, sAction, _objMLServ);
		}
		
		
		return res;
	}

	private boolean init(RESTServiceReq req)
	{
		if(_objMLServ==null)
		{
			synchronized (objLock) {
				if(_objMLServ==null)
				{
					Map<String, String> mapConfig = req.getConfigMap();
			    	_nativeLibFolder 	= mapConfig.getOrDefault("objml.config.native-lib.folder", null);
			    	_pluginFolder 		= mapConfig.getOrDefault("objml.config.plugins.folder", ".");
			    	_testImageFolder 	= mapConfig.getOrDefault("objml.config.test-image.folder", ".");
			    	
			    	_objMLServ = new ObjMLServ(_nativeLibFolder, _pluginFolder);
				}
			}
		}
		return (_objMLServ!=null);
	}
	
	//==================================================
	
	protected HttpResp doGET(RESTServiceReq aReq, HttpResp aRes, final String aAction, ObjMLServ aObjMLServ)
	{
		return aRes;
	}
	
	protected HttpResp doPOST(RESTServiceReq aReq, HttpResp aRes, final String aAction, ObjMLServ aObjMLServ)
	{
		return aRes;
	}
	

}
