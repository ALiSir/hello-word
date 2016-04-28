/*
 * sambasetting.cpp
 *
 *  Created on: 2015-11-5
 *      Author: cyril
 */


/**/
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

#include <jni.h>
#include <string.h>

#include <string>
#include <vector>
#include <map>

#include <android/log.h>

#include <sys/socket.h>
#include <arpa/inet.h>

#include <cutils/sockets.h>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>
#include <fcntl.h>
#include <stdarg.h>
#include <dirent.h>
#include <limits.h>
#include <errno.h>

#include <sys/socket.h>
#include <sys/un.h>
#include <sys/select.h>
#include <sys/types.h>
#include <netinet/in.h>


#define D_SOCKET_PORT		7678

#define D_ANDROID_STORAGE			 "/mnt/shell/emulated"
#define D_ANDROID_USER_SPACE		 "/mnt/shell/emulated/%d"
#define D_ANDROID_USER_HOME			 "/mnt/shell/emulated/%d/%s"
#define D_ANDROID_PUBLIC_SPACE		 "/mnt/shell/emulated/%d/public_share/%s"
//#define D_PUBLIC_SHARE_STORAGE_PATH "/mnt/shell/emulated/public_share"
#define D_PUBLIC_SHARE_STORAGE_PATH "/data/pool/android_userspace/public_share"

#define D_PRIVATE_SAMBA_PARAM_BUF		"\n\tcomment = %s\n\
\tbrowseable = yes\n\
\tpath = %s\n\
\tguest ok = no\n\
\tread only = no\n\
\tvalid users = %s\n\
\twriteable = yes\n\
\n\n"

#define D_PUBLIC_SAMBA_PARAM_BUF		"\n\tcomment = %s\n\
\tbrowseable = yes\n\
\tpath = %s\n\
\tguest ok = yes\n\
\tread only = no\n\
\twriteable = yes\n\
\n\n"


#define TAG "sambasetting-jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, TAG, __VA_ARGS__)

#define HOMESERVER "homeserver.sock"


int ShellCmdAgent(std::string &result, const std::string& shellCmd)
{
	int client_fd = 0;
    int socket_type = SOCK_STREAM;

    client_fd = socket_local_client(HOMESERVER, ANDROID_SOCKET_NAMESPACE_ABSTRACT, socket_type);
    if(client_fd < 0)
    {
    	LOGI("socket_local_server err: %s %s %d \n", __FILE__, __func__, __LINE__);
        return -1;
    }

    char cmd[2048];
    char retBuf[4*1024];
    strncpy(cmd, shellCmd.c_str(), sizeof(cmd)-1);

	if( write(client_fd, cmd, sizeof(cmd)) < 0)
	{
		LOGI("\t socket_local_client write err: %s \n", cmd);
		close(client_fd);
		return -2;
	}


	fd_set fds;
	int maxFd = client_fd + 1;
	FD_ZERO(&fds);
	FD_SET(client_fd, &fds);
	struct timeval tv={3,0};

	int ret = select(maxFd, &fds, NULL, NULL, &tv);
	if (ret < 0 || !FD_ISSET(client_fd, &fds))
	{
		return -4;
	}


	if (read(client_fd, retBuf, sizeof(retBuf)) < 0)
	{
		LOGI("\t socket_local_client read err: %s \n", retBuf);
		close(client_fd);
		return -3;
	}

	result = retBuf;

	close(client_fd);

	return 0;
}

static char *smb_config_path = "/data/samba/etc/smb.conf";

typedef struct _SmbConfObj
{
	std::string shareName;
	std::string shareParamBuf;
} SmbConfObj;

typedef struct _SmbConfParam
{
	std::string shareName;
	std::string path;
	std::string validUsers;
	bool isPublicShare;
} SmbConfParam;

static bool IsSambaConfFileExist()
{
	FILE *fp = fopen(smb_config_path, "r");
	if (fp == NULL)
	{
		return false;
	}

	fclose(fp);
	return true;
}

