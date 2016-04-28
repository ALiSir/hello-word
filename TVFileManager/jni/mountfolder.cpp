#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

#include <jni.h>
#include <string.h>

#include <string>
#include <vector>

#include <android/log.h>

#include <sys/socket.h>
#include <arpa/inet.h>

#include <cutils/sockets.h>

#define TAG "mountfolder-jni"
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

extern "C" {

jstring Java_com_powerleader_sambasetting_SambaShareSetting_GetMountHome
  (JNIEnv *env, jobject thiz, jint reqUserId, jint resUserId)
{
	char mount_point[1024];
	//sprintf(mount_point, "/storage/emulated/%d/private_share/%d", reqUserId, resUserId);
	sprintf(mount_point, "/storage/emulated/%d/private_share", reqUserId);
	return env->NewStringUTF(mount_point);
}
jint Java_com_powerleader_sambasetting_SambaShareSetting_FckD
  (JNIEnv *env, jobject thiz)
{
//	std::string result;
//	ShellCmdAgent(result, "chmod 0771 /data/pool/android_userspace/0");
//	ShellCmdAgent(result, "chown root:sdcard_r /data/pool/android_userspace/0");
	return (jint) 0;
}

}
