package in.curioustools.water_reminder.ui.screen_dashboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import in.curioustools.water_reminder.R;
import in.curioustools.water_reminder.db.pref.PrefUserDetails;
import in.curioustools.water_reminder.db.pref.PrefUserDetails.Defaults;
import in.curioustools.water_reminder.ui.custom.wave_loader.WaveLoadingView;
import in.curioustools.water_reminder.ui.screen_dashboard.daily_logs_fragment.DailyLogsFragment;
import in.curioustools.water_reminder.ui.screen_dashboard.dashboard_fragment.DashboardFragment;
import in.curioustools.water_reminder.ui.screen_dashboard.settings_fragment.SettingsFragment;

import static in.curioustools.water_reminder.db.pref.PrefUserDetails.KEYS.KEY_DAILY_TARGET;
import static in.curioustools.water_reminder.db.pref.PrefUserDetails.KEYS.KEY_TODAY_INTAKE_ACHIEVED;


public class DashBoardActivity extends AppCompatActivity {
    TabLayout tbTabs;
    ViewPager2 viewPager2;
    WaveLoadingView waveLoader;

    SharedPreferences prefMain;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private static final String TAG = "DashBoardActivity>>";


    //create an about page

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tbTabs = findViewById(R.id.tl_dashboard);
        viewPager2 = findViewById(R.id.vp2_dashboard);

        waveLoader = findViewById(R.id.wave_loader_dashboard);
        waveLoader.setProgressValue(5);

        prefMain = getSharedPreferences(PrefUserDetails.PREF_NAME, MODE_PRIVATE);
        listener = (sharedPreferences, s) -> {
            Log.e("TAG", "onSharedPreferenceChanged: called ");
            updateWave(sharedPreferences);
        };


        updateWave(prefMain);

        Toast t = Toast.makeText(this, "Tap a button to save your drink logs", Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();


        loadFragments();
        handleSplash();


    }

    void updateWave(SharedPreferences preferences) {
        int achieved = preferences.getInt(KEY_TODAY_INTAKE_ACHIEVED, Defaults.TODAY_INTAKE_ACHIEVED);
        int target = preferences.getInt(KEY_DAILY_TARGET, Defaults.DAILY_TARGET);
        int progress = Math.round((float) achieved / target * 100);
        progress = Math.min(progress, 100);
        Log.e(TAG, "updateWave: called progress=" + progress);
        waveLoader.setProgressValue(progress);
    }

    private void loadFragments() {
        DashboardFragmentsAdapter adpPager = new DashboardFragmentsAdapter(this);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setAdapter(adpPager);
        viewPager2.setUserInputEnabled(false);

        adpPager.addFragment(new DailyLogsFragment());
        adpPager.addFragment(new DashboardFragment());
        adpPager.addFragment(new SettingsFragment());

        viewPager2.setCurrentItem(1);




        new TabLayoutMediator(tbTabs, viewPager2, (tab, position) -> {
                    tab.setText("");
                    int iconRes = R.drawable.ic_notif_icon;
                    switch (position) {
                        case 0: {
                            iconRes = R.drawable.ic_daily_logs_white;
                            break;
                        }
                        case 1: {
                            iconRes = R.drawable.ic_notif_icon;
                            break;
                        }
                        case 2: {
                            iconRes = R.drawable.ic_settings;
                            break;
                        }
                    }
                    tab.setIcon(iconRes);

                }).attach();

    }

    private void handleSplash() {
        findViewById(R.id.ll_splash).setVisibility(View.VISIBLE);
        new Thread(() -> {
            SystemClock.sleep(500);
            runOnUiThread(() -> {
                Animation anim = AnimationUtils.loadAnimation(DashBoardActivity.this, R.anim.slide_out);
                findViewById(R.id.ll_splash).startAnimation(anim);
                findViewById(R.id.ll_splash).setVisibility(View.GONE);

            });
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        prefMain.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefMain.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
