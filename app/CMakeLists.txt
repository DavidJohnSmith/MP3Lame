# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             mp3lame

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/bitstream.c
             src/main/cpp/encoder.c
             src/main/cpp/fft.c
             src/main/cpp/gain_analysis.c
             src/main/cpp/id3tag.c
             src/main/cpp/lame.c
             src/main/cpp/mpglib_interface.c
             src/main/cpp/newmdct.c
             src/main/cpp/presets.c
             src/main/cpp/psymodel.c
             src/main/cpp/quantize.c
             src/main/cpp/quantize_pvt.c
             src/main/cpp/reservoir.c
             src/main/cpp/set_get.c
             src/main/cpp/tables.c
             src/main/cpp/takehiro.c
             src/main/cpp/util.c
             src/main/cpp/vbrquantize.c
             src/main/cpp/VbrTag.c
             src/main/cpp/version.c
             src/main/cpp/com_study_mp3lame_MainActivity.c
             )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       mp3lame

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )