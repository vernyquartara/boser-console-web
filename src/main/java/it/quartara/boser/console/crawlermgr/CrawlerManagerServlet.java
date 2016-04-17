package it.quartara.boser.console.crawlermgr;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.StartInstancesRequest;

import it.quartara.boser.console.AWSHelper;

@WebServlet("/crawler")
public class CrawlerManagerServlet extends HttpServlet {

	private static final long serialVersionUID = 617434256201619156L;
	
	private static final Logger log = LoggerFactory.getLogger(CrawlerManagerServlet.class);
	
	private static final String TARGET_URL = "http://boser.quartara.it/";
	
	@Resource(lookup="java:/jdbc/BoserDS")
	private DataSource ds;
	
	/*
	 * Rimanda alla URL della macchina remota, dopo averla accesa se necessario.
	 * In caso di accensione, viene schedulato il job che si occupa dello standby automatico.
	 * 
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.info("richiesta di avvio da IP {}", req.getRemoteAddr());
		String crawlerInstanceId, solrInstanceId;
		try {
			Connection conn = ds.getConnection();
			crawlerInstanceId = CrawlerManagerHelper.getCrawlerInstanceId(conn);
			solrInstanceId = CrawlerManagerHelper.getSolrInstanceId(conn);
			conn.close();
		} catch (SQLException e) {
			log.error("instance id non trovato, controllo remoto non disponibile", e);
			RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/jsps/home-error.jsp");
            rd.forward(req, resp);
            return;
		} 
		/*
		 * avviare la VM
		 * se lo STATUS è running non deve fare nulla (redirect diretto)
		 */
		AmazonEC2 ec2 = AWSHelper.createAmazonEC2Client(AWSHelper.CREDENTIALS_PROFILE);
        try {
        	Instance crawlerInstance = AWSHelper.getInstance(ec2, crawlerInstanceId);
        	if (crawlerInstance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
        		resp.sendRedirect(TARGET_URL);
        		return;
        	}
        	StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
        	startInstancesRequest.withInstanceIds(crawlerInstanceId);
        	ec2.startInstances(startInstancesRequest);
        	log.debug("crawler start request submitted");
        	
        	Instance solrInstance = AWSHelper.getInstance(ec2, solrInstanceId);
        	if (solrInstance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
        		resp.sendRedirect(TARGET_URL);
        		return;
        	}
        	startInstancesRequest = new StartInstancesRequest();
        	startInstancesRequest.withInstanceIds(solrInstanceId);
        	ec2.startInstances(startInstancesRequest);
        	log.debug("solr start request submitted");
        	
        	boolean running = false;
        	do {
        		try {
					Thread.sleep(2000);
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
        	CrawlerManagerHelper.scheduleStandbyJob(ds, getServletContext(), crawlerInstance.getLaunchTime(), false);
        	/*
        	 * attendo 40 secondi prima di restituire il controllo all'utente
        	 * per essere sicuro che Tomcat sia avviato
        	 */
        	try {
				Thread.sleep(40000);
			} catch (InterruptedException e) {
				//nop
			}
        	req.setAttribute("crawlerStatus", "operativo");
        	req.setAttribute("crawlerRunning", true);
        	RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/jsps/home.jsp");
    		rd.forward(req, resp);
        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }
	}

}
