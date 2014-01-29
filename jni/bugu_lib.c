/*
 *   Copyright (C) 2014, Mobile and Internet Systems Laboratory.
 *   All rights reserved.
 *
 *   Authors: Hui Chen (hchen229@gmail.com)
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.
 */
#include <stdlib.h>
#include <linux/tick.h>
#include <unistd.h>

#include "edu_wayne_cs_bugu_util_NativeLib.h"

/*
 * Class:     edu_wayne_cs_bugu_util_NativeLib
 * Method:    getCPUIdleTime
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_edu_wayne_cs_bugu_util_NativeLib_getCPUIdleTime
  (JNIEnv * env, jobject obj, jint cpu_num){
	jint i;
	jlong idletime = 0;
	for( i = 0; i < cpu_num; i++)
	{
		idletime += get_cpu_idle_time_us(i, NULL);
	}

	return idletime;
}

/*
 * Class:     edu_wayne_cs_bugu_util_NativeLib
 * Method:    getCPUIOWaitTime
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_edu_wayne_cs_bugu_util_NativeLib_getCPUIOWaitTime
  (JNIEnv * env, jobject obj, jint cpu_num){
	jint i;
	jlong iowaittime = 0;
	for( i = 0; i < cpu_num; i++)
	{
		iowaittime += get_cpu_iowait_time_us(i, NULL);
	}

	return iowaittime;
}

/*
 * Class:     edu_wayne_cs_bugu_util_NativeLib
 * Method:    getPageSize
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_edu_wayne_cs_bugu_util_NativeLib_getPageSize
  (JNIEnv * env, jobject obj){
	return getpagesize();
}
