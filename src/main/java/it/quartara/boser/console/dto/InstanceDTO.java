package it.quartara.boser.console.dto;

import java.io.Serializable;
import java.util.Date;

public class InstanceDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4915081208120888756L;
	
	private String instanceId;
	private String instanceName;
	private String publicDNSName;
	private String state;
	private Date launchTime;
	
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public String getPublicDNSName() {
		return publicDNSName;
	}
	public void setPublicDNSName(String publicDNSName) {
		this.publicDNSName = publicDNSName;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Date getLaunchTime() {
		return launchTime;
	}
	public void setLaunchTime(Date launchTime) {
		this.launchTime = launchTime;
	}

}
