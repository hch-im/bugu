APP_ABI := armeabi
APP_MODULES      := bugu_lib

#generate jni interface
# $cd ./bin/classes
# $javah -jni edu.wayne.cs.bugu.util.NativeLib
# $mv ./edu_wayne_cs_bugu_util_NativeLib.h ../../jni/edu_wayne_cs_bugu_util_NativeLib.h

# Build Tips
# 1. change the project target as to any one that is different with the hacked one.
# 2. Build the library: 
#    $ cd <bugu project dir> 
#    $ <ndk dir>/ndk-build
# 3. the lib file will be in the libs/<APP_ABI> directory.

