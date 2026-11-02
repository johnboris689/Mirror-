#include <jni.h>
#include <stddef.h>
#include "httrack-library.h"

JNIEXPORT jint JNICALL
Java_com_mirror_app_NativeMirror_run(JNIEnv *env, jobject thiz,
                                     jstring url_value,
                                     jstring output_value) {
  (void) thiz;
  if (url_value == NULL || output_value == NULL) {
    return -1000;
  }
  const char *url = (*env)->GetStringUTFChars(env, url_value, NULL);
  if (url == NULL) return -1001;
  const char *output = (*env)->GetStringUTFChars(env, output_value, NULL);
  if (output == NULL) {
    (*env)->ReleaseStringUTFChars(env, url_value, url);
    return -1002;
  }

  /* Use HTTrack's public command-line entry point so URL parsing, filters,
     retries, rewriting, and file storage remain the upstream implementation. */
  char *argv[] = {
      (char *) "mirror", (char *) "-q", (char *) "-O",
      (char *) output, (char *) url
  };
  (void) hts_init();
  const int result = hts_main((int) (sizeof(argv) / sizeof(argv[0])), argv);

  (*env)->ReleaseStringUTFChars(env, output_value, output);
  (*env)->ReleaseStringUTFChars(env, url_value, url);
  return (jint) result;
}
