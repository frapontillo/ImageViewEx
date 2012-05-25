ImageViewEx
===========

Extension of Android's ImageView that supports animated GIFs and includes a better density management.


###Author
Francesco Pontillo and Sebastiano Poggi

###Description
The **ImageViewEx** is an extension of the standard Android `ImageView` that fills in one of the biggest gaps in the standard `ImageView`: displaying **animated GIFs**.
The Android Framework `Drawable` class, in fact, only supports static GIFs. This View is able to receive a raw image in `byte[]` form, bypassing the standard `Drawable`s handling and thus providing broader support.

The `ImageViewEx` also allows you to specify what has to be considered the default image density when loading images from raw data. Android defaults to considering `mdpi` as the baseline, but using `setInDensity` you can choose to change it to whatever value you like (we suggest to stick to standard abstracted density buckets like `hdpi` thou).

###Known issues and workarounds
It internally uses an old Android Framework class, `Movie`, to parse animated GIFs. This ensures fast execution, since the `Movie` class internally relies on native code. Due to `Movie` being a legacy class, though, there are a few quirks.
Firstly, you can't have `Movie` working on an hardware-accelerated canvas in Honeycomb and newer versions of Android. The `ImageViewEx` thus automatically disables hardware acceleration on itself when it has to display a GIF image. One side effect is that hardware acceleration is "lost" forever on the View once turned off, so if you reuse the `ImageViewEx` and at some point you assign a GIF image to it, from that point onwards it won't be hardware accelerated anymore. That's a limitation Android itself imposes, so there's not much we can do about that. On the bright side, this only affects cases where hardware acceleration is available; even when software rendering is active, there's not a big performance hit thou.
The second issue is that `Movie` has serious issues on some emulator instances and some retail devices. This is most likely due to some broken code down at native (maybe in Skia) or video driver level. So not much we can do on this one either. On the bright side, we've provided a workaround, that is setting `setCanAlwaysAnimate(true)` on phones known to cause issues. You will lose animation support, but you don't need to get crazy trying to handle several layouts, some using `ImageView`s and some using `ImageViewEx`es.




## License

Released under the [MIT license](http://www.opensource.org/licenses/mit-license.php).

Copyright (c) 2011-2 Francesco Pontillo and Sebastiano Poggi

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.