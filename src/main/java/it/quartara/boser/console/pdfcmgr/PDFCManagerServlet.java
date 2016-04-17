package it.quartara.boser.console.pdfcmgr;
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

@WebServlet("/converter")
public class PDFCManagerServlet extends HttpServlet {

	private static final long serialVersionUID = 617434256201619156L;
	
	private static final Logger log = LoggerFactory.getLogger(PDFCManagerServlet.class);
	
	private static final String TARGET_URL = "http://boser.quartara.it/conversionHome";
	
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
		String instanceId;
		try {
			Connection conn = ds.getConnection();
			instanceId = PDFCManagerHelper.getInstanceId(conn);
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
        	Instance instance = AWSHelper.getInstance(ec2, instanceId);
        	if (instance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
        		resp.sendRedirect(TARGET_URL);
        		return;
        	}
        	StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
        	startInstancesRequest.withInstanceIds(instanceId);
        	ec2.startInstances(startInstancesRequest);
        	log.debug("start request submitted");
        	
        	boolean running = false;
        	do {
        		try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					//nop
				}
        		log.debug("checking status...");
        		instance = AWSHelper.getInstance(ec2, instanceId);
        		if (instance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
        			running = true;
        			log.debug("instance's running. lauch time: {}", instance.getLaunchTime());
        		}
        	} while (!running);
        	/*
        	 * schedulazione job per lo standby automatico
        	 */
        	PDFCManagerHelper.scheduleStandbyJob(ds, getServletContext(), instance.getLaunchTime(), false);
        	/*
        	 * attendo 25 secondi prima di restituire il controllo all'utente
        	 * per essere sicuro che Tomcat sia avviato
        	 */
        	try {
				Thread.sleep(25000);
			} catch (InterruptedException e) {
				//nop
			}
        	req.setAttribute("converterStatus", "operativo");
        	req.setAttribute("converterRunning", true);
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
