package it.quartara.boser.console.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.StartInstancesRequest;

import it.quartara.boser.console.dto.InstanceDTO;
import it.quartara.boser.console.helper.AWSHelper;
import it.quartara.boser.console.helper.ConverterManagerHelper;

/**
 * servizio REST per
 * 1) ottenere l'ID di una istanza dato il nome
 * 2) ottenere lo stato di una istanza dato l'ID (e altri dati correlati)
 * 3) avviare una istanza (comprende avvio job di standby)
 * 
 * @author webny
 *
 */
@Path("/converter")
@Stateless
public class ConverterService {
	
	private static final Logger log = LoggerFactory.getLogger(ConverterService.class);
	
	@Resource(lookup="java:/jdbc/BoserDS")
	private DataSource ds;
	
	@GET
	@Produces("application/json")
	public InstanceDTO getConverterInstance() {
		log.debug("controllo status converter");
		AmazonEC2 ec2 = AWSHelper.createAmazonEC2Client(AWSHelper.CREDENTIALS_PROFILE);
		
		String instanceId = null;
		Connection conn;
		try {
			conn = ds.getConnection();
			instanceId = ConverterManagerHelper.getConverterInstanceId(conn);
			conn.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		}

		Instance instance = AWSHelper.getInstance(ec2, instanceId);
		log.debug("converter getInstanceId {}", instance.getInstanceId());
		log.debug("converter getLaunchTime {}", instance.getLaunchTime());
		log.debug("converter getPublicIpAddress {}", instance.getPublicIpAddress());
		log.debug("converter getState().getName() {}", instance.getState().getName());
		
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
	public InstanceDTO startInstance() {
		log.info("richiesta di avvio converter");
		String converterInstanceId;
		try {
			Connection conn = ds.getConnection();
			converterInstanceId = ConverterManagerHelper.getConverterInstanceId(conn);
			conn.close();
		} catch (SQLException e) {
			throw new WebApplicationException(e);
		} 
		
		AmazonEC2 ec2 = AWSHelper.createAmazonEC2Client(AWSHelper.CREDENTIALS_PROFILE);
        try {
        	Instance converterInstance = AWSHelper.getInstance(ec2, converterInstanceId);
        	
        	boolean converterIsUp = converterInstance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString());
        	
        	if (!converterIsUp) {
        		StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
        		startInstancesRequest.withInstanceIds(converterInstanceId);
        		ec2.startInstances(startInstancesRequest);
        		log.debug("crawler start request submitted");
        	}
        	
        	boolean running = false;
        	do {
        		try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					//nop
				}
        		log.debug("checking status...");
        		converterInstance = AWSHelper.getInstance(ec2, converterInstanceId);
        		if (converterInstance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
        			running = true;
        			log.debug("instances are running. lauch time: {}", converterInstance.getLaunchTime());
        		}
        	} while (!running);
        	/*
        	 * schedulazione job per lo standby automatico
        	 */
        	ConverterManagerHelper.scheduleStandbyJob(ds, converterInstance.getLaunchTime(), false);
        	/*
        	 * controllo che le macchine siano raggiungibili
        	 */
        	checkInstanceAvailability(converterInstance.getPublicDnsName());
        	
        	InstanceDTO dto = new InstanceDTO();
    		dto.setInstanceId(converterInstanceId);
    		dto.setState(converterInstance.getState().getName());
    		dto.setPublicDNSName(converterInstance.getPublicDnsName());
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
	private void checkInstanceAvailability(String converterDNSName) throws IOException {
		Request request = Request.Get("http://"+converterDNSName);
		
		boolean available = false;
    	do {
    		try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				//nop
			}
    		log.debug("checking availability...");
    		
    		available = 
    				HttpServletResponse.SC_OK ==
    				request.execute().returnResponse().getStatusLine().getStatusCode();
    		log.debug("converter available");
    	} while (!available);
	}
	
}
