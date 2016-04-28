package com.powerleader.sambasetting;

import java.util.ArrayList;

public class SambaShareSetting {
	
	public native int CreatePrivateShare(String user, int userId, String shareName);
	public native int CreatePublicShare(String user, int userId,  String shareName);
	
	public native int DeletePrivateShare(String user,  int userId,  String shareName);
	public native int DeletePublicShare(String user,  int userId, String shareName);
	
	public native String MountFolder(int reqUserId, int resUserId, String path);
	
	public native String GetMountHome(int reqUserId, int resUserId);
	
	public native int FckD();
	
	public native int MountPublicShareToUser(int userId);
	public native int UmountPublicShareFromUser(int userId);
	
	public native ArrayList<SambaConf> GetPublicShareList();
	public native ArrayList<SambaConf> GetUserPrivateShareList(String userName);
	
}
