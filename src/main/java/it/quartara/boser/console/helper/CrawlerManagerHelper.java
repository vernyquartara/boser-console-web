package it.quartara.boser.console.helper;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.quartara.boser.console.job.CrawlerManagerJob;

public class CrawlerManagerHelper {
	
	private static final Logger log = LoggerFactory.getLogger(CrawlerManagerHelper.class);
	
	private static final String SELECT_STANDBY_INTERVAL = "select VALUE from PARAMETERS where id = 'STANDBY_INTERVAL'";
	private static final String SELECT_CRAWLER_INSTANCEID = "select VALUE from PARAMETERS where id = 'CRAWLER_INSTANCE_ID'";
	private static final String SELECT_SOLR_INSTANCEID = "select VALUE from PARAMETERS where id = 'SOLR_INSTANCE_ID'";
	
	public static short getStandbyInterval(Connection conn) throws SQLException {
		short standbyInterval = -1;
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery(SELECT_STANDBY_INTERVAL);
		if (rs.next()) {
			standbyInterval = rs.getShort(1);
			log.debug("intervallo di standby: {}", standbyInterval);
		} else {
			log.error("parametro 'STANDBY_INTERVAL' non presente in base dati");
		}
		return standbyInterval;
	}
	
	public static void scheduleStandbyJob(DataSource ds, Date instanceDate, boolean startNow) {
		short standbyInterval = -1;
		try {
			Connection conn = ds.getConnection();
			standbyInterval = getStandbyInterval(conn);
			conn.close();
			if (standbyInterval == -1) {
				log.error("impossibile schedulare il job di standby automatico");
				return;
			}
		} catch (SQLException e) {
			log.error("errore di lettura dal db, il job di standby non può essere schedulato", e);
			return;
		}
		if (startNow) {
			/*
			 * se richiesto l'avvio immediato, si imposta a 1 l'intervallo
			 * in modo che 1-1=0.
			 */
			standbyInterval = 1;
		}
		
		Scheduler scheduler;
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			
			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put(CrawlerManagerJob.INSTANCE_DATE_KEY, instanceDate);
			jobDataMap.put("ds", ds);
			
			JobDetail jobDetail =  newJob(CrawlerManagerJob.class)
									.withIdentity("CRWLMGRJOB", "BOSERCONSOLE")
									.usingJobData(jobDataMap)
									.build();
			Trigger trigger = newTrigger()
								.withIdentity("CRWLMGRTRG", "BOSERCONSOLE")
								.withSchedule(simpleSchedule()
											.withRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
											.withIntervalInSeconds(60)
											.withMisfireHandlingInstructionNextWithRemainingCount())
								.startAt(futureDate(standbyInterval-1, IntervalUnit.MINUTE))
								.build();
			scheduler.scheduleJob(jobDetail, trigger);
			log.info("job schedulato per l'avvio tra {} minuti", standbyInterval);
		} catch (SchedulerException e) {
			log.error("scheduler non trovato!!", e);
		}
	}
	
	public static String getCrawlerInstanceId(Connection conn) throws SQLException {
		String instanceId = null;
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery(SELECT_CRAWLER_INSTANCEID);
		if (rs.next()) {
			instanceId = rs.getString(1);
			log.debug("instance id: {}", instanceId);
		} else {
			log.error("parametro 'INSTANCE_ID' non presente in base dati");
		}
		return instanceId;
	}
	public static String getSolrInstanceId(Connection conn) throws SQLException {
		String instanceId = null;
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery(SELECT_SOLR_INSTANCEID);
		if (rs.next()) {
			instanceId = rs.getString(1);
			log.debug("instance id: {}", instanceId);
		} else {
			log.error("parametro 'INSTANCE_ID' non presente in base dati");
		}
		return instanceId;
	}

}
