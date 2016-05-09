package it.quartara.boser.console;
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

import it.quartara.boser.console.helper.AWSHelper;
import it.quartara.boser.console.helper.ConverterManagerHelper;
import it.quartara.boser.console.helper.CrawlerManagerHelper;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

	private static final long serialVersionUID = 617434256201619156L;
	
	private static final Logger log = LoggerFactory.getLogger(HomeServlet.class);
	
	@Resource(lookup="java:/jdbc/BoserDS")
	private DataSource ds;
	
	/*
	 * Controlla lo stato della macchina remota.
	 * 
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("controllo status, richiesta da IP {}", req.getRemoteAddr());
		AmazonEC2 ec2 = AWSHelper.createAmazonEC2Client(AWSHelper.CREDENTIALS_PROFILE);
		
		
		String crawlerStatus = "in stand-by";
    	boolean crawlerRunning = false;
		String converterStatus = "in stand-by";
    	boolean converterRunning = false;
    	try {
    		if (crawlerIsUp(ec2)) {
    			crawlerStatus = "operativo";
    			crawlerRunning = true;
    		}
    		if (converterIsUp(ec2)) {
    			converterStatus = "operativo";
    			converterRunning = true;
    		}
    	} catch (SQLException sqle) {
    		log.error("instance id non trovato, controllo remoto non disponibile", sqle);
			RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/jsps/home-error.jsp");
            rd.forward(req, resp);
            return;
    	} catch (AmazonServiceException ase) {
    		log.error("Caught Exception: " + ase.getMessage());
            log.error("Reponse Status Code: " + ase.getStatusCode());
            log.error("Error Code: " + ase.getErrorCode());
            log.error("Request ID: " + ase.getRequestId());
            RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/jsps/home-error.jsp");
            rd.forward(req, resp);
    	}
		
    	req.setAttribute("crawlerStatus", crawlerStatus);
    	req.setAttribute("crawlerRunning", crawlerRunning);
		req.setAttribute("converterStatus", converterStatus);
    	req.setAttribute("converterRunning", converterRunning);
    	RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/jsps/home.jsp");
		rd.forward(req, resp);
	}

	private boolean crawlerIsUp(AmazonEC2 ec2) throws SQLException {
		String instanceId;
		Connection conn = ds.getConnection();
		instanceId = CrawlerManagerHelper.getCrawlerInstanceId(conn);
		conn.close();

		Instance instance = AWSHelper.getInstance(ec2, instanceId);

		log.debug("getInstanceId {}", instance.getInstanceId());
		log.debug("getLaunchTime {}", instance.getLaunchTime());
		log.debug("getPublicIpAddress {}", instance.getPublicIpAddress());
		log.debug("getState().getName() {}", instance.getState().getName());
		// log.debug("getStateReason().getMessage() {}",
		// instance.getStateReason().getMessage());
		// log.debug("getStateTransitionReason {}",
		// instance.getStateTransitionReason());

		if (instance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
			return true;
		}
		return false;
	}

	private boolean converterIsUp(AmazonEC2 ec2) throws SQLException {
		String instanceId;
		Connection conn = ds.getConnection();
		instanceId = ConverterManagerHelper.getConverterInstanceId(conn);
		conn.close();

		Instance instance = AWSHelper.getInstance(ec2, instanceId);

		log.debug("getInstanceId {}", instance.getInstanceId());
		log.debug("getLaunchTime {}", instance.getLaunchTime());
		log.debug("getPublicIpAddress {}", instance.getPublicIpAddress());
		log.debug("getState().getName() {}", instance.getState().getName());
		// log.debug("getStateReason().getMessage() {}",
		// instance.getStateReason().getMessage());
		// log.debug("getStateTransitionReason {}",
		// instance.getStateTransitionReason());

		if (instance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
			return true;
		}
		return false;

	}

}
