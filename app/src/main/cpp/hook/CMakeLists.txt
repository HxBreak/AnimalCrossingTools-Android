# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(DobbyHome ${PROJECT_SOURCE_DIR}/Dobby)

include_directories(
        ${DobbyHome}/include
        ${DobbyHome}/source
        ${DobbyHome}/builtin-plugin
        ${DobbyHome}/builtin-plugin/AndroidRestriction
        ${DobbyHome}/builtin-plugin/SymbolResolver
        ${DobbyHome}/external/logging
)

macro(SET_OPTION option value)
    set(${option} ${value} CACHE INTERNAL "" FORCE)
endmacro()

SET_OPTION(DOBBY_DEBUG ON)
SET_OPTION(GENERATE_SHARED OFF)
SET_OPTION(DynamicBinaryInstrument ON)
SET_OPTION(NearBranch ON)
SET_OPTION(Plugin.SymbolResolver ON)

set(CMAKE_VERBOSE_MAKEFILE on)

set("dobby_src_list"
        ${DobbyHome}/builtin-plugin/AndroidRestriction/android_restriction_demo.cc
        ${DobbyHome}/builtin-plugin/ApplicationEventMonitor/posix_file_descriptor_operation_monitor.cc
        ${DobbyHome}/builtin-plugin/ApplicationEventMonitor/dynamic_loader_monitor.cc)

if (CMAKE_SYSTEM_PROCESSOR STREQUAL "armv7-a" OR CMAKE_SYSTEM_PROCESSOR STREQUAL "aarch64" OR CMAKE_SYSTEM_PROCESSOR STREQUAL "x86_64")
    set(dobby_bin dobby)
    set(dobby_src ${dobby_src_list})
    add_subdirectory(${DobbyHome} dobby)
endif ()

find_library(log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

add_library( # Sets the name of the library.
        hook
        # Sets the library as a shared library.
        SHARED
        ${dobby_src}
        native-lib.cpp)
message("src: ${dobby_bin}, home: ${DobbyHome}")
target_link_libraries( # Specifies the target library.
        hook
        ${dobby_bin}
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})