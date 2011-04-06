LibListenScreen
===============

A simple audio gui library. LibListenScreen provides an Activity for playing
audio. It's intended to be flexible so it can be used from a variety of
applications.


Cloning
=======

I've included the eclipse project files (.project and .classpath) in the
eclipse_files branch. Eclipse gives me problems using existing projects, so
these files might be helpful to someone.


Usage
=====

Add LibListenScreen as a Library and a Project reference to your Android
project. You should then be able to reference the AudioPlay Activity from your
code. See Launcher.java for example of how to use the Activity.


Testing
=======

You can use Launcher to see how LibListenScreen works, but you'll need to point it at an audio file. Change getUriToPlay() to point to a valid path on your device.

To launch Launcher, you need to make LibListenScreen not a Library project. Open LibListenScreen's project settings and under Android deselect "Is Library".


License
=======

   Copyright 2011 David Briscoe

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


Media playback icons from the Tango Desktop Project
http://tango.freedesktop.org/Tango_Icon_Library

Icons are public domain:

    The Tango base icon theme is released to the Public Domain. The palette is
    in public domain. Developers, feel free to ship it along with your
    application. The icon naming utilities are licensed under the GPL.

    Though the tango-icon-theme package is released to the Public Domain, we
    ask that you still please attribute the Tango Desktop Project, for all the
    hard work we've done. Thanks.


Tango Icons:

    media-playback-pause.png
    media-playback-start.png
    media-skip-backward.png
    media-skip-forward.png
