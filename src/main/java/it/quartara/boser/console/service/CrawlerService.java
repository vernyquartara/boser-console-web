package it.quartara.boser.console.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.apache.http.client.fluent.Request;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.StartInstancesRequest;

import it.quartara.boser.console.dto.InstanceDTO;
import it.quartara.boser.console.helper.AWSHelper;
import it.quartara.boser.console.helper.CrawlerManagerHelper;

/**
 * servizio REST per
 * 1) ottenere l'ID di una istanza dato il nome
 * 2) ottenere lo stato di una istanza dato l'ID (e altri dati correlati)
 * 3) avviare una istanza (comprende avvio job di standby)
 * 
 * @author webny
 *
 */
@Path("/crawler")
@Stateless
public class CrawlerService {
	
	private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);
	
	@Resource(lookup="java:/jdbc/BoserDS")
	private DataSource ds;
	
	@GET
	@Produces("application/json")
	public InstanceDTO getCrawlerInstance() {
		log.debug("controllo status crawler");
		AmazonEC2 ec2 = AWSHelper.createAmazonEC2Client(AWSHelper.CREDENTIALS_PROFILE);
		
		String instanceId = null;
		Connection conn;
		try {
			conn = ds.getConnection();
			instanceId = CrawlerManagerHelper.getCrawlerInstanceId(conn);
			conn.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

		Instance instance = AWSHelper.getInstance(ec2, instanceId);
		log.debug("crawler getInstanceId {}", instance.getInstanceId());
		log.debug("crawler getLaunchTime {}", instance.getLaunchTime());
		log.debug("crawler getPublicIpAddress {}", instance.getPublicIpAddress());
		log.debug("crawler getState().getName() {}", instance.getState().getName());
		
		InstanceDTO dto = new InstanceDTO();
		dto.setInstanceId(instanceId);
		dto.setState(instance.getState().getName());
		dto.setPublicDNSName(instance.getPublicDnsName());
		dto.getLaunchTime();
		return dto;
	}
	
	@PUT
	@Path("/start")
	@Produces("application/json")
	public InstanceDTO startInstance(@FormParam("fakeParam") String fakeParam) {
		log.info("richiesta di avvio crawler");
		String crawlerInstanceId, solrInstanceId;
		try {
			Connection conn = ds.getConnection();
			crawlerInstanceId = CrawlerManagerHelper.getCrawlerInstanceId(conn);
			solrInstanceId = CrawlerManagerHelper.getSolrInstanceId(conn);
			conn.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		} 
		
		/*
		 * devono essere avviate entrambe le VM, crawler e solr
		 */
		AmazonEC2 ec2 = AWSHelper.createAmazonEC2Client(AWSHelper.CREDENTIALS_PROFILE);
        try {
        	Instance crawlerInstance = AWSHelper.getInstance(ec2, crawlerInstanceId);
        	Instance solrInstance = AWSHelper.getInstance(ec2, solrInstanceId);
        	
        	boolean crawlerIsUp = crawlerInstance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString());
        	boolean solrIsUp = solrInstance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString());
        	
        	if (crawlerIsUp && solrIsUp) {
        		InstanceDTO dto = new InstanceDTO();
        		dto.setInstanceId(crawlerInstanceId);
        		dto.setState(crawlerInstance.getState().getName());
        		dto.setPublicDNSName(crawlerInstance.getPublicDnsName());
        		dto.getLaunchTime();
        		return dto;
        	}
        	
        	if (!crawlerIsUp) {
        		StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
        		startInstancesRequest.withInstanceIds(crawlerInstanceId);
        		ec2.startInstances(startInstancesRequest);
        		log.debug("crawler start request submitted");
        	}
        	
        	if (!solrIsUp) {
        		StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
        		startInstancesRequest.withInstanceIds(solrInstanceId);
        		ec2.startInstances(startInstancesRequest);
        		log.debug("SOLR start request submitted");
        	}
        	
        	boolean running = false;
        	do {
        		try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					//nop
				}
        		log.debug("checking status...");
        		crawlerInstance = AWSHelper.getInstance(ec2, crawlerInstanceId);
        		solrInstance = AWSHelper.getInstance(ec2, solrInstanceId);
        		if (crawlerInstance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())
        				&& solrInstance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
        			running = true;
        			log.debug("instances are running. lauch time: {}(crawler), {}(solr)", crawlerInstance.getLaunchTime(), solrInstance.getLaunchTime());
        		}
        	} while (!running);
        	/*
        	 * schedulazione job per lo standby automatico
        	 */
        	CrawlerManagerHelper.scheduleStandbyJob(ds, crawlerInstance.getLaunchTime(), false);
        	/*
        	 * controllo che le macchine siano raggiungibili
        	 */
        	checkInstanceAvailability(crawlerInstance.getPublicDnsName(), solrInstance.getPublicDnsName());
        	
        	InstanceDTO dto = new InstanceDTO();
    		dto.setInstanceId(crawlerInstanceId);
    		dto.setState(crawlerInstance.getState().getName());
    		dto.setPublicDNSName(crawlerInstance.getPublicDnsName());
    		dto.getLaunchTime();
    		return dto;
        } catch (AmazonServiceException ase) {
        	log.error("problemi con il client AWS", ase);
            throw new WebApplicationException(ase);
        } catch (IOException e) {
        	log.error("problemi di comunicazione con i server remoti", e);
        	throw new WebApplicationException(e);
		}
	}

	/*
	 * Ogni n secondi invia una richiesta per controllare la raggiungiilità dei servizi.
	 * Si basa sul codice di stato HTTP.
	 */
	private void checkInstanceAvailability(String crawlerDNSName, String solrDNSName) throws IOException {
//		Request crawlerRequest = Request.Get("http://"+crawlerDNSName+":8080");
		Request crawlerRequest = Request.Get("http://boser.quartara.it");
		Request solrRequest = Request.Get("http://"+solrDNSName+":8983/solr");
		
		boolean crawlerAvailable = false, solrAvailable = false;
    	do {
    		try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				//nop
			}
    		log.debug("checking availability...");
    		
    		try {
	    		crawlerAvailable = 
	    				HttpServletResponse.SC_UNAUTHORIZED ==
	    				crawlerRequest.execute().returnResponse().getStatusLine().getStatusCode();
	    		
	    		solrAvailable =
	    				HttpServletResponse.SC_OK ==
	    				solrRequest.execute().returnResponse().getStatusLine().getStatusCode();
    		} catch (HttpHostConnectException e) {
    			log.debug("unavailable: {}", e.getMessage());
    			continue;
    		}
    		
    	} while (!crawlerAvailable || !solrAvailable);
    	log.debug("all available");
	}
	
}
