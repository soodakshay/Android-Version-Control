package com.akshay.versioninfo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by Akshay Sood on 7/4/17.
 */

public class VersionInfoInternal {


  private static String TAG = VersionInfoInternal.class.getSimpleName();

  /**
   * This function will check if the app with the same package is available
   * on the Google Play Store
   *
   * @param packageName Package name of the app
   * @param context
   * @return If true then package is available
   * else package is not available on Play Store
   */
  public static Observable<Boolean> isPackageValid(final String packageName,
                                                   Context context) throws IOException {
    return Observable.fromEmitter(new Action1<Emitter<Boolean>>() {
      @Override public void call(Emitter<Boolean> booleanEmitter) {

        try {
          boolean result = validatePackage(packageName);
          booleanEmitter.onNext(result);
        } catch (IOException e) {
          booleanEmitter.onError(e);
        }
        booleanEmitter.onCompleted();
      }
    }, Emitter.BackpressureMode.LATEST);
  }


  /**
   * This function will get the latest app version available from play store
   */
  public static Observable<String> grabVersionCode(final String packageName,
                                                   final String property) {
    return Observable.fromEmitter(new Action1<Emitter<String>>() {
      @Override public void call(Emitter<String> stringEmitter) {
        String parsedData = null;
        try {
          parsedData = Jsoup.connect(Constant.PLAYSTORE_URL + packageName)
              .timeout(Constant.DEFAULT_TIMEOUT)
              .ignoreHttpErrors(true)
              .referrer("https://www.google.com").get()
              .select("div[itemprop=" + property + "]").first()
              .ownText();
          stringEmitter.onNext(parsedData);

        } catch (IOException e) {
          stringEmitter.onError(e);
        }
        stringEmitter.onCompleted();
      }
    }, Emitter.BackpressureMode.LATEST);
  }

  /**
   * This function will validate the package name on Play Store
   */
  private static boolean validatePackage(String packageName) throws IOException {
    URL url = new URL(Constant.PLAYSTORE_URL + packageName);
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setRequestMethod("GET");

    urlConnection.connect();
    if (urlConnection.getResponseCode() == 200) {
      //Package is Available
      Log.e(TAG, "Package " + packageName + " is Available on Google Play Store");
      return true;

    } else {
      //Package is not available on Play Store
      Log.e(TAG, "Package " + packageName + "  is not  Available on Google Play Store");
      return false;
    }
  }
}
