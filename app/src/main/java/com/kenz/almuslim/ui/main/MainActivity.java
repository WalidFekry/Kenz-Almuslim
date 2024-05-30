package com.kenz.almuslim.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;


import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kenz.almuslim.R;
import com.kenz.almuslim.Settings;
import com.kenz.almuslim.data.adapter.ViewPagerAdapter;
import com.kenz.almuslim.data.base.BaseActivity;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.SharedPref;
import com.kenz.almuslim.databinding.ActivityMainBinding;
import com.kenz.almuslim.ui.home.HomeFragment;


import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class MainActivity extends BaseActivity<MainView, MainPresenter> implements MainView {

    public ActivityMainBinding binding;
    String[] permission = new String[]{
            Manifest.permission.POST_NOTIFICATIONS
    };
    boolean isPermission = false;
    private final ActivityResultLauncher<String> requestPermissionLauncherNotification =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                isPermission = isGranted;
            });

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void beforeOnCreate() {
        if (SharedPref.getSharedPref(this).contains(MainPresenter.THEME_MODE) && SharedPref.getSharedPref(this).readBoolean(MainPresenter.THEME_MODE)) {
            setTheme(R.style.BStudioThemeDark);
        } else {
            setTheme(R.style.BStudioThemeLight);
        }
        FirebaseMessaging.getInstance().subscribeToTopic("all");
    }

    @Override
    protected void onStarting() {
        if (!Settings.premium_unlocked_premanent) {
            SharedPref.getSharedPref(this).write("premium_checker", "");
        }
        binding = DataBindingUtil.setContentView(this, getLayoutId());
        presenter.initView(this, binding, getSupportFragmentManager());
        initViewPager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            cheakNotificationPermission();
        }
    }

    private void cheakNotificationPermission() {
        if (!isPermission) {
            requestPermissionsNotfication();
        }
    }

    private void requestPermissionsNotfication() {
        if (ContextCompat.checkSelfPermission(this, permission[0]) == PackageManager.PERMISSION_GRANTED) {
            isPermission = true;
        } else {
            requestPermissionLauncherNotification.launch(permission[0]);
            showPermissionDialog();
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setMessage("من فضلك قم بالموافقة على الاشعارات لكي تصلك رسائل التفاؤل والإقتباسات من التطبيق ..")
                .setPositiveButton("الاعدادات", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("إلغاء", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private ViewPagerAdapter viewPagerAdapter;

    private void initViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.viewPager.setCurrentItem(2);
        binding.viewPager.setOffscreenPageLimit(viewPagerAdapter.getCount());
//        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
    }

    @Override
    protected void onDestroyed() {
    }

    @Override
    protected MainPresenter initPresenter() {
        return new MainPresenter();
    }

    @Override
    public void BottomNavigationCenter() {

    }

    @Override
    public void BottomNavigationHome() {

    }

    @Override
    public void BottomNavigationCategory() {

    }


    @Override
    public void onBackPressed() {
        final PrettyDialog pDialog = new PrettyDialog(this);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setTitle("هل تود الخروج من التطبيق ؟")
                .setMessage("ما رأيك بتقييم التطبيق إذا نال اعجابك !").
                setIcon(R.mipmap.ic_launcher)
                .setAnimationEnabled(true)
                .setMessageColor(R.color.btn_bg)
                .setTitleColor(R.color.b1)
                .setIconTint(R.color.pdlg_color_white)
                .setGravity(Gravity.DISPLAY_CLIP_HORIZONTAL)
                .setTypeface(Typeface.createFromAsset(getAssets(), "jazeera.ttf"))
                .addButton(
                        "تقييم التطبيق ",
                        R.color.gnt_white,
                        R.color.n2,
                        new PrettyDialogCallback() {
                            @Override
                            public void onClick() {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.co/w1AIQWabTc")));
                            }
                        }
                )

                .addButton(
                        "مشاركه التطبيق",
                        R.color.gnt_white,
                        R.color.br,
                        new PrettyDialogCallback() {
                            @Override
                            public void onClick() {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_SUBJECT, " تطبيق كنز المسلم");
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "\n" + getResources().getString(R.string.aboutapp) + "\n" +
                                        "تفضل رابط التطبيق  https://t.co/w1AIQWabTc \n");
                                sendIntent.setType("text/plain");
                                startActivity(Intent.createChooser(sendIntent, "مشاركه تطبيق كنز المسلم مع الاصدقاء:"));
                            }
                        }
                )
                .addButton(
                        "الخروج من التطبيق",
                        R.color.gnt_white,
                        R.color.red_bg_light,
                        new PrettyDialogCallback() {
                            @Override
                            public void onClick() {
                                finish();
                            }
                        }
                )
                .show();

//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        }
    }

    public void exitCollapsed() {
        presenter.exitCollapsed();
    }

//    @Override
//    public void onOrientationClickChanged(AppCompatCheckBox appCompatCheckBox, boolean isChecked) {
//        if (viewPagerAdapter.fragmentReferences.get(2).get() instanceof HomeFragment) {
//            ((HomeFragment) viewPagerAdapter.fragmentReferences.get(2).get()).onCheckedChanged(appCompatCheckBox, isChecked);
//        }
//    }


    public void onChangeCheckBox(boolean isChecked) {
        binding.changeOrientation.setChecked(isChecked);
    }

    @Override
    public void onFilterChosen(String filter) {
        if (viewPagerAdapter.fragmentReferences.get(2).get() instanceof HomeFragment) {
            ((HomeFragment) viewPagerAdapter.fragmentReferences.get(2).get()).onFilterNameChosen(filter);
        }
    }

    @Override
    public void showHorizontal() {
        if (viewPagerAdapter.fragmentReferences.get(2).get() instanceof HomeFragment) {
            ((HomeFragment) viewPagerAdapter.fragmentReferences.get(2).get()).onShowHorizontal();
        }
    }

    @Override
    public void showVertical() {
        if (viewPagerAdapter.fragmentReferences.get(2).get() instanceof HomeFragment) {
            ((HomeFragment) viewPagerAdapter.fragmentReferences.get(2).get()).onShowVertical();
        }
    }

    public interface OnNewFeatures {

        void onFilterNameChosen(String filterName);

        void onShowHorizontal();

        void onShowVertical();

    }
}
