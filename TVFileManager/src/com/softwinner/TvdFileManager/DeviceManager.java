/**
 * you can find the path of sdcard,flash and usbhost in here
 * @author chenjd
 * @email chenjd@allwinnertech.com
 * @data 2011-8-10
 */
package com.softwinner.TvdFileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.powerleader.sambasetting.SambaConf;
import com.powerleader.sambasetting.SambaShareSetting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.os.Environment;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.provider.DocumentsContract.Root;

public class DeviceManager{
	
	public static final String SAMBA_PRIVATE_SHARE_DIR_HOME = "/mnt/shell/emulated/";
	
	public static final String WORLD_SHAREDPREFERENCES_NAME = "sbsharename4";
	
	private static String TAG = "DeviceManager";
	
	//private ArrayList<String> localDevicesList;
	//private ArrayList<String> sdDevicesList;
	//private ArrayList<String> usbDevicesList;
	//private ArrayList<String> sataDevicesList;
	/*
	 * 本地存储列表
	 * 只有一个：/storage/emulated/legacy
	 */
	private ArrayList<String> internalDevicesList;
	//private ArrayList<String> mountedDevicesList;
	
	/**
	 * Samba公开共享列表
	 */
	private ArrayList<String> sambaPublicList;
	/**
	 * Samba私有共享列表
	 */
	private ArrayList<String> sambaPrivateList;
	
	/**
	 * 系统用户名称列表
	 */
	private ArrayList<String> userNameList;
	
	/**
	 * 系统用户id表 : 用户名 -> 用户id
	 */
	private Map<String, Integer> userIdMap;
	
	private Context mContext;
	private Context mSettingContext;
	private StorageManager manager;
	private SambaShareSetting mSambaShareSetting;
	/**
	 * 浏览共享的所属用户id
	 */
	private int mUserId;
	/**
	 * 当前登录系统的用户id
	 */
	private int mCurrentUserId;
	
	/**
	 * 是否是从系统设置的多用户里面点进来
	 */
	private boolean isUserIdEmpty;
	
	private String mMountHome;
	private String mExternalStoragePath;
	
	private Map<String, String> mMountMap = new HashMap<String, String>();
	
	static {
		System.loadLibrary("mountfolder");
		System.loadLibrary("sambasetting_tvfm");
	}

	public DeviceManager(Context context, int uid)
	{
		mContext = context;
		mUserId = uid;
		mCurrentUserId = UserHandle.myUserId();
		mSambaShareSetting = new SambaShareSetting();
		
		/* 获取总设备列表 */
//		localDevicesList = new ArrayList<String>();
		String[] volumeList;
		manager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
//		volumeList = manager.getVolumePaths();
//		for(int i = 0; i < volumeList.length; i ++)
//		{
//			localDevicesList.add(volumeList[i]);
//		}
		
		try {
			mSettingContext = mContext.getApplicationContext().createPackageContext("com.android.settings", Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        
		mSambaShareSetting.FckD();
        /* 获取内部存储设备路径列表 */
        internalDevicesList = new ArrayList<String>();
        mExternalStoragePath = Environment.getExternalStorageDirectory().getPath();
        mMountHome = mSambaShareSetting.GetMountHome(mCurrentUserId, mUserId);
        if(mUserId == -1) {
        	//String path = "/storage/emulated/legacy";
        	isUserIdEmpty = true;
        	internalDevicesList.add(mExternalStoragePath);
        } else {
        	isUserIdEmpty = false;
        	//internalDevicesList.add(SAMBA_PRIVATE_SHARE_DIR_HOME + mUserId);
        }
//        sdDevicesList = new ArrayList<String>();
//        usbDevicesList = new ArrayList<String>();
//        sataDevicesList = new ArrayList<String>();
        sambaPublicList = new ArrayList<String>();
        sambaPrivateList = new ArrayList<String>();
        userNameList = new ArrayList<String>();
        userIdMap = new HashMap<String, Integer>();
        
        String path;
//        for(int i = 0; i < localDevicesList.size(); i++)
//        {
//        	path = localDevicesList.get(i);
//        	if(!path.equals(Environment.getExternalStorageDirectory().getPath()))
//        	{
//        		if(path.contains("sd"))
//        		{
//        			/* 获取SD卡设备路径列表 */
//        			sdDevicesList.add(path);
//        		}
//        		else if(path.contains("usb"))
//        		{
//        			/* 获取USB设备路径列表 */
//        			usbDevicesList.add(path);
//        		}
//        		else if(path.contains("sata"))
//        		{
//        			/* 获取sata设备路径列表 */
//        			sataDevicesList.add(path);
//        		}
//        	}
//        }
	}
//	public boolean isLocalDevicesRootPath(String path)
//	{
//		for(int i = 0; i < localDevicesList.size(); i++)
//		{
//			if(path.equals(localDevicesList.get(i)))
//				return true;
//		}
//		return false;
//	}
	
	/**
	 * 只有当前登录用户是共享的所属用户才能添加私有共享
	 */
	public boolean isSambaOwner() {
		if(mCurrentUserId != mUserId) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isSambaPublicDirPath(String path) {
		if(sambaPublicList.contains(path)) {
			return true;
		}
		return false;
	}
	
	public boolean isSambaPrivateDirPath(String path) {
		if(sambaPrivateList.contains(path)) {
			return true;
		}
		return false;
	}
	
	public ArrayList<String> getSambaPublicDirList() {
		sambaPublicList.clear();
		ArrayList<SambaConf> dir_lists = mSambaShareSetting.GetPublicShareList();
		if(dir_lists != null && dir_lists.size() > 0) {
			for(SambaConf dir : dir_lists) {
				if(!TextUtils.isEmpty(dir.path)) {
					if(dir.path.startsWith("/mnt/shell/emulated")) {
						int pos = dir.path.indexOf("public_share/");
						if(pos != -1) {
							sambaPublicList.add(mExternalStoragePath + "/" + dir.path.substring(pos));
						}
					}
				}
			}
		}
		return (ArrayList<String>) sambaPublicList.clone();
	}
	
	public ArrayList<String> getSambaPrivateDirList() {
		sambaPrivateList.clear();
		if(mUserId != -1) {
//        	try {
//        		SharedPreferences sp = mSettingContext.getSharedPreferences(WORLD_SHAREDPREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
//        		if(sp != null) {
//        			Set<String> shares = sp.getStringSet(mUserId + "_", null);
//        			if(shares != null && shares.size() > 0) {
//        				for(String share : shares) {
//        					sambaPrivateList.add(mMountHome + "/" + share);
//        					mMountMap.put(mMountHome + "/" + share, SAMBA_PRIVATE_SHARE_DIR_HOME + mUserId + "/" + share);
//        				}
//        			}
//        		}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			ArrayList<SambaConf> dir_lists = mSambaShareSetting.GetUserPrivateShareList(getUsername(mUserId));
			if(dir_lists != null && dir_lists.size() > 0) {
				for(SambaConf dir : dir_lists) {
					int pos = dir.path.lastIndexOf("/");
					if(pos != -1) {
						String share = dir.path.substring(pos + 1);
						sambaPrivateList.add(mMountHome + "/" + share);
						mMountMap.put(mMountHome + "/" + share, SAMBA_PRIVATE_SHARE_DIR_HOME + mUserId + "/" + share);
					}
				}
			}
        }
		return (ArrayList<String>) sambaPrivateList.clone();
	}
	
	public int createSambaPublicDir(String shareName) {
		return mSambaShareSetting.CreatePublicShare(getUsername(mCurrentUserId), mCurrentUserId, shareName);
	}
	
	public int createSambaPrivateDir(String shareName) {
		return mSambaShareSetting.CreatePrivateShare(getUsername(mCurrentUserId), mCurrentUserId, shareName);
	}
	
	public void mountFolder(String path) {
		String originPath = mMountMap.get(path);
		Log.e(TAG, "uid = " + mCurrentUserId + ", userid = " + mUserId + ", originPath = " + originPath);
		mSambaShareSetting.MountFolder(mCurrentUserId, mUserId, originPath);
	}
	
	public String getChannelId(String username) {
		SharedPreferences sp = mSettingContext.getSharedPreferences(WORLD_SHAREDPREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
		if(sp != null) {
			return sp.getString(username + "_channelid", "");
		}
		return null;
	}
	
	public int getAuthExpireTime() {
		SharedPreferences sp = mSettingContext.getSharedPreferences(WORLD_SHAREDPREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
		int default_time = 10 * 60 * 1000;
		if(sp != null) {
			return sp.getInt("auth_expire_time", default_time);
		}
		return default_time;
	}
	
	public void setUserId(int userId) {
		mUserId = userId;
	}
	
	public int getUserId() {
		return mUserId;
	}
	
	public boolean isUserIdEmpty() {
		return isUserIdEmpty;
	}
	
	public int getUserId(String username) {
		if(userIdMap.containsKey(username)) {
			return userIdMap.get(username);
		}
		return 0;
	}
	
	public String getUsername(int userId) {
		UserManager userManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
		UserInfo userInfo = userManager.getUserInfo(userId);
		return userInfo.name;
	}
	
	public List<String> getUserList() {
		userNameList.clear();
		UserManager userManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
		List<UserInfo> userList = userManager.getUsers();
		if(userList != null && userList.size() > 0) {
			for(UserInfo userInfo : userList) {
				userNameList.add(userInfo.name);
				userIdMap.put(userInfo.name, userInfo.id);
			}
		}
		return (List<String>) userNameList.clone();
	}
	
	public boolean isUser(String user) {
		if(userNameList.contains(user)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取总设备的列表
	 * @return
	 */
//	public ArrayList<String> getLocalDevicesList()
//	{
//		return (ArrayList<String>) localDevicesList.clone();
//	}
	
	/**
	 * 获取当前被挂载的设备列表
	 */
//	public ArrayList<String> getMountedDevicesList()
//	{
//		String state;
//		ArrayList<String> mountedDevices = new ArrayList<String>();
//		try 
//		{
//	        for(int i = 0; i < localDevicesList.size(); i++)
//	        {
//	            state = manager.getVolumeState(localDevicesList.get(i));
//	           	if(state.equals(Environment.MEDIA_MOUNTED))
//	           	{
//	           		mountedDevices.add(localDevicesList.get(i));
//	           	}
//	        }
//	    } catch (Exception rex) 
//	    {
//	    }
//	    return mountedDevices;
//	}
	
	public boolean isInterStoragePath(String path)
	{
		if(internalDevicesList.contains(path))
		{
			return true;
		}
		return false;
	}
	
//	public boolean isSdStoragePath(String path)
//	{
//		if(sdDevicesList.contains(path))
//		{
//			return true;
//		}
//		return false;
//	}
//	
//	public boolean isUsbStoragePath(String path)
//	{
//		if(usbDevicesList.contains(path))
//		{
//			return true;
//		}
//		return false;
//	}
//	
//	public boolean isSataStoragePath(String path)
//	{
//		if(sataDevicesList.contains(path))
//		{
//			return true;
//		}
//		return false;
//	}
//	
//	public ArrayList<String> getSataDevicesList()
//	{
//		return (ArrayList<String>) sataDevicesList.clone();
//	}
//	
//	public ArrayList<String> getSdDevicesList()
//	{
//		return (ArrayList<String>) sdDevicesList.clone();
//	}
//	
//	public ArrayList<String> getUsbDevicesList()
//	{
//		return (ArrayList<String>) usbDevicesList.clone();
//	}
	
	public boolean isInternalDevicesList(String path)
	{
		if(internalDevicesList.contains(path)) {
			return true;
		}
		return false;
	}
	
	public ArrayList<String> getInternalDevicesList()
	{
		return (ArrayList<String>) internalDevicesList.clone();
	}
	

	
//	public boolean hasMultiplePartition(String dPath)
//	{
//		try
//		{
//			File file = new File(dPath);
//			String minor = null;
//			String major = null;
//			for(int i = 0; i < localDevicesList.size(); i++)
//			{
//				if(dPath.equals(localDevicesList.get(i)))
//				{
//					String[] list = file.list();
//					/********
//					 *add by hechuanlong 2013-08-20  start{{----------------------------
//					 *fix a bug when list.length==0 the device be a multiple partition
//					 ********/
//					 if(0 == list.length)
//					 {
//					 		return false;
//					 }
//					/********
//					 *------------------------}}end
//					 *********/
//					for(int j = 0; j < list.length; j++)
//					{
//						/* 如果目录命名规则不满足"主设备号:次设备号"(当前分区的命名规则),则返回false */
//						int lst = list[j].lastIndexOf("_");
//						if(lst != -1 && lst != (list[j].length() -1))
//						{
//							major = list[j].substring(0, lst);
//							minor = list[j].substring(lst + 1, list[j].length());
//							try
//							{
//							
//								Integer.valueOf(major);
//								Integer.valueOf(minor);
//							}
//							catch(NumberFormatException e)
//							{
//								/* 如果该字符串不能被解析为数字,则退出 */
//								return false;
//							}
//						}
//						else 
//						{
//							return false;
//						}
//					}
//					return true;
//				}
//			}
//			return false;
//		}
//		catch(Exception e)
//		{
//			Log.e(TAG, "hasMultiplePartition() exception e");
//			return false;
//		}
//	}
	
	
//	public ArrayList<String> getNetDeviceList(){
//		SharedPreferences pref = mContext.getSharedPreferences("Device", 0);
//		String list = pref.getString("Net", null);
//		if(list != null){
//			String[] split = list.split(",");
//			if(split != null){
//				ArrayList<String> devList = new ArrayList<String>();
//				for(int i = 0; i < split.length; i++){
//					devList.add(split[i]);
//				}
//				return devList;
//			}
//		}
//		return null;
//	}
//	
//	public void saveNetDevice(String devPath){
//		if(devPath == null){
//			return;
//		}
//		SharedPreferences pref = mContext.getSharedPreferences("Device", 0);
//		SharedPreferences.Editor editor = pref.edit();
//		String list = pref.getString("Net", null);
//		if(list == null){
//			editor.putString("Net", devPath);
//		}else{
//			list = list + "," + devPath;
//			editor.putString("Net", list);
//		}
//		editor.commit();
//	}
//	
//	public void delNetDevice(String devPath){
//		if(devPath == null){
//			return;
//		}
//		ArrayList<String> list = getNetDeviceList();
//		if(list != null && list.size() > 0) {
//			list.remove(devPath);
//			String st = null;
//			for(int i = 0; i < list.size(); i++){
//				if(st == null){
//					st = list.get(i);
//				}else{
//					st = st + "," + list.get(i);
//				}
//			}
//			SharedPreferences pref = mContext.getSharedPreferences("Device", 0);
//			SharedPreferences.Editor editor = pref.edit();
//			editor.putString("Net", st);
//			editor.commit();
//		}
//	}
//	
//	public boolean isNetStoragePath(String path){
//		return false;
//	}
}
