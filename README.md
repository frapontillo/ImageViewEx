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
This library requires Android **API level 8** (Android 2.2) as minimum, and targets the Android **API level 17**.

Starting from version 2.0.0, you need to include in your destination project:

 * [JakeWharton/DiskLruCache](https://github.com/JakeWharton/DiskLruCache) library, used for caching on disk.
 * [foxykeep/DataDroid](https://github.com/foxykeep/DataDroid) library, used for handling async operations.
 
The Eclipse project included specifies this is a library project, although it provides two basic Activities for testing the extended `ImageView`s provided.

For your application, you need to include the permissions specified in the AndroidManifest of the library, which are:

 * `android.permission.INTERNET` for getting images on the internet
 * `android.permission.ACCESS_NETWORK_STATE` to monitor the network state
 * `android.permission.WRITE_EXTERNAL_STORAGE` for making the cache access and write the SD card
 
##Documentation
This is a brief documentation of the classes, methods and views included in this library.

###ImageViewEx
`ImageViewEx` is an extended `ImageView` that supports some additional methods for your every-day life.

####Animated GIF
The best thing about `ImageViewEx` is its **automatic handling of animated GIF images** starting from a simple `byte[]`.

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

So we've provided you with a **conditional method** that gets triggered just before each animation begins, `boolean canAnimate()`. This method should be overridden by your custom implementation. By default, it always returns `true`. This method decides whether animations can be started for this instance of `ImageViewEx`.

If you don't want to have another class extending `ImageViewEx` and your `canAnimate()` returns the same value throughout your application, you can use the following

```java
	ImageViewNext.setCanAlwaysAnimate(false);
```

to specify you never want to animate GIFs. If you don't set any value to `setCanAlwaysAnimate`, it defaults to `true`. The result you get by setting the value to `false` is that it will stop all animations, no matter what `canAnimate()` returns.

You can check the current behavior by calling the `static boolean getCanAlwaysAnimate()` method.

####Density Level
You can set a **specific density to simulate** for every instance of `ImageViewEx` by using the following methods:

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
	
	// Sets a density for the img1 only.
	// Changing the density after an object has been set will
	// do nothing, you will have to re-set the object.
	img1.setInDensity(DisplayMetrics.DENSITY_LOW);
	
	img1.setSource(Converters.assetToByteArray(getAssets(), "image.png"));
    img2.setSource(Converters.assetToByteArray(getAssets(), "animated_image.gif"));
```

###ImageViewExService
The `ImageViewExService` service is internally used by `ImageViewNext` for handling asynchronous operation. You need to declare this service in your `AndroidManifest.xml`:

```xml
	<service android:name="net.frakbot.imageviewex.service.ImageViewExService"/>
```

###ImageViewNext
`ImageViewExService` is used by `ImageViewNext`, an extension of `ImageViewEx` that handles **downloading, displaying and caching of images (and animated GIFs, of course)**.

`ImageViewNext` extends `ImageViewEx`, thus supporting all of its methods, plus some more.

####Loading and Error Drawables
`ImageViewNext` supports loading and error `Drawable`s:

 * `static void setClassLoadingDrawable(int classLoadingDrawableResId)` sets a `Drawable` for every instance of `ImageViewNext` from the resources to be displayed (and animated, if it's an `AnimatedDrawable`) as soon as the caching tells us there's no in-memory reference for the asked resource. If you have enabled a disk cache, this `Drawable` will be set before fetching the disk memory.
 * `void setLoadingDrawable(Drawable loadingDrawable)` sets a `Drawable` for the current instance of `ImageViewNext` from the resources to be displayed (and animated, if it's an `AnimatedDrawable`) as soon as the caching tells us there's no in-memory reference for the asked resource. If you have enabled a disk cache, this `Drawable` will be set before fetching the disk memory.
 * `static void setClassErrorDrawable(int classErrorDrawableResId)` sets a `Drawable` for every instance of `ImageViewNext` from the resources to be displayed (and animated, if it's an `AnimatedDrawable`) as soon as the RemoteLoader returns an error, not being able to retrieve the image.
 * `void setErrorDrawable(Drawable errorDrawable)` sets a `Drawable` for the current instance of of `ImageViewNext` from the resources to be displayed (and animated, if it's an `AnimatedDrawable`) as soon as the RemoteLoader returns an error, not being able to retrieve the image.
 * `Drawable getLoadingDrawable()` returns the `Drawable` to be displayed while waiting for long-running operations.
 * `Drawable getErrorDrawable()` returns the `Drawable` to be displayed in case of an error.

####Remote loading and caching of images
`ImageViewNext` uses `ImageViewExService` and some DataDroid `Operation`s to retrieve images from a two-level cache and the internet and set them into your `ImageViewNext`.
 
`ImageViewNext` takes care of instantiating the cache to some default values, which can be overridden/read by using the following `static` methods (pretty self-explanatory, read the JavaDoc for more information about them):

 * `getMemCache()`
 * `getDiskCache()`
 * `getMemCacheSize()`
 * `setMemCacheSize(int memCacheSize)`
 * `getAppVersion()`
 * `setAppVersion(int appVersion)`
 * `getDiskCacheSize()`
 * `setDiskCacheSize(int diskCacheSize)`

####Getting images from the Internet
In order to get images from the Internet, simply call `setUrl(String url)` to start retrieving an image from the internet or the caches.

`ImageViewNext` can be overridden in order to do some custom operations in the following methods:

 * `void onMemCacheHit(byte[] image)` is called as soon as there's a memory cache hit for the requested URL
 * `void onMemCacheMiss()` is called as soon as there's a memory cache miss for the requested URL
 * `void onDiskCacheHit(byte[] image)` is called as soon as there's a disk cache hit for the requested URL
 * `void onDiskCacheMiss()` is called as soon as there's a disk cache miss for the requested URL
 * `void onNetworkHit(byte[] image)` is called as soon as there's a network hit for the requested URL
 * `void onNetworkMiss()` is called as soon as there's a network miss for the requested URL
 * `void onMiss()` is called when an error occurs or the resource can't be found anywhere
 * `void onSuccess(byte[] image)` is automatically called after the image has been retrieved

You should not worry about setting images, as this is handled by `ImageViewNext` itself , which by defaults sets the loading image when there's a memory miss (on `onMemCacheMiss()`), an error one in case of error (`onMiss()`) and the retrieved image in case of success (`onSuccess(byte[] image)`).

If you override `ImageViewNext`, always call the default implementation of these methods.

####Maximum number of threads

You can set the maximum number of concurrent threads; threads are used to retrieve an image, given its URL, from the memory cache, the disk cache or the network.

Use `ImageViewNext.setMaximumNumberOfThreads(THREAD_NUMBER)` BEFORE any `ImageViewNext` object is instantiated (ideally, in your `Application` class), as calling this function again after an `ImageViewNext` has been instantiated will have no effect.

You can retrieve the maximum number of concurrent threads with `ImageViewNext.getMaximumNumberOfThreads()`.

####Example of use

```java
	// Sets class-level loading/error Drawables
	ImageViewNext.setClassErrorDrawable(R.drawable.error_thumb);
    ImageViewNext.setClassLoadingDrawable(R.drawable.loading_spinner);
	
	img1.setUrl("http://upload.wikimedia.org/wikipedia/commons/9/91/Cittadimatera1.jpg");
    img2.setUrl("http://upload.wikimedia.org/wikipedia/commons/4/49/Basilicata_Matera1_tango7174.jpg");
```

###Known issues and workarounds

`ImageViewEx`internally uses an old Android Framework class, `Movie`, to parse animated GIFs. This ensures fast execution, since the `Movie` class internally relies on native code. Due to `Movie` being a legacy class, though, there are a few quirks.

Firstly, you can't have `Movie` working on an hardware-accelerated canvas in Honeycomb and newer versions of Android. The `ImageViewEx` thus automatically disables hardware acceleration by itself when it has to display a GIF image. One side effect is that hardware acceleration is "lost" forever on the View once turned off, so if you reuse the `ImageViewEx` and at some point you assign a GIF image to it, from that point onwards it won't be hardware accelerated anymore. That's a limitation Android itself imposes, so there's not much we can do about that. On the bright side, this only affects cases where hardware acceleration is available; even when software rendering is active, there's not a big performance hit thou.

The second issue is that `Movie` has serious issues on some emulator instances and some retail devices. This is most likely due to some broken code down at native (maybe in Skia) or video driver level. So not much we can do on this one either. On the bright side, we've provided a workaround, that is setting `setCanAlwaysAnimate(false)` on phones known to cause issues. You will lose animation support, but you don't need to get crazy trying to handle several layouts, some using `ImageView`s and some using `ImageViewEx`es.

##Some boring stuff
If you like this project and want to make a contribution, feel free to make a pull request, submit a bug report or ask for anything. Any contribution is appreciated!

If you use this library, letting us know would make us proud. We do not ask for anything else.

## Version history

### 2.0.0-alpha3
 * Enabled setting of maximum number of concurrent threads.
 * Minor fixes and improvements.

### 2.0.0-alpha2
 * Speed improvements.
 * Moved assets to the test project.

### 2.0.0-alpha
 * Caching/async system completelly rewrited.
 * Several performance optimization.
 * Few bugs fixed.

### 1.1.0
 * Few bugs fixed.

### 1.0.0
 * First release.

## License
Released under the [MIT license](http://www.opensource.org/licenses/mit-license.php).

Copyright (c) 2011-2013 Francesco Pontillo and Sebastiano Poggi

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.