package it.quartara.boser.console.pdfcmgr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.sql.DataSource;

import org.apache.commons.lang3.time.DateUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.StopInstancesRequest;

/**
 * Se ci sono conversioni in corso non effettua alcuna operazione.
 * 
 * Altrimenti, verifica l'orario di avvio dell'instanza.
 * Se è passata un'ora, lo aggiorna (aggiungendo un'ora).
 * 
 * Quindi, mette in stop l'istanza se sono passati più di N minuti dall'ultima conversione e
 * se sono passati più di N minuti dall'orario di avvio (eventualmente aggiornato).
 * Se l'istanza viene stoppata, il job si auto-deschedula.
 * N è un parametro su DB.
 * 
 * @author Verny Quartara
 *
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class PDFCManagerJob implements Job {
	
	private static final Logger log = LoggerFactory.getLogger(PDFCManagerJob.class);
	
	public static final String INSTANCE_DATE_KEY = "instanceDate";
	public static final String SELECT_RUNNING_CONVERTIONS = "SELECT ID FROM PDF_CONVERTIONS where STATE in ('READY','STARTED')";
	public static final String SELECT_LAST_CONVERTION_DATE = "select max(ENDDATE) from PDF_CONVERTIONS";
	
	private Date instanceDate;
	private DataSource ds;
	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.ITALY);

	/*
	 * Il metodo deve essere transazionale SERIALIZZATO sulle conversioni,
	 * poiché non deve essere possibile avviare una conversione
	 * mentre si sta stoppando l'istanza.
	 * 
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug("executing, instanceDate: {}", dateFormat.format(instanceDate));
		String instanceId;
		try {
			Connection conn = ds.getConnection();
			instanceId = PDFCManagerHelper.getInstanceId(conn);
			conn.close();
		} catch (SQLException e) {
			log.error("instance id non trovato, controllo remoto non disponibile");
			throw new JobExecutionException(e);
		} 
		AmazonEC2 ec2 = AWSHelper.createAmazonEC2Client(AWSHelper.CREDENTIALS_PROFILE);
		Instance instance = AWSHelper.getInstance(ec2, instanceId);
		
		updateInstanceDate(context.getJobDetail().getJobDataMap());
		/*
		 * se l'istanza è già stoppata, non ha più senso eseguire il job, si deschedula
		 */
    	if (instance.getState().getName().equalsIgnoreCase(InstanceStateName.Stopped.toString())) {
    		log.info("istanza già stoppata, si deschedula il job");
    		try {
				context.getScheduler().unscheduleJob(context.getTrigger().getKey());
				return;
			} catch (SchedulerException e) {
				log.error("impossibile deschedulare il job", e);
				throw new JobExecutionException(e);
			}
    	}
    	if (!instance.getState().getName().equalsIgnoreCase(InstanceStateName.Running.toString())) {
    		String msg = "istanza non stoppata né attiva, si rimanda l'esecuzione";
    		log.error(msg);
    		throw new JobExecutionException(msg);
    	}
    	Connection conn = null;
		try {
			conn = ds.getConnection();
			conn.setAutoCommit(Boolean.FALSE);
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		} catch (SQLException e) {
			log.error("impossibile aprire la connessione al db", e);
			throw new JobExecutionException(e);
		}
		
    	if (isCurrentlyConverting(conn)) {
    		try {
				conn.commit();
				conn.close();
			} catch (SQLException e) {
				log.error("impossibile effettuare il commit della connessione al db", e);
			}
			return;
    	}
		/*
		 * se non ci sono, seleziono la data dell'ultima conversione
		 * e l'intervallo di standy
		 */
		Date lastConversionDate = getLastConversionDate(conn);
		short standbyInterval = -1;
		try {
			standbyInterval = PDFCManagerHelper.getStandbyInterval(conn);
			if (standbyInterval == -1) {
				log.error("impossibile proseguire, il job sarà deschedulato");
				context.getScheduler().unscheduleJob(context.getTrigger().getKey());
			}
		} catch (SQLException e) {
			log.error("errore di lettura dal db", e);
			try {
				conn.rollback();
				conn.close();
			} catch (SQLException e1) {
				log.error("impossibile effettuare il rollback della connessione al db", e1);
			}
			throw new JobExecutionException(e);
		} catch (SchedulerException e) {
			log.error("impossibile deschedulare il job", e);
			try {
				conn.rollback();
				conn.close();
			} catch (SQLException e1) {
				log.error("impossibile effettuare il rollback della connessione al db", e1);
			}
			throw new JobExecutionException(e);
		}
		/*
		 * effettuo il controllo sulle date
		 * e stoppo se necessario
		 */
		if (isTimeToStandby(instanceDate, lastConversionDate, standbyInterval)) {
			log.info("stopping instance...");
			StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
			stopInstancesRequest.withInstanceIds(instanceId);
			ec2.stopInstances(stopInstancesRequest);
			log.info("richiesta di stop inviata. il job sarà deschedulato");
			try {
				context.getScheduler().unscheduleJob(context.getTrigger().getKey());
			} catch (SchedulerException e) {
				log.error("impossibile deschedulare il job", e);
				throw new JobExecutionException(e);
			}
		}
		try {
			conn.commit();
			conn.close();
		} catch (SQLException e) {
			log.error("impossibile effettuare il commit della connessione al db", e);
		}
	}

	/*
	 * se è passata più di un'ora dall'orario di avvio,
	 * aggiorna l'orario aggiungendo un'ora.
	 * 
	 * algoritmo: si considerano le date instanceDate e now.
	 * si conta quante ore bisogna aggiugere a instanceDate.
	 * per farlo, si aggiunge 1 ora a instanceDate finché valgono due condizioni:
	 * 1) instanceDate + counter (counter parte da 0) è minore di now - 1 ora
	 * 2) trunc(instanceDate, H) minore di trunc(now)
	 */
	private void updateInstanceDate(JobDataMap jobDataMap) {
		Date now = new Date();
		Date truncatedInstanceDate = DateUtils.truncate(instanceDate, Calendar.HOUR);
		Date truncatedActualDate = DateUtils.truncate(now, Calendar.HOUR);
		int counter = 0;
		while (DateUtils.addHours(instanceDate, counter).before(DateUtils.addHours(now, -1))
				&& truncatedInstanceDate.before(truncatedActualDate)) {
			truncatedInstanceDate = DateUtils.addHours(truncatedInstanceDate, 1);
			counter++;
		}
		if (counter>0) {
			instanceDate = DateUtils.addHours(instanceDate, counter);
			log.info("updating instanceDate to {}", dateFormat.format(instanceDate));
			jobDataMap.put(PDFCManagerJob.INSTANCE_DATE_KEY, instanceDate);
		}
	}

	/**
	 * Controlla se ci sono esecuzioni in corso.
	 * @return true se esistono conversioni in stato READY o STARTED, false altrimenti
	 * @throws JobExecutionException 
	 */
	private boolean isCurrentlyConverting(Connection conn) throws JobExecutionException {
		Statement stat;
		try {
			stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(SELECT_RUNNING_CONVERTIONS);
			if (rs.next()) {
				log.info("sono presenti conversioni in corso");
				return true;
			} else {
				log.info("nessuna conversione in corso");
			}
		} catch (SQLException e) {
			log.error("errore di lettura dal db", e);
			throw new JobExecutionException(e);
		}
		return false;
	}
	
	/**
	 * Restituisce la data dell'ultima conversione effettuata.
	 * @return
	 * @throws JobExecutionException 
	 */
	private Date getLastConversionDate(Connection conn) throws JobExecutionException {
		Date lastConversionDate = null;
		try {
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(SELECT_LAST_CONVERTION_DATE);
			if (rs.next()) {
				lastConversionDate = new Date(rs.getTimestamp(1).getTime());
				log.info("data ultima conversione effettuata: {}", dateFormat.format(lastConversionDate));
			} else {
				lastConversionDate = DateUtils.addDays(new Date(), -1);
				log.warn("non sono presenti conversioni effettuate in base dati,"
						+ "si considera la data di ieri ({})", dateFormat.format(lastConversionDate));
			}
		} catch (SQLException e) {
			log.error("errore di lettura dal db", e);
			throw new JobExecutionException(e);
		}
		return lastConversionDate;
	}
	
	private boolean isTimeToStandby(Date instanceDate, Date lastConversionDate, short interval) {
		log.debug("checking if it's time to standby...");
		Date now = new Date();
		log.debug("instance date: {}", dateFormat.format(instanceDate));
		log.debug("last conversion date: {}", dateFormat.format(lastConversionDate));
		log.debug("current date: {}", dateFormat.format(now));
		log.debug("standby after: {} minutes", interval);
		if (now.after(DateUtils.addMinutes(lastConversionDate, interval))
				&& now.after(DateUtils.addMinutes(instanceDate, interval))) {
			log.info("it's time to standby");
			return true;
		}
		log.info("it isn't time to standby yet");
		return false;
	}

	public void setInstanceDate(Date instanceDate) {
		this.instanceDate = instanceDate;
	}

	public void setDs(DataSource ds) {
		this.ds = ds;
	}

}