static int ReadSambaConfFile(std::string &smbConfBuf)
{
	char *copy_of_read_operation = (char *)"/data/smb.conf.read_copy";

	std::string result;
	std::string cmd = (std::string)"cp " + smb_config_path + " " + copy_of_read_operation;
	ShellCmdAgent(result, cmd);

	cmd =  (std::string)"chmod 0777 " + copy_of_read_operation;
	ShellCmdAgent(result, cmd);

/*-------- read a copy of samba configuration -------*/

//	FILE *fp = fopen(smb_config_path, "r");
	FILE *fp = fopen(copy_of_read_operation, "r");
	if (fp == NULL)
	{
		return -1;
	}

	unsigned int bufferSize = 1024*1024;
	char *buffer = (char *)malloc(bufferSize);
	memset(buffer, 0, bufferSize);

	unsigned int readSize = fread(buffer, 1, bufferSize, fp);
	if (readSize < 0)
	{
		return -2;
	}

	smbConfBuf = buffer;

	free(buffer);
	fclose(fp);

	/*-------- end -------*/


	cmd =  (std::string)"rm -f " + copy_of_read_operation;
	ShellCmdAgent(result, cmd);

	return 0;
}

static int ReadSambaConf(std::vector<SmbConfObj> &shareConfList)
{
	std::string smbConfBuf;
	int errNo = ReadSambaConfFile(smbConfBuf);
	if (errNo != 0)
	{
		return -1;
	}

	SmbConfObj smbConfObj;
	std::string::size_type startPos = 0;
	std::string::size_type endPos = 0;

	std::string key = "\n[";
	while (1)
	{
		if (startPos == 0 && smbConfBuf[0] == '[')
		{
			startPos = startPos + 1;
		}
		else
		{
			endPos = smbConfBuf.find(key, startPos);
			if (endPos == std::string::npos)
			{
				break;
			}
			startPos = endPos + key.size();
		}

		endPos = smbConfBuf.find(']', startPos);
		if (endPos == std::string::npos)
		{
			break;
		}

		smbConfObj.shareName = smbConfBuf.substr(startPos, endPos - startPos);

		startPos = endPos + 1;
		endPos = smbConfBuf.find(key, startPos);
		if (endPos == std::string::npos)
		{
			smbConfObj.shareParamBuf = smbConfBuf.substr(startPos, smbConfBuf.length() - 1 - startPos);
		}
		else
		{
			smbConfObj.shareParamBuf = smbConfBuf.substr(startPos, endPos - startPos);
		}

		shareConfList.push_back(smbConfObj);

		startPos = endPos;
		continue;
	}

	return 0;
}

static int WriteSambaConf(const std::vector<SmbConfObj> &shareConfList)
{
	char *copy_of_write_operation = (char *)"/data/smb.conf.write_copy";

	std::string result;
	std::string cmd =  (std::string)"touch " + copy_of_write_operation;
	ShellCmdAgent(result, cmd);

	cmd =  (std::string)"chmod 0777 " + copy_of_write_operation;
	ShellCmdAgent(result, cmd);

	/*-------- write a copy of samba configuration -------*/

	std::string smbConfBuf;

	for (unsigned int i=0; i < shareConfList.size(); i++)
	{
		smbConfBuf += "\n[" + shareConfList[i].shareName + "]" + shareConfList[i].shareParamBuf;
	}

//	FILE *fp = fopen(smb_config_path, "w+");
	FILE *fp = fopen(copy_of_write_operation, "w+");
	if (fp == NULL)
	{
		return -1;
	}

	fwrite(smbConfBuf.c_str(), 1, smbConfBuf.size(), fp);

	fclose(fp);

	/*-------- end -------*/

	cmd =  (std::string)"cp "+ copy_of_write_operation + " "  + smb_config_path ;
	ShellCmdAgent(result, cmd);

	cmd =  (std::string)"rm -f "+ copy_of_write_operation;
	ShellCmdAgent(result, cmd);

	return 0;
}

static int AddSambaConf(const SmbConfObj &smbConfObj)
{
	std::vector<SmbConfObj> shareConfList;
	int errNo = ReadSambaConf(shareConfList);
	//LOGE("shareConfList size = %d", shareConfList.size());
	if (errNo != 0)
	{
		return -1;
	}

	shareConfList.push_back(smbConfObj);
	int ret = WriteSambaConf(shareConfList);

	return ret;
}

