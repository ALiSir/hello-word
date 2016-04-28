package com.powerleader.sambasetting;

public class SambaUserSetting {
	
		public native int AddSambaUser(String user, int userId, String password);
		public native int DeleteSambaUser(String user);
		public native int EnableSambaUser(String user);
		public native int DisableSambaUser(String user);
		
		public native int ChangeSambaUserPassword(String user, String password);
		
		public native int CreateUserHome(int userId);
}
