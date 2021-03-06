package it.quartara.boser.console.listener;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;

import it.quartara.boser.console.helper.AWSHelper;
import it.quartara.boser.console.helper.ConverterManagerHelper;

/**
 * Se l'istanza è attiva al momento dell'avvio del contesto,
 * il job di standby deve essere schedulato.
 * @author webny
 *
 */
//@WebListener
public class ConverterManagerContextListener implements ServletContextListener {
	
	private static final Logger log = LoggerFactory.getLogger(ConverterManagerContextListener.class);
	
	@Resource(lookup="java:/jdbc/BoserDS")
	private DataSource ds;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		AmazonEC2 ec2 = AWSHelper.createAmazonEC2Client(AWSHelper.CREDENTIALS_PROFILE);
		String instanceId;
		try {
			Connection conn = ds.getConnection();
			instanceId = ConverterManagerHelper.getConverterInstanceId(conn);
			conn.close();
		} catch (SQLException e) {
			log.error("instance id non trovato, controllo remoto non disponibile", e);
			return;
		}
		Instance instance = AWSHelper.getInstance(ec2, instanceId);
		if (instance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
    		ConverterManagerHelper.scheduleStandbyJob(ds, instance.getLaunchTime(), true);
    	}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