static int DeleteSambaConf(const std::string shareName)
{
	std::vector<SmbConfObj> shareConfList;
	int errNo = ReadSambaConf(shareConfList);
	if (errNo != 0)
	{
		return -1;
	}

	for (std::vector<SmbConfObj>::iterator i=shareConfList.begin(); i != shareConfList.end(); i++)
	{
		if (strcasecmp(i->shareName.c_str(), shareName.c_str()) == 0)
		{
			shareConfList.erase(i);
			break;
		}
	}

	int ret = WriteSambaConf(shareConfList);

	return 0;
}

static int SplitPath(const std::string& path, std::string &dir, std::string &fname)
{
	std::string::size_type pos = path.find_last_of('/');
	if (pos == std::string::npos)
	{
		fname = path;
	}
	else
	{
		dir = path.substr(0, pos);
		fname = path.substr(pos + 1);
	}

	return 0;
}


static std::vector<std::string> ReadLine(std::string buffer, bool lineEndStay=true)
{
	std::vector<std::string> lineList;
	std::string::size_type startPos = 0;
	std::string::size_type endPos = std::string::npos;
	std::string lineEnd = "\n";
	while (1)
	{
		endPos = buffer.find(lineEnd, startPos);
		if (endPos == std::string::npos)
		{
			if (startPos != buffer.size())
			{
				lineList.push_back(buffer.substr(startPos /*,  buffer.size() - startPos*/));
			}
			break;
		}

		if (lineEndStay)
		{
			lineList.push_back(buffer.substr(startPos, (endPos+1) - startPos));
		}
		else
		{
			lineList.push_back(buffer.substr(startPos, endPos - startPos));
		}

		startPos = endPos + 1;
	}

	return lineList;
}

std::string DelSideSpace(const std::string& sourceStr)
{
	std::string::size_type frontPos = 0;
	for (std::string::size_type index = 0; index != sourceStr.size(); index++)
	{
		if (sourceStr[index] != ' ' && sourceStr[index] != '\t')
		{
			frontPos = index;
			break;
		}
	}

	std::string::size_type backPos = 0;
	for (std::string::size_type index = sourceStr.size() - 1; index >= 0; index--)
	{
		if (sourceStr[index] != ' ' && sourceStr[index] != '\t')
		{
			backPos = index;
			break;
		}
	}

	return sourceStr.substr(frontPos,backPos + 1 - frontPos);
}

std::map<std::string, std::string> ParseSmbConfParam(const std::string& confParamBuf)
{
	std::vector<std::string> lineList = ReadLine(confParamBuf, false);

	std::map<std::string, std::string> confMap;
	for (unsigned int i=0; i < lineList.size(); i++)
	{
		std::string lineStr = lineList[i];
		if (lineStr[0] == ';' || lineStr[0] == '#')
		{
			continue;
		}
		std::string::size_type charPos = lineStr.find('=');
		if (charPos == std::string::npos)
		{
			continue;
		}

		std::string confField = DelSideSpace(lineStr.substr(0,charPos));
		std::string confValue  = DelSideSpace(lineStr.substr(charPos+1, lineStr.length()-charPos-1));
		confMap[confField] = confValue;
	}

	return confMap;
}

static std::vector<SmbConfParam> GetShareList()
{
	std::vector<SmbConfObj> smbConfList;
	ReadSambaConf(smbConfList);

	std::vector<SmbConfParam> smbConfParamList;
	//LOGE("smb conf size = %d", smbConfList.size());
	for (unsigned int i=0; i < smbConfList.size(); i++)
	{
		if (strcasecmp(smbConfList[i].shareName.c_str(), "global") == 0)
		{
			continue;
		}
		std::map<std::string, std::string> paramMap = ParseSmbConfParam(smbConfList[i].shareParamBuf);

		SmbConfParam smbConfParam;
		smbConfParam.shareName = smbConfList[i].shareName;
		smbConfParam.path = paramMap["path"];
		//LOGE("paramMap[path] = %s", paramMap["path"].c_str());
		if (paramMap.find("guest ok") == paramMap.end())
		{
			smbConfParam.isPublicShare = false;
		}
		else
		{
			std::string valueOfGuestOk = paramMap["guest ok"];
			if (strcasecmp(valueOfGuestOk.c_str(), "yes") == 0)
			{
				smbConfParam.isPublicShare = true;
			}
			else
			{
				smbConfParam.isPublicShare = false;
				smbConfParam.validUsers = paramMap["valid users"];
			}
		}

		smbConfParamList.push_back(smbConfParam);
	}

	return smbConfParamList;
}

