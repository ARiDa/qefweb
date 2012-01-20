package controllers;

import play.*;
import play.mvc.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import models.JsonModelBinding;
import models.JsonModelBindingDrug;
import models.JsonModelInitial;
import models.JsonModelInitialDrug;

import com.google.gson.Gson;

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
			String appPath = Play.applicationPath.getAbsolutePath() + "/";
			Logger.info("App Path: %s", appPath);
			AppConfig.setRootPath(appPath);
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
            QueryManagerRepository qmr = QueryManagerRepository.getInstance();
			render(qeps, qmr);
		} catch (Exception e) {
			flash.error(e.getMessage());
			Logger.error(e.getMessage(), e);
			index();
		}
    }

    public static void executeQuery(Integer qep, String format) {
		try {
			long startTime = System.currentTimeMillis();
			if (format == null)
				format = "html";
			
			Logger.info("Execute QEP %s (%s)", qep, format);
			if (qep == null)
				throw new IllegalArgumentException("id parameter missing or not a number.");
			
			QueryManager queryManager = QueryManagerRepository.getInstance().get(qep);
			long loadPlanTime = System.currentTimeMillis();

			// If query has named parameters
			String[] paramNames = null;
			String[] paramValues = null;
			if (queryManager.getNamedParams() != null && queryManager.getNamedParams().size() > 0) {
				
				Iterator<String> it = queryManager.getNamedParams().iterator();
				paramNames = new String[queryManager.getNamedParams().size()];
				paramValues = new String[queryManager.getNamedParams().size()];
				
				for (int i=0; it.hasNext(); i++) {
					paramNames[i] = it.next();
					paramValues[i] = params.get(paramNames[i].substring(2)); // Do not consider '?:'
					if (paramValues[i] == null) {
						formParams(qep, format, queryManager.getNamedParams());
						// throw new IllegalArgumentException("Missing named parameter " + paramNames[i]);
					}
				}
				Logger.info("paramNames: %s - paramValues: %s", 
					Arrays.toString(paramNames), Arrays.toString(paramValues));
			}
			
			queryManager.executeRequest(paramNames, paramValues);
			RequestResult result = queryManager.getRequestResult();
			
			Metadata mt = result.getResultMetadata();
			ResultSet rs = result.getResultSet();
			long executionTime = System.currentTimeMillis();

			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			
			ResultOutput output = null;
			if (format.equals("html")) {
				response.setContentTypeIfNotSet("application/xml");
				output = new XMLOutput("/public/xml-to-html.xsl");
				
			} else if (format.equals("xml")) {
				response.setContentTypeIfNotSet("application/sparql-results+xml");
				response.setHeader("Content-Disposition", "attachment;filename=sparql-results.xml");
				output = new XMLOutput();
				
			} else if (format.equals("json")) {
				response.setContentTypeIfNotSet("application/sparql-results+json; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment;filename=sparql-results.json");
				output = new JSONOutput();
				
			} 
			
			if (output != null) {
				output.format(response.out, rs, mt);
				
			} else {
				throw new IllegalArgumentException("Invalid format: " + format);
			}
			long outputTime = System.currentTimeMillis();
			
			Logger.info("Plan(%s):%s Exec:%s Output(%s):%s Total:%s", 
					qep, (loadPlanTime - startTime), (executionTime - loadPlanTime), 
					format, (outputTime - executionTime), (outputTime - startTime));
			
		} catch (Exception e) {
			Logger.error(e.toString());
			flash.error(e.getMessage());
			Logger.error(e.getMessage(), e);
			index();
		}
    	
    }

    public static void formParams(Integer qep, String format, Set<String> namedParams) {
    	render(qep, format, namedParams);
    }
    
    public static void show(Integer id) {
    	try {
    		QEPInfo qepInfo = QEPInfo.getQEPInfo(id);
    		
    		if (qepInfo == null) {
    			throw new IllegalArgumentException("Could not find qep with id = " + id);
    		}

			String fileName = qepInfo.getPath();
			String description = qepInfo.getDescription();
    		String fileContents = FileUtils.readFile(AppConfig.CODIMS_HOME + fileName);
			render(fileName, description, fileContents);
			
		} catch (Exception e) {
			flash.error(e.getMessage());
			Logger.error(e.getMessage(), e);
			index();
		}
    }
    
    // To test: http://localhost:9001/edit/11
    public static void edit(Integer id) {
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

    public static void reload(Integer id) {
    	try {
    		if (id == null) {
    			throw new IllegalArgumentException("Invalid plan id = " + id);
    		}
    		
			QueryManager queryManager = QueryManagerRepository.getInstance().get(id, true);
			flash.success("Plan successfully loaded in " + queryManager.getQepCreationTime() + "ms.");
			index();
			
		} catch (Exception e) {
			flash.error(e.getMessage());
			Logger.error(e.getMessage(), e);
			index();
		}
    }
    public static void queryDiseasesOccurrences(){
    	render();
    }
    
public static void sendDiseasesResults(String url) {
		
		BufferedReader bis = null;
		InputStream is = null;
		Gson gson = new Gson();
		JsonModelBinding jmb = new JsonModelBinding();
		List<JsonModelBinding> mylists = null;
		String name = " ";
		// deserialização do JSON
		try {
			
			
			URL site = new URL(
					"http://qefweb.mooo.com/results/21/json?dsname=%22" + url
							+ "%22");

			BufferedReader read = new BufferedReader(new InputStreamReader(site.openStream()));
			String arquivo = read.readLine();
			String inputLine;
			String jsonstring = "{";
			while ((inputLine = read.readLine()) != null) {
				
				jsonstring = jsonstring+inputLine;
			}
			read.close();
			
			
			// teste
			BufferedReader br = new BufferedReader(new StringReader(jsonstring)); 
			//FileReader(	"/home/macedo/Desktop/query.json"));

			JsonModelInitial jsonresult = gson.fromJson(br,
					JsonModelInitial.class);

			// System.out.println();
			mylists = (ArrayList<JsonModelBinding>) jsonresult.results.bindings
					.clone();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		render(mylists);
	}

	public static void sendDrugsResults(String diseaseName) {
		BufferedReader bis = null;
		InputStream is = null;
		Gson gson = new Gson();
		JsonModelBinding jmb = new JsonModelBinding();
		List<JsonModelBindingDrug> mylistdiseases = null;
		String name = " ";
		// deserialização do JSON
		try {
			URLConnection connection = new URL(
					"http://qefweb.mooo.com/results/11/json/sparql-results.json")
					.openConnection();
			System.out.println("espere um momento");
			

			URL url = new URL(
					"http://qefweb.mooo.com/results/22/json?ds=%22" + diseaseName
							+ "%22");

			BufferedReader read = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String arquivo = read.readLine();
			String inputLine;
			String jsonstring = "{";
			while ((inputLine = read.readLine()) != null) {
				
				jsonstring = jsonstring+inputLine;
			}
			read.close();
			//System.out.println(jsonstring);

			// quando é obtido o arquivo Json
			BufferedReader br = new BufferedReader(new StringReader(jsonstring));  
			//FileReader("/home/macedo/Desktop/sparql-results.json"));

			// convert the json string back to object
			JsonModelInitialDrug jresult = gson
					.fromJson(br, JsonModelInitialDrug.class);

			mylistdiseases = (ArrayList<JsonModelBindingDrug>) jresult.results.bindings
					.clone();

			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		render(mylistdiseases);
	}

	public static void showDrugDetails(String drugName) {
		BufferedReader bis = null;
		InputStream is = null;
		Gson gson = new Gson();
		JsonModelBinding jmb = new JsonModelBinding();
		List<JsonModelBinding> mylistsdrugs = null;
		String name = " ";
		// deserialização do JSON
		try {
			
			
			URL site = new URL(
					"http://qefweb.mooo.com/results/23/json?dg=%22" + drugName
							+ "%22");

			BufferedReader read = new BufferedReader(new InputStreamReader(site.openStream()));
			String arquivo = read.readLine();
			String inputLine;
			String jsonstring = "{";
			while ((inputLine = read.readLine()) != null) {
				
				jsonstring = jsonstring+inputLine;
			}
			read.close();
			
			BufferedReader br = new BufferedReader(new StringReader(jsonstring)); 
			//FileReader(	"/home/macedo/Desktop/query.json"));

			JsonModelInitial jsonresult = gson.fromJson(br,
					JsonModelInitial.class);

			mylistsdrugs = (ArrayList<JsonModelBinding>) jsonresult.results.bindings
					.clone();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		render(mylistsdrugs);
	}
    
}

