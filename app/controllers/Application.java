package controllers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import models.JsonModelBinding;
import models.JsonModelBindingDrug;
import models.JsonModelBindingDrugDetails;
import models.JsonModelInitial;
import models.JsonModelInitialDrug;
import models.JsonModelInitialDrugDetails;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import ch.epfl.codimsd.config.AppConfig;
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

import com.google.gson.Gson;
import com.mchange.v2.resourcepool.TimeoutException;

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
    	renderTemplate("Application/formParams.html", qep, format, namedParams);
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
   
    public static void showSparql(int query) {
        String fileContents = null, fileName = null, description = null;
	switch (query) {
		case 11: 
			fileName = "Q1";
                        description = "gets resources' URIs from the linkedgeodata dataset, together with their respective latitudes and longitudes obtained from the DBpedia dataset.";
			fileContents = 	"\nPREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                                       	"PREFIX geopos: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n\n" +
                                       	"SELECT ?s ?lat ?long \n" +
					"WHERE { \n" +
					"  SERVICE <http://virtuoso.mooo.com/lgd-dbpedia/sparql> { \n" +
					"    ?s owl:sameAs ?geo . \n" +
					"  } \n" +
					"  SERVICE <http://virtuoso.mooo.com/dbpedia-geo/sparql> { \n" +
					"    ?geo geopos:lat ?lat ; \n" +
					"    geopos:long ?long . \n" +
					"  } \n" +
					"}";
			break;
		case 12:
			fileName = "Q2";
                        description = "gets URIs of diseases and possible drugs used to treat each disease from the diseasome data source. In addition to these data, the full names of the drugs used in treating each disease are obtained from the dailymed data source.";
			fileContents =	"\nPREFIX ds: <http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/>\r\n" + 
					"PREFIX dm: <http://www4.wiwiss.fu-berlin.de/dailymed/resource/dailymed/>\r\n" + 
					"\r\n" + 
					"SELECT DISTINCT ?ds ?dg ?dgn\r\n" + 
					"WHERE {\r\n" + 
					"  SERVICE <http://virtuoso.mooo.com/diseasome/sparql> {\r\n" + 
					"    ?ds ds:possibleDrug ?dg .\r\n" + 
					"  }\r\n" + 
					"  SERVICE <http://virtuoso.mooo.com/dailymed/sparql> {\r\n" + 
					"    ?dg dm:fullName ?dgn .\r\n" + 
					"  }\r\n" + 
					"}";

			break;

		case 13:
			fileName = "Q3";
                        description = "gets, initially, the name of active pharmacological agents for some drugs in the dailymed dataset. From these values, Q3 checks: 1) the owl:sameAs links with sider, in order to get the side ects for each drug, and 2) the links dailymed:genericDrug with drugbank to retrieves chemical formulas of drugs.";
			fileContents = 	"\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
					"PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + 
					"PREFIX dm: <http://www4.wiwiss.fu-berlin.de/dailymed/resource/dailymed/>\r\n" + 
					"PREFIX db: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/>\r\n" + 
					"PREFIX sider: <http://www4.wiwiss.fu-berlin.de/sider/resource/sider/>\r\n" + 
					"\r\n" + 
					"SELECT ?dgain ?dgcf ?sen\r\n" + 
					"WHERE {\r\n" + 
					"  SERVICE <http://virtuoso.mooo.com/dailymed/sparql> {\r\n" + 
					"    ?dg dm:activeIngredient ?dgai . \r\n" + 
					"    ?dgai rdfs:label ?dgain .\r\n" + 
					"    ?dg dailymed:genericDrug ?gdg .\r\n" + 
					"    ?dg owl:sameAs ?sa .\r\n" + 
					"  }\r\n" + 
					"  SERVICE <http://virtuoso.mooo.com/sider/sparql> {\r\n" + 
					"    ?sa sider:sideEffect ?se .\r\n" + 
					"    ?se sider:sideEffectName ?sen .\r\n" + 
					"  }\r\n" + 
					"  SERVICE <http://virtuoso.mooo.com/drugbank/sparql> {\r\n" + 
					"    ?gdg db:chemicalFormula ?dgcf .\r\n" + 
					"  }\r\n" + 
					"}";
			break;
		case 14:
			fileName = "Q4";
                        description = "performs the union of generic names of drugs and medical treatment indications between the datasets drugbank and dailymed.";
			fileContents = 	"\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + 
					"PREFIX db: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/>\r\n" + 
					"PREFIX dm: <http://www4.wiwiss.fu-berlin.de/dailymed/resource/dailymed/>\r\n" + 
					"\r\n" + 
					"SELECT ?gn ?indication\r\n" + 
					"WHERE {\r\n" + 
					"  {\r\n" + 
					"    SERVICE <http://virtuoso.mooo.com/drugbank/sparql> {\r\n" + 
					"      ?dn db:genericName ?gn ;\r\n" + 
					"      db:indication ?indication.\r\n" + 
					"    }\r\n" + 
					"  }\r\n" + 
					"  UNION {\r\n" + 
					"    SERVICE <http://virtuoso.mooo.com/dailymed/sparql> {\r\n" + 
					"      ?dn dm:name ?gn ;\r\n" + 
					"      dm:indication ?indication .\r\n" + 
					"    }\r\n" + 
					"  }\r\n" + 
					"}";
			break;
		case 15:
			fileName = "Q5";
                        description = "performs the union of researchers names and their publications in the DBLP dataset.";
			fileContents = 	"\nPREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n" + 
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
					"\r\n" + 
					"SELECT ?label ?pub_title where {\r\n" + 
					"  {\r\n" + 
					"    SERVICE <http://virtuoso.mooo.com/dblp/sparql> {\r\n" + 
					"    ?publication dc:creator ?dblp_researcher ;\r\n" + 
					"    dc:title ?pub_title .\r\n" + 
					"    ?dblp_researcher rdfs:label ?label .\r\n" + 
					"    FILTER regex(?label, \"^Aab\")\r\n" + 
					"  }\r\n" + 
					"  ...\r\n" + 
					"  } UNION {\r\n" + 
					"    SERVICE <http://virtuoso.mooo.com/dblp/sparql> {\r\n" + 
					"      ?publication dc:creator ?dblp_researcher ;\r\n" + 
					"      dc:title ?pub_title .\r\n" + 
					"      ?dblp_researcher rdfs:label ?label .\r\n" + 
					"      FILTER regex(?label, \"^Jab\")\r\n" + 
					"    }\r\n" + 
					"  }\r\n" + 
					"}";
			break;
        }
	render(fileName, description, fileContents);
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
	boolean error = true;
	
	// deserialização do JSON
	try {
		URL site = new URL("http://qefweb.mooo.com/results/21/json?dsname=%22"+url+ "%22");

		BufferedReader read = new BufferedReader(new InputStreamReader(site.openStream()));
		String arquivo = read.readLine();
		String inputLine;
		String jsonstring = "{";
		while ((inputLine = read.readLine()) != null) {
			
			jsonstring = jsonstring+inputLine;
		}
		read.close();
		
		
		// test
		BufferedReader br = new BufferedReader(new StringReader(jsonstring)); 
		//FileReader(	"/home/macedo/Desktop/query.json"));

		JsonModelInitial jsonresult = gson.fromJson(br,
				JsonModelInitial.class);

		// System.out.println();
		mylists = (ArrayList<JsonModelBinding>) jsonresult.results.bindings
				.clone();

	}catch (NullPointerException te) {
		render(error);
	}catch (IOException te) {
		render(error);
	}catch (IndexOutOfBoundsException te) {
		render(error);
	}
	finally {
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
		
		
	render(mylists);
	}
}

	public static void sendDrugsResults(String diseaseName) {
		BufferedReader bis = null;
		InputStream is = null;
		Gson gson = new Gson();
		JsonModelBinding jmb = new JsonModelBinding();
		List<JsonModelBindingDrug> mylistdiseases = null;
		boolean error = true;
		// deserialização do JSON
		try {
			URLConnection connection = new URL(
					"http://qefweb.mooo.com/results/11/json/sparql-results.json")
					.openConnection();
					
			URL url = new URL(
					"http://qefweb.mooo.com/results/22/json?ds=<"+ diseaseName+">");

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
			JsonModelInitialDrug jresult = gson.fromJson(br, JsonModelInitialDrug.class);

			mylistdiseases = (ArrayList<JsonModelBindingDrug>)jresult.results.bindings.clone();

		} catch (NullPointerException te) {
			render(error);
		}catch (IOException te) {
			render(error);
		}catch (IndexOutOfBoundsException te) {
			render(error);
		}finally {
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
		List<JsonModelBindingDrugDetails> mylistsdrugdetails = null;
		boolean error = true;
		// deserialização do JSON
		try {
			URL site = new URL(
					"http://qefweb.mooo.com/results/23/json?dg=<"+drugName+">");

			BufferedReader read = new BufferedReader(new InputStreamReader(site.openStream()));
			String inputLine;
			String jsonstring = "";
			while ((inputLine = read.readLine()) != null) {
				jsonstring = jsonstring+inputLine;
			}
			read.close();
			
			//System.out.println(jsonstring);
			//test
			BufferedReader br = new BufferedReader(new StringReader(jsonstring)); 
			//FileReader(	"/home/macedo/Desktop/query.json"));

			JsonModelInitialDrugDetails jsonresult = gson.fromJson(br,
					JsonModelInitialDrugDetails.class);

			mylistsdrugdetails = (ArrayList<JsonModelBindingDrugDetails>) jsonresult.results.bindings
					.clone();

		} catch (NullPointerException te) {
			render(error);
		}catch (IOException te) {
			render(error);
		}catch (IndexOutOfBoundsException te) {
			render(error);
		}finally {
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

		render(mylistsdrugdetails);
	}
}
