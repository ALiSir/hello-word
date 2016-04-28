package com.powerleader.sambasetting;

public class SambaConf {

	public String shareName;
	public String path;
	
	
	public SambaConf()
	{
		
	}
	
	public SambaConf(String shareName, String path)
	{
		this.shareName = shareName;
		this.path = path;
	}
	
}