//using namespace std;
#define D_SAMBA_FILE_NOT_EXIST 		-101

/*Implementation Of SambaUserSetting*/
extern "C" {
jint
Java_com_powerleader_sambasetting_SambaUserSetting_AddSambaUser
  (JNIEnv *env, jobject thiz, jstring user, jint userId, jstring password)
{
	const char *username = (env)->GetStringUTFChars(user, NULL);
	const char *userpass = (env)->GetStringUTFChars(password, NULL);

	char cmdBuf[1024];
	sprintf(cmdBuf, "(echo \"%s\"; echo \"%s\") | smbpasswd -s -a %s", userpass, userpass, username);
	std::string result;
	int ret = ShellCmdAgent(result, cmdBuf);
	LOGE("[test]result = %s, cmdBuf = %s, ret = %d", result.c_str(), cmdBuf, ret);

//	char sharedir[1024];
//	sprintf(sharedir, D_ANDROID_USER_SPACE, (int)userId);
//
//	char sharename[1024];
//	sprintf(sharename, "%s_share_dir", username);
//
//	char shareparambuf[1024];
//	sprintf(shareparambuf, D_PRIVATE_SAMBA_PARAM_BUF, sharename, sharedir, username);
//	SmbConfObj priSmbConf;
//	priSmbConf.shareName = sharename;
//	priSmbConf.shareParamBuf = shareparambuf;
//
//	AddSambaConf(priSmbConf);

	(env)->ReleaseStringUTFChars(user, username);
	(env)->ReleaseStringUTFChars(password, userpass);

	return (jint)0;
}


jint
Java_com_powerleader_sambasetting_SambaUserSetting_DeleteSambaUser
  (JNIEnv *env, jobject thiz, jstring user)
{
	const char *username = (env)->GetStringUTFChars(user, NULL);

	char cmdBuf[1024];
	sprintf(cmdBuf, "smbpasswd -x %s", username);
	std::string result;
	ShellCmdAgent(result, cmdBuf);

//	char sharename[1024];
//	sprintf(sharename, "%s_share_dir", username);
//	DeleteSambaConf(sharename);
	/*char sharedir[1024];
		sprintf(sharedir, D_ANDROID_USER_HOME, (int)userId, sharename);

		std::string cmd = (std::string)"rm -rf "  + sharedir;
		std::string result;
		ShellCmdAgent(result, cmd.c_str());

		int ret = DeleteSambaConf(sharename);*/

	(env)->ReleaseStringUTFChars(user, username);

	return (jint)0;
}

jint
Java_com_powerleader_sambasetting_SambaUserSetting_EnableSambaUser
  (JNIEnv *env, jobject thiz, jstring user)
{
	const char *username = (env)->GetStringUTFChars(user, NULL);

	char cmdBuf[1024];
	sprintf(cmdBuf, "smbpasswd -e %s", username);
	std::string result;
	ShellCmdAgent(result, cmdBuf);

	(env)->ReleaseStringUTFChars(user, username);

	return (jint)0;
}

jint
Java_com_powerleader_sambasetting_SambaUserSetting_DisableSambaUser
  (JNIEnv *env, jobject thiz, jstring user)
{
	const char *username = (env)->GetStringUTFChars(user, NULL);

	char cmdBuf[1024];
	sprintf(cmdBuf, "smbpasswd -d %s", username);
	std::string result;
	ShellCmdAgent(result, cmdBuf);

	(env)->ReleaseStringUTFChars(user, username);

	return (jint)0;
}

jint
Java_com_powerleader_sambasetting_SambaUserSetting_ChangeSambaUserPassword
  (JNIEnv *env, jobject thiz, jstring user, jstring password)
{
	const char *username = (env)->GetStringUTFChars(user, NULL);
	const char *userpass = (env)->GetStringUTFChars(password, NULL);

	char cmdBuf[1024];
	sprintf(cmdBuf, "(echo \"%s\"; echo \"%s\") | smbpasswd -s -a %s", userpass, userpass, username);
	std::string result;
	ShellCmdAgent(result, cmdBuf);

	(env)->ReleaseStringUTFChars(user, username);
	(env)->ReleaseStringUTFChars(password, userpass);

	return (jint)0;
}

jint
Java_com_powerleader_sambasetting_SambaUserSetting_CreateUserHome
  (JNIEnv *env, jobject thiz, jint userId)
{
	char cmdBuf[1024];
	std::string result;
	sprintf(cmdBuf, "mkdir -p %s/%d/%s", D_ANDROID_STORAGE, userId, "Alarms");
	ShellCmdAgent(result, cmdBuf);
	sprintf(cmdBuf, "mkdir -p %s/%d/%s", D_ANDROID_STORAGE, userId, "DCIM");
	ShellCmdAgent(result, cmdBuf);
	sprintf(cmdBuf, "mkdir -p %s/%d/%s", D_ANDROID_STORAGE, userId, "Download");
	ShellCmdAgent(result, cmdBuf);
	sprintf(cmdBuf, "mkdir -p %s/%d/%s", D_ANDROID_STORAGE, userId, "Movies");
	ShellCmdAgent(result, cmdBuf);
	sprintf(cmdBuf, "mkdir -p %s/%d/%s", D_ANDROID_STORAGE, userId, "Music");
	ShellCmdAgent(result, cmdBuf);
	sprintf(cmdBuf, "mkdir -p %s/%d/%s", D_ANDROID_STORAGE, userId, "Notifications");
	ShellCmdAgent(result, cmdBuf);
	sprintf(cmdBuf, "mkdir -p %s/%d/%s", D_ANDROID_STORAGE, userId, "Pictures");
	ShellCmdAgent(result, cmdBuf);
	sprintf(cmdBuf, "mkdir -p %s/%d/%s", D_ANDROID_STORAGE, userId, "Podcasts");
	ShellCmdAgent(result, cmdBuf);
	sprintf(cmdBuf, "mkdir -p %s/%d/%s", D_ANDROID_STORAGE, userId, "Ringtones");
	ShellCmdAgent(result, cmdBuf);
	return (jint)0;
}

/*Implementation Of SambaShareSetting*/
jint
Java_com_powerleader_sambasetting_SambaShareSetting_CreatePrivateShare
  (JNIEnv *env, jobject thiz, jstring user, jint userId, jstring shareName)
{
	const char *username = (env)->GetStringUTFChars(user, NULL);
	const char *sharename = (env)->GetStringUTFChars(shareName, NULL);

	char sharedir[1024];
	sprintf(sharedir, D_ANDROID_USER_HOME, (int)userId, sharename);

	std::string cmd = (std::string)"mkdir "  + sharedir;
	std::string result;
	ShellCmdAgent(result, cmd.c_str());

	char shareparambuf[1024];
	sprintf(shareparambuf, D_PRIVATE_SAMBA_PARAM_BUF, sharename, sharedir, username);
	SmbConfObj priSmbConf;
	priSmbConf.shareName = sharename;
	priSmbConf.shareParamBuf = shareparambuf;

	int ret = AddSambaConf(priSmbConf);

	(env)->ReleaseStringUTFChars(user, username);
	(env)->ReleaseStringUTFChars(shareName, sharename);

	return (jint)ret;
}

jint
Java_com_powerleader_sambasetting_SambaShareSetting_CreatePublicShare
 (JNIEnv *env, jobject thiz, jstring user, jint userId, jstring shareName)
{
	const char *username = (env)->GetStringUTFChars(user, NULL);
	const char *sharename = (env)->GetStringUTFChars(shareName, NULL);

	char sharedir[1024];
	sprintf(sharedir, D_ANDROID_PUBLIC_SPACE, userId, sharename);

	std::string cmd = (std::string)"mkdir -p "  + sharedir;
	std::string result;
	ShellCmdAgent(result, cmd.c_str());

	char shareparambuf[1024];
	sprintf(shareparambuf, D_PUBLIC_SAMBA_PARAM_BUF, sharename, sharedir);
	SmbConfObj pubSmbConf;
	pubSmbConf.shareName = sharename;
	pubSmbConf.shareParamBuf = shareparambuf;

	AddSambaConf(pubSmbConf);

	(env)->ReleaseStringUTFChars(user, username);
	(env)->ReleaseStringUTFChars(shareName, sharename);

	return (jint)0;
}

jint
Java_com_powerleader_sambasetting_SambaShareSetting_DeletePrivateShare
 (JNIEnv *env, jobject thiz, jstring user, jint userId, jstring shareName)
{

	const char *username = (env)->GetStringUTFChars(user, NULL);
	const char *sharename = (env)->GetStringUTFChars(shareName, NULL);

	char sharedir[1024];
	sprintf(sharedir, D_ANDROID_USER_HOME, (int)userId, sharename);

	std::string cmd = (std::string)"rm -rf "  + sharedir;
	std::string result;
	ShellCmdAgent(result, cmd.c_str());

	int ret = DeleteSambaConf(sharename);

	(env)->ReleaseStringUTFChars(user, username);
	(env)->ReleaseStringUTFChars(shareName, sharename);

	return (jint)0;
}

jint
Java_com_powerleader_sambasetting_SambaShareSetting_DeletePublicShare
(JNIEnv *env, jobject thiz, jstring user, jint userId, jstring shareName)
{

	const char *username = (env)->GetStringUTFChars(user, NULL);
	const char *sharename = (env)->GetStringUTFChars(shareName, NULL);

	char sharedir[1024];
	sprintf(sharedir, D_ANDROID_PUBLIC_SPACE, userId, sharename);

	std::string cmd = (std::string)"rm -rf "  + sharedir;
	std::string result;
	ShellCmdAgent(result, cmd.c_str());

	int ret = DeleteSambaConf(sharename);

	(env)->ReleaseStringUTFChars(user, username);
	(env)->ReleaseStringUTFChars(shareName, sharename);

	return (jint)0;
}


/*
 * Class:     com_powerleader_sambasetting_SambaShareSetting
 * Method:    MountFolder
 * Signature: (IILjava/lang/String;)Ljava/lang/String;
 */
jstring Java_com_powerleader_sambasetting_SambaShareSetting_MountFolder
  (JNIEnv *env, jobject thiz, jint reqUserId, jint resUserId, jstring path)
{
	const char *local_path = (env)->GetStringUTFChars(path, NULL);

	std::string dir;
	std::string fname;
	SplitPath(local_path, dir, fname);

	char mount_point[1024];
	sprintf(mount_point, "/mnt/shell/emulated/%d/private_share/%s", reqUserId, fname.c_str());

	std::string result;
	char mkdir_cmd[1024];
	sprintf(mkdir_cmd, "mkdir /mnt/shell/emulated/%d/private_share", reqUserId);
	ShellCmdAgent(result, mkdir_cmd);

//	sprintf(mkdir_cmd, "mkdir /mnt/shell/emulated/%d/private_share/%s", reqUserId, fname);
	sprintf(mkdir_cmd, "mkdir %s", mount_point);
	ShellCmdAgent(result, mkdir_cmd);

	char mount_cmd[2048];
	sprintf(mount_cmd, "busybox mount --bind %s %s", local_path, mount_point);
	LOGE("local path = %s", local_path);
	ShellCmdAgent(result, mount_cmd);

	(env)->ReleaseStringUTFChars(path, local_path);

	char android_user_path[1024];
	sprintf(android_user_path, "/storage/emulated/%d/private_share/%s", reqUserId, fname.c_str());
	return env->NewStringUTF(android_user_path);
}

/*
 * Class:     com_powerleader_sambasetting_SambaShareSetting
 * Method:    MountPublicShareToUser
 * Signature: (I)I
 */
jint Java_com_powerleader_sambasetting_SambaShareSetting_MountPublicShareToUser
  (JNIEnv *env, jobject thiz, jint userId)
{
		char mount_point[1024];
		//	sprintf(mount_point, "/mnt/shell/emulated/%d/public_share", userId);
		sprintf(mount_point, "/data/pool/android_userspace/%d/public_share", userId);


		std::string result;
		char mkdir_cmd[1024];
		sprintf(mkdir_cmd, "mkdir %s", mount_point);
		ShellCmdAgent(result, mkdir_cmd);

		char mount_cmd[2048];
		sprintf(mount_cmd, "busybox mount --bind %s %s", D_PUBLIC_SHARE_STORAGE_PATH, mount_point);
		ShellCmdAgent(result, mount_cmd);

		return (jint)0;
}


/*
 * Class:     com_powerleader_sambasetting_SambaShareSetting
 * Method:    UmountPublicShareFromUser
 * Signature: (I)I
 */
jint Java_com_powerleader_sambasetting_SambaShareSetting_UmountPublicShareFromUser
  (JNIEnv *env, jobject thiz, jint userId)
{
	char mount_point[1024];
	//	sprintf(mount_point, "/mnt/shell/emulated/%d/public_share", userId);
	sprintf(mount_point, "/data/pool/android_userspace/%d/public_share", userId);


	std::string result;
	char umount_cmd[2048];
	sprintf(umount_cmd, "busybox umount %s", mount_point);
	ShellCmdAgent(result, umount_cmd);

	return (jint)0;
}

/*
 * Class:     com_powerleader_sambasetting_SambaShareSetting
 * Method:    GetPublicShareList
 * Signature: ()Ljava/util/ArrayList;
 */
JNIEXPORT jobject JNICALL Java_com_powerleader_sambasetting_SambaShareSetting_GetPublicShareList
  (JNIEnv *env, jobject thiz)
{
	std::vector<SmbConfParam> shareList = GetShareList();

	jclass list_cls = env->FindClass("java/util/ArrayList");
	jmethodID list_costruct = env->GetMethodID(list_cls , "<init>", "()V");
	jobject list_obj = env->NewObject(list_cls , list_costruct);
	jmethodID list_add  = env->GetMethodID(list_cls, "add", "(Ljava/lang/Object;)Z");

	jclass eleObj = env->FindClass("com/powerleader/sambasetting/SambaConf");
	jmethodID eleInitM = env->GetMethodID(eleObj, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");

	for (int i=0; i < shareList.size(); i++)
	{
		//LOGE("public shareName = %s", shareList[i].shareName.c_str());
		if (!shareList[i].isPublicShare)
		{
			continue;
		}
		jobject value_shareName = env->NewStringUTF(shareList[i].shareName.c_str());
		jobject value_path = env->NewStringUTF(shareList[i].path.c_str());
		jobject obj = env->NewObject(eleObj, eleInitM,
				value_shareName,
				value_path);
		env->CallBooleanMethod(list_obj, list_add, obj);
	}

	return list_obj;
}

/*
 * Class:     com_powerleader_sambasetting_SambaShareSetting
 * Method:    GetUserPrivateShareList
 * Signature: (Ljava/lang/String;)Ljava/util/ArrayList;
 */
JNIEXPORT jobject JNICALL Java_com_powerleader_sambasetting_SambaShareSetting_GetUserPrivateShareList
  (JNIEnv *env, jobject thiz, jstring userName)
{
	std::vector<SmbConfParam> shareList = GetShareList();

	jclass list_cls = env->FindClass("java/util/ArrayList");
	jmethodID list_costruct = env->GetMethodID(list_cls , "<init>", "()V");
	jobject list_obj = env->NewObject(list_cls , list_costruct);
	jmethodID list_add  = env->GetMethodID(list_cls, "add", "(Ljava/lang/Object;)Z");

	jclass eleObj = env->FindClass("com/powerleader/sambasetting/SambaConf");
	jmethodID eleInitM = env->GetMethodID(eleObj, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");

	const char *local_userName = (env)->GetStringUTFChars(userName, NULL);

	for (int i=0; i < shareList.size(); i++)
	{
		//LOGE("private shareName = %s", shareList[i].shareName.c_str());
		if (!shareList[i].isPublicShare && (shareList[i].validUsers == local_userName))
		{
			jobject value_shareName = env->NewStringUTF(shareList[i].shareName.c_str());
			jobject value_path = env->NewStringUTF(shareList[i].path.c_str());
			jobject obj = env->NewObject(eleObj, eleInitM,
					value_shareName,
					value_path);
			env->CallBooleanMethod(list_obj, list_add, obj);
		}
	}

	return list_obj;
}

}
