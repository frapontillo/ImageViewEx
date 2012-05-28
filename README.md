ImageViewEx
===========

Extension of Android's ImageView that supports animated GIFs and includes a better density management.

###Author

**Francesco Pontillo** and **Sebastiano Poggi**

###Description

The **ImageViewEx** is an extension of the standard Android `ImageView` that fills in one of the biggest gaps in the standard `ImageView`: displaying **animated GIFs**.

The Android Framework `Drawable` class, in fact, only supports static GIFs. This View is able to receive a raw image in `byte[]` form, bypassing the standard `Drawable`s handling and thus providing broader support.

The `ImageViewEx` also allows you to specify what has to be considered the default image density when loading images from raw data. Android defaults to considering `mdpi` as the baseline, but using `setInDensity` you can choose to change it to whatever value you like (we suggest to stick to standard abstracted density buckets like `hdpi` thou).

##Import and usage
This library requires Android **API level 8** (Android 2.2) as minimum, and targets the Android **API level 15**.

In your project you need to include:

 * [ignition-support](https://github.com/kaeppler/ignition) library, a nice collection of caching and downloading helpers ImageViewEx (and ImageViewNext) are based on top of
 * [guava] library, required by ignition-support's cache
 
The Eclipse project included specifies this is a library project, although it provides two basic Activities for testing the extended `ImageView`s provided.

For your application, you need to include the permissions specified in the AndroidManifest of the library, which are:

 * android.permission.INTERNET for getting images on the internet
 * android.permission.ACCESS_NETWORK_STATE to monitor the network state
 * android.permission.WRITE_EXTERNAL_STORAGE for making the cache access and write the SD card
 
**Documentation
This is a brief documentation of the classes, methods and views included in this library

###BytesCache
**`BytesCache`** is an extension of ignition's `AbstractCache` and is basically a cache useful for storing any `byte[]` (byte array) and accessing them by a `String` key.

It does nothing more than overriding some methods from `AbstractCache` so that any value is stored as a `byte[]`. Please, refer to ignition's documentation for futher instructions on how to use this simple caching system.

###ImageViewEx
`ImageViewEx` is an extended `ImageView` that supports some additional methods for your every-day life.

####Animated GIF
The best thing about `ImageViewEx` is its automatic handling of animated GIF images starting from a simple `byte[]`.

Simply call `img.setSource(mGIF)` and see your GIF animating. Note that there may be some issues under some conditions (see [Known issues and workarounds](#known-issues-and-workarounds)).

What if you don't know if an image is a GIF or a regular one? No problem, simply call `setSource` and `ImageViewEx` will do the rest, displaying your image as a regular one or an animated GIF when necessary.

Accessory methods are:

 * `void setFramesDuration(int duration)` to set the duration, in milliseconds, of each frame during the GIF animation (it is the refresh period)
 * `void setFPS(float fps)` to set the number of frames per second during the GIF animation
 * `boolean isPlaying()` to know if your GIF is playing
 * `boolean canPlay()` to know if your source set by `setSource` was an animated GIF after all
 * `int getFramesDuration()` to get the frame duration, in milliseconds
 * `float getFPS()` to get the number of frames per second during the GIF animation
 * `void play()` to start the GIF, if it hasn't started yet.
 * `void pause()` to pause the GIF, if it has started
 * `void stop()` to stop playing the GIF, if it has started
 
####Conditional animation
As mentioned earlier, you may not want to animate some GIF under some conditions.

So we've provided you with a conditional method that gets triggered just before each animation begins, `boolean canAnimate()`. This method should be overridden with your custom implementation. By default, it always returns `true`. This method decides whether animations can be started for this instance of `ImageViewEx`.

If you don't want to have another class extending `ImageViewEx` and your `canAnimate()` returns the same value throughout your application, you can use the following

```java
	ImageViewNext.setCanAlwaysAnimate(false);
```

to specify you never want to animate GIFs. If you don't set any value to `setCanAlwaysAnimate`, it defaults to `true`. The actual result in setting the value to `false` is that it will stopping all animation, no matter what `canAnimate()` returns.

You can check the behavior by calling the `static boolean getCanAlwaysAnimate()` method.

####Density Level
You can set a specific density to simulate for every instance of `ImageViewEx` by using the following methods:

 * `static void setClassLevelDensity(int classLevelDensity)` to set a specific density for every image
 * `static void removeClassLevelDensity()` to remove the class-level customization
 * `static boolean isClassLevelDensitySet()`, checks if a class-level density has been set
 * `static int getClassLevelDensity()`, gets the set class-level density, or null if none has been set
 
You can even set a density for just one of your `ImageViewEx`s:
 
 * `void setDensity(int fixedDensity)`, to set the density for a particular instance of `ImageViewEx`
 * `int getDensity()`, gets the set density for a particular instance of `ImageViewEx` (an instance-level density has higher priority over a class-level density)
 * `void dontOverrideDensity()`, restores the regular density of the `ImageViewEx`
 
####Regular behavior
`ImageViewEx` is, after all, a regular `ImageView`, so you can go ahead and use its regular methods:

 * `void setImageResource(int resId)`
 * `void setImageDrawable(Drawable drawable)`
 * `void setImageBitmap(Bitmap bm)`
 * and so on.
 
####Example of use

```java
	// Disables animation, behaving like a regular ImageView,
	// except you can still set byte[] as the source
	ImageViewEx.setCanAlwaysAnimate(false);
	
	// Sets a default density for all of the images in each ImageViewEx.
	ImageViewEx.setClassLevelDensity(DisplayMetrics.DENSITY_MEDIUM);
	
	// Sets a density for the img5 only.
	// Changing the density after an object has been set will
	// do nothing, you will have to re-set the object.
	img1.setInDensity(DisplayMetrics.DENSITY_LOW);
	
	img1.setSource(Converters.assetToByteArray(getAssets(), "image.png"));
    img2.setSource(Converters.assetToByteArray(getAssets(), "animated_image.gif"));
```

###RemoteLoader
The `RemoteLoader` class, together with `RemoteLoaderJob` and `RemoteLoaderHandler` provides a simple yet extensible system to handle data download and caching, by using the ignition-support library and the `BytesCache` provided with this library.
It is an abstraction of the RemoteImageLoader provided in the ignition-support library, edited in such a way that it can handle any kind of file.

It realizes a background loader that downloads any data from a URL, optionally backed by a two-level FIFO cache. A thread from a thread pool will be used to download the data in the background and call the appropriate callbacks of a handler.

###ImageViewNext
`RemoteLoader` is used by `ImageViewNext`, an extension of `ImageViewEx` that handles **downloading, displaying and caching of images (and animated GIFs, of course)**.

`ImageViewNext` extends `ImageViewEx`, thus supporting all of its methods, plus some more.

####Loading and Error Drawables
`ImageViewNext` supports loading and error `Drawable`s:

 * `static void setClassLoadingDrawable(int classLoadingDrawableResId)` sets a `Drawable` for every instance of `ImageViewNext` from the resources to be displayed (and animated, if it's an `AnimatedDrawable`) as soon as the caching tells us there's no in-memory reference for the asked resource. If you have enabled a disk cache, this `Drawable` will be set before fetching the disk memory.
 * `void setLoadingDrawable(Drawable loadingDrawable)` sets a `Drawable` for the current instance of `ImageViewNext` from the resources to be displayed (and animated, if it's an `AnimatedDrawable`) as soon as the caching tells us there's no in-memory reference for the asked resource. If you have enabled a disk cache, this `Drawable` will be set before fetching the disk memory.
 * `static void setClassErrorDrawable(int classErrorDrawableResId)` sets a `Drawable` for every instance of `ImageViewNext` from the resources to be displayed (and animated, if it's an `AnimatedDrawable`) as soon as the RemoteLoader returns an error, not being able to retrieve the image.
 * `void setErrorDrawable(Drawable errorDrawable)` sets a `Drawable` for the current instance of of `ImageViewNext` from the resources to be displayed (and animated, if it's an `AnimatedDrawable`) as soon as the RemoteLoader returns an error, not being able to retrieve the image.
 * `Drawable getLoadingDrawable()` returns the `Drawable` to be displayed while waiting for long-running operations.
 * `Drawable getErrorDrawable()` returns the `Drawable` to be displayed in case of an error.
 * `static void setClassLoader(RemoteLoader classLoader)` sets a class `RemoteLoader` to be used for every request to the caching / networking system.

####Remote loading and caching of images
`ImageViewNext` uses `RemoteLoader` and its handler, `RemoteLoaderHandler` to retrieve images from a cache and the internet and set them to your `ImageViewNext`.

 * `void setLoader(RemoteLoader loader)` sets an instance-level `RemoteLoader` to be used for every request to the caching / networking system for the current `ImageViewNext`.
 * `RemoteLoader getLoader()` returns the `RemoteLoader` to be used for the current instance of `ImageViewNext`.
 * `boolean isLoaderSet()` is `true` if a `RemoteLoader` is set.
 
Ideally, you should instantiate a `RemoteLoader` for your application, and set it to `ImageViewNext`, so it will be used for every request you make.

####Getting images from the Internet
In order to get images from the Internet, simply set a `RemoteLoader` and then call `setUrl(String url)` to start retrieving an image from the internet.

This method returns a `RemoteLoaderHandler`, which provides some useful methods you can override to handle the states of the image retrieval:

 * `void before()` is called before doing anything
 * `void onMemoryHit(byte[] object)` is called as soon as there's a memory hit for the requested URL
 * `void onMemoryMiss()` is called as soon as there's a memory miss for the requested URL
 * `void onDiskHit(byte[] object)` is called as soon as there's a disk hit for the requested URL
 * `void onDiskMiss()` is called as soon as there's a disk miss for the requested URL
 * `void onNetHit(byte[] object)` is called as soon as there's a network hit for the requested URL
 * `void onNetMiss()` is called as soon as there's a network miss for the requested URL
 * `void success(byte[] object)` is automatically called by all of the `onHit`-kind-of methods
 * `void error(Exception e)` is called when an error occurs or the resource can't be found anywhere
 * `void after(byte[] object)` is automatically called after everything is done (good or bad)

You should not worry about setting images, as this is handled by an extension of `RemoteLoaderHandler` (`RemoteLoaderHandlerNext`), which by defaults sets the loading image when there's a memory miss (on `onMemoryMiss()`), an error one in case of error (`error(Exception e)`) and the retrieved image in case of success (`success(byte[] object)`).

You can still make your own class extend `RemoteLoaderHandler` and handle the several exposed states as you like.

####Example of use

```java
	// In your Application class
	private static RemoteLoader loader = new RemoteLoader(getApplicationContext(), true);
	// Sets the class loader
	ImageViewNext.setClassLoader(loader);
	// Sets class-level loading/error Drawables
	ImageViewNext.setClassErrorDrawable(R.drawable.error_thumb);
    ImageViewNext.setClassLoadingDrawable(R.drawable.loading_spinner);
	
	// In some Activity
	if (loader == null) loader = new RemoteLoader(getApplicationContext(), true);
	
	img1.setUrl("http://upload.wikimedia.org/wikipedia/commons/9/91/Cittadimatera1.jpg");
    img2.setUrl("http://upload.wikimedia.org/wikipedia/commons/4/49/Basilicata_Matera1_tango7174.jpg");
```

###Known issues and workarounds

It internally uses an old Android Framework class, `Movie`, to parse animated GIFs. This ensures fast execution, since the `Movie` class internally relies on native code. Due to `Movie` being a legacy class, though, there are a few quirks.

Firstly, you can't have `Movie` working on an hardware-accelerated canvas in Honeycomb and newer versions of Android. The `ImageViewEx` thus automatically disables hardware acceleration on itself when it has to display a GIF image. One side effect is that hardware acceleration is "lost" forever on the View once turned off, so if you reuse the `ImageViewEx` and at some point you assign a GIF image to it, from that point onwards it won't be hardware accelerated anymore. That's a limitation Android itself imposes, so there's not much we can do about that. On the bright side, this only affects cases where hardware acceleration is available; even when software rendering is active, there's not a big performance hit thou.

The second issue is that `Movie` has serious issues on some emulator instances and some retail devices. This is most likely due to some broken code down at native (maybe in Skia) or video driver level. So not much we can do on this one either. On the bright side, we've provided a workaround, that is setting `setCanAlwaysAnimate(false)` on phones known to cause issues. You will lose animation support, but you don't need to get crazy trying to handle several layouts, some using `ImageView`s and some using `ImageViewEx`es.

##Some boring stuff
If you like this project and want to make a contribution, feel free to make a pull request, submit a bug report or ask for anything. Any contribution is appreciated!

If you use this library, letting us know would make us proud. We do not ask for anything else.

## License
Released under the [MIT license](http://www.opensource.org/licenses/mit-license.php).

Copyright (c) 2011-2 Francesco Pontillo and Sebastiano Poggi

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.