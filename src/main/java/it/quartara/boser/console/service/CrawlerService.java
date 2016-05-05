package it.quartara.boser.console.service;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.console.dto.InstanceDTO;

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
	
	@GET
	@Produces("application/json")
	public InstanceDTO getCrawlerInstance() {
		InstanceDTO instance = new InstanceDTO();
		instance.setInstanceId("ijoiji");
		instance.setState("stopped");
		instance.setPublicDNSName("dns");
		return instance;
	}
	
	@POST
	@Path("/start")
	@Produces("application/json")
	public InstanceDTO startInstance() {
		
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			//nop
		}
		
		InstanceDTO instance = new InstanceDTO();
		instance.setInstanceId("ijoiji");
		instance.setState("started");
		instance.setPublicDNSName("dns");
		return instance;
	}

}
