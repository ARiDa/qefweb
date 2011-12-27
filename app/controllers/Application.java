package controllers;

import play.*;
import play.mvc.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import ch.epfl.codimsd.config.AppConfig;
import ch.epfl.codimsd.exceptions.CodimsException;
import ch.epfl.codimsd.helper.FileUtils;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.QueryManager;
import ch.epfl.codimsd.qeef.QueryManagerImpl;
import ch.epfl.codimsd.qeef.QueryManagerRepository;
import ch.epfl.codimsd.qep.QEPInfo;
import ch.epfl.codimsd.query.RequestResult;
import ch.epfl.codimsd.query.ResultSet;
import ch.epfl.codimsd.query.sparql.JSONOutput;
import ch.epfl.codimsd.query.sparql.ResultOutput;
import ch.epfl.codimsd.query.sparql.XMLOutput;

public class Application extends Controller {

	static {
		try {
			// Initializes the Service
			QueryManagerImpl.getQueryManagerImpl();
			Logger.info("QEF - Initialized successfully");
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
	
    public static void index() {
    	try {
			List<QEPInfo> qeps = QEPInfo.getQEPInfo();
			render(qeps);
		} catch (Exception e) {
			flash.error(e.getMessage());
			Logger.error(e.getMessage(), e);
			index();
		}
    }

    public static void executeQuery(Integer qep, String format) {
		try {
			if (format == null)
				format = "html";
			
			Logger.info("Execute QEP " + qep + " (" + format  + ")");
			if (qep == null)
				throw new IllegalArgumentException("id parameter missing or not a number.");
			
			QueryManager queryManager = QueryManagerRepository.getInstance().get(qep);
			queryManager.executeRequest();
			
			RequestResult result = queryManager.getRequestResult();
			
			Metadata mt = result.getResultMetadata();
			ResultSet rs = result.getResultSet();

			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			
			ResultOutput output = null;
			if (format.equals("html")) {
				response.setContentTypeIfNotSet("application/xml");
				output = new XMLOutput("/public/xml-to-html.xsl");
				
			} else if (format.equals("xml")) {
				response.setContentTypeIfNotSet("application/xml");
				output = new XMLOutput();
				
			} else if (format.equals("json")) {
				response.setContentTypeIfNotSet("application/json");
				output = new JSONOutput();
				
			} 
			
			if (output != null) {
				output.format(response.out, rs, mt);
			} else {
				throw new IllegalArgumentException("Invalid format: " + format);
			}
			Logger.info("Execute query finished successfully.");
			
		} catch (Exception e) {
			flash.error(e.getMessage());
			Logger.error(e.getMessage(), e);
			index();
		}
    	
    }
    
    // To test: http://localhost:9001/show/11
    public static void show(Integer id) {
    	try {
    		QEPInfo qepInfo = QEPInfo.getQEPInfo(id);
    		
    		if (qepInfo == null) {
    			throw new IllegalArgumentException("Could not find qep with id = " + id);
    		}

			String fileContents = FileUtils.readFile(AppConfig.CODIMS_HOME + qepInfo.getPath());
			render(fileContents);
			
		} catch (Exception e) {
			flash.error(e.getMessage());
			Logger.error(e.getMessage(), e);
			index();
		}
    }

    // To test: http://localhost:9001/save/teste.txt/oi 
    public static void save(String fileName, String fileContents) {
    	try {
			FileUtils.writeFile(fileName, fileContents);
			flash.success("File " + fileName + " saved successfully.");
			index();
		} catch (IOException e) {
			flash.error(e.getMessage());
			Logger.error(e.getMessage(), e);
			index();
		}
    }
    
}
