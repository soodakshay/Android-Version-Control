package com.akshay.versioninfo;

import android.content.Context;

import java.io.IOException;

import rx.Observable;
import rx.schedulers.Schedulers;

public class VersionInfo {
  public static VersionInfo versionInfo;
  public PackageAvailableCallback callback;

  public static VersionInfo getInstance() {
    if (versionInfo == null) {
      versionInfo = new VersionInfo();
    }
    return versionInfo;
  }

  public void setListener(PackageAvailableCallback callback) {
    this.callback = callback;
  }

  public void onPackageSearched(boolean isFound, String packageName) {
    if (callback != null) {
      callback.onPackageSearchComplete(isFound, packageName);
    }
  }

  public Observable<Boolean> checkPackageAvailabity(String packageName, Context context) {
    try {
      return VersionInfoInternal.isPackageValid(packageName, context)
          .subscribeOn(Schedulers.newThread());

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This function will first check the package name availability
   */
  public Observable<String> isUpdateAvailable(final String packageName, Context context) {
    try {
      return VersionInfoInternal.isPackageValid(packageName, context)
          .flatMap(result -> {
            if (result) {
              onPackageSearched(true, packageName);
              return VersionInfoInternal.grabVersionCode(packageName, Constant.TAG_PLAY_STORE_VERSION);
            } else {
              onPackageSearched(false, packageName);
              return null;
            }
          });

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public interface PackageAvailableCallback {
    void onPackageSearchComplete(boolean isFound, String packageName);
  }

}
