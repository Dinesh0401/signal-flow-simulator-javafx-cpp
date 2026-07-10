cmake_minimum_required(VERSION 3.15)
project(signalflow_native LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find JNI
find_package(JNI REQUIRED)

# Include directories
include_directories(
    ${JNI_INCLUDE_DIRS}
    ${CMAKE_SOURCE_DIR}/include
)

# Shared library
add_library(signalflow_native SHARED
    src/native_math.cpp
    src/jni_bridge.cpp
)

# Link JNI
target_link_libraries(signalflow_native ${JNI_LIBRARIES})

# Output to project root for easy loading
set_target_properties(signalflow_native PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../
    PREFIX ""
)

# On Windows with MinGW, the output is a .dll
if(WIN32)
    set_target_properties(signalflow_native PROPERTIES
        SUFFIX ".dll"
    )
endif()
