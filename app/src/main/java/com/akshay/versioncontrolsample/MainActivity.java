package com.akshay.versioncontrolsample;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.akshay.versioninfo.VersionInfo;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements VersionInfo.PackageAvailableCallback {

  private String TAG = MainActivity.class.getSimpleName();
  private TextView mTextViewVersionInfo;
  private EditText mEditTextVersion;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    VersionInfo.getInstance().setListener(this);
    initView();
  }

  /**
   * Initialize View
   */
  private void initView() {
    mTextViewVersionInfo = (TextView) findViewById(R.id.textViewVersionInfo);
    Button buttonGrabVersion = (Button) findViewById(R.id.buttonGrabVersion);
    mEditTextVersion = (EditText) findViewById(R.id.editTextVersion);

    buttonGrabVersion.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (mEditTextVersion.getText().toString().trim().isEmpty()) {
          Toast.makeText(MainActivity.this, "Please enter packageName", Toast.LENGTH_SHORT).show();
          return;
        }

        getVersionName(mEditTextVersion.getText().toString().trim());
      }
    });


  }

  /**
   * This function will get the version name from play store
   */
  private void getVersionName(String packageName) {
    String versionName = "";
    //Get the current App Version
    try {
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      versionName = packageInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }


    final String finalVersionName = versionName;
    Observable<String> versionObserver = VersionInfo.getInstance().isUpdateAvailable(packageName, this);
    if (versionObserver == null) {
      Toast.makeText(this, "Package => " + packageName + " not found on Play Store", Toast.LENGTH_SHORT).show();
      return;
    }
    versionObserver.subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<String>() {
          @Override public void onCompleted() {

          }

          @Override public void onError(Throwable e) {
            e.printStackTrace();
          }

          @Override public void onNext(String s) {
            if (s != null && !s.isEmpty()) {
              mTextViewVersionInfo.setText(s);
              //Version class will help us to compare two different versions
              Version currentVersion = new Version(finalVersionName);
              Version playStoreVersion = new Version(s);



              if (currentVersion.compareTo(playStoreVersion) == -1) {
                //A new Version is available
                //TODO SHOW UPDATE DIALOG
              } else {
                //No Update Available
                //TODO No Dialog Required
              }
            }
          }
        });
  }

  @Override public void onPackageSearchComplete(final boolean isFound, final String packageName) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        if (!isFound) {
          Toast.makeText(MainActivity.this, "Package => " + packageName + " Not Found On PlayStore", Toast.LENGTH_SHORT).show();
        }
      }
    });
  }
}
