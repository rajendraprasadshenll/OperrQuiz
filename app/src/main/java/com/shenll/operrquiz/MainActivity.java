package com.shenll.operrquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.shenll.operrquiz.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create binding class object for accessing view components
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        // set the build type to view
        binding.test.setText(BuildConfig.BUILD_TYPE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //check service enable or not
        if (!NotificationTimerService.isServiceEnable) {
            // create service intent and start service for creating background working task notification
            // start foreground service
            startService(new Intent(this, NotificationTimerService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (NotificationTimerService.isServiceEnable) {
            Toast.makeText(this, getString(R.string.message), Toast.LENGTH_LONG).show();
        }
    }
}
