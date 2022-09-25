# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

project(libvocalcords)

set(CMAKE_CXX_STANDARD 14)


#set(CMAKE_EXE_LINKER_FLAGS "-static-libgcc -static-libstdc++ -static")

file(GLOB LIBGSM_H src/main/cpp/inc/*.h)
file(GLOB LIBGSM_C src/main/cpp/src/*.c)
add_definitions(-DWAV49=Y)

file(GLOB_RECURSE Q_HEADERS src/main/cpp/q_lib/include/**/*.hpp)
file(GLOB_RECURSE DSP_CPP_FILTERS src/main/cpp/dsp_cpp_filters/lib/*.h)

source_group("Header Files\\q"
        FILES
        ${Q_HEADERS}
        )

set(SRC_HEADERS
        ${LIBGSM_H}
        ${Q_HEADERS}
        ${DSP_CPP_FILTERS}
        src/main/cpp/library.h
        src/main/cpp/aq_metallists_freundschaft_vocoder_GSMNativeVocoder.h
        )
set(SRC_FILES
        ${LIBGSM_C}
        src/main/cpp/library.cpp
        src/main/cpp/neurocortex.cpp
        )

include_directories(src/main/cpp/inc src/main/cpp/q_lib/include)


add_library( # Specifies the name of the library.
             freundschaft

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s)
            ${SRC_FILES} ${SRC_HEADERS}
        )
target_link_libraries(freundschaft log)