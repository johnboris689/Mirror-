cmake_minimum_required(VERSION 3.22.1)
project(mirror_httrack LANGUAGES C)

set(HTTRACK_ROOT "${CMAKE_CURRENT_LIST_DIR}/../../../../..")
set(COUCAL_DIR "${HTTRACK_ROOT}/src/coucal")
if(NOT EXISTS "${COUCAL_DIR}/coucal.c" OR NOT EXISTS "${COUCAL_DIR}/coucal.h")
  message(FATAL_ERROR "Missing required src/coucal submodule. Clone with --recurse-submodules or run: git submodule update --init --recursive")
endif()

set(MIRROR_OPENSSL_ROOT "" CACHE PATH "Prefix of an Android-ABI OpenSSL build (include/ and lib/)")
if(NOT MIRROR_OPENSSL_ROOT)
  message(FATAL_ERROR "HTTrack HTTPS support requires an Android OpenSSL build. Set -DMIRROR_OPENSSL_ROOT=<OpenSSL Android install prefix>.")
endif()
set(OPENSSL_ABI_ROOT "${MIRROR_OPENSSL_ROOT}/${ANDROID_ABI}")
find_path(OPENSSL_INCLUDE_DIR openssl/ssl.h PATHS "${OPENSSL_ABI_ROOT}/include" NO_DEFAULT_PATH REQUIRED)
find_library(OPENSSL_SSL_LIBRARY ssl PATHS "${OPENSSL_ABI_ROOT}/lib" NO_DEFAULT_PATH REQUIRED)
find_library(OPENSSL_CRYPTO_LIBRARY crypto PATHS "${OPENSSL_ABI_ROOT}/lib" NO_DEFAULT_PATH REQUIRED)
find_library(Z_LIBRARY z REQUIRED)
find_library(LOG_LIBRARY log REQUIRED)

set(GENERATED_DIR "${CMAKE_CURRENT_BINARY_DIR}/generated")
file(MAKE_DIRECTORY "${GENERATED_DIR}")
configure_file(config.h.in "${GENERATED_DIR}/config.h")

set(HTTRACK_SOURCES
  htscore.c htsparse.c htsback.c htscache.c htscatchurl.c htsfetch.c
  htspsl.c htspsl_data.c htsfilters.c htsftp.c htshash.c
  htscmdline.c htshelp.c htslib.c htslines.c htsurlport.c htsdate.c
  htscoremain.c htsname.c htsrobots.c htstools.c htsrandom.c htswizard.c
  htsalias.c htsthread.c htsindex.c htsbauth.c htsbundle.c htscrashtest.c
  htsmd5.c htsnet.c htscodec.c htswarc.c htschanges.c htssinglefile.c
  htssitemap.c htsproxy.c htszlib.c htswrap.c htsconcat.c htsmodules.c
  htscharset.c punycode.c htsencoding.c htsescape.c htssniff.c md5.c
  minizip/ioapi.c minizip/mztools.c minizip/unzip.c minizip/zip.c
)
list(TRANSFORM HTTRACK_SOURCES PREPEND "${HTTRACK_ROOT}/src/")
list(APPEND HTTRACK_SOURCES "${COUCAL_DIR}/coucal.c")

add_library(httrack_engine SHARED ${HTTRACK_SOURCES} mirror_jni.c)
target_include_directories(httrack_engine PRIVATE
  "${GENERATED_DIR}"
  "${HTTRACK_ROOT}/src"
  "${COUCAL_DIR}"
  "${OPENSSL_INCLUDE_DIR}"
)
target_compile_definitions(httrack_engine PRIVATE
  HTS_INTERNAL_BUILD=1 HAVE_CONFIG_H=1
  HTS_USEOPENSSL=1 HTS_USEZLIB=1 HTS_USEBROTLI=0 HTS_USEZSTD=0
  HTS_USEICONV=0 HTS_NO_SELFTEST=1
  HTS_INET6=1 HTS_LFS=1 HAVE_VISIBILITY=1
  PREFIX="/data/user/0/com.mirror.app/files"
  SYSCONFDIR="/data/user/0/com.mirror.app/files"
  BINDIR="/data/user/0/com.mirror.app/files"
  DATADIR="/data/user/0/com.mirror.app/files"
  LIBDIR="/data/user/0/com.mirror.app/files"
)
target_compile_options(httrack_engine PRIVATE -fvisibility=hidden -Wno-deprecated-declarations)
target_link_libraries(httrack_engine PRIVATE
  "${OPENSSL_SSL_LIBRARY}" "${OPENSSL_CRYPTO_LIBRARY}" "${Z_LIBRARY}"
  "${LOG_LIBRARY}" android
)
