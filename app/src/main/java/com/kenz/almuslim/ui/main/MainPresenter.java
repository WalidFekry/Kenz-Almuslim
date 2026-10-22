package com.kenz.almuslim.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.AppBarLayout;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.SharedPref;
import com.kenz.almuslim.databinding.ActivityMainBinding;
import com.kenz.almuslim.ui.splash.SplashActivity;
import com.kenz.almuslim.utils.CommonUtils;
import com.yarolegovich.slidingrootnav.SlideGravity;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import org.jetbrains.annotations.NotNull;

import uc.benkkstudio.bsbottomnavigation.NavigationClickListner;

public class MainPresenter extends BasePresenter<MainView> {
    public static final String THEME_MODE = "THEME_MODE";
    private Activity activity;
    private ActivityMainBinding binding;
    private SlidingRootNav slidingRootNav;

    private SharedPreferences preferences;

    protected void initView(@NotNull Activity activity, @NotNull ActivityMainBinding binding, FragmentManager fragmentManager) {
        this.activity = activity;
        this.binding = binding;
        setClickListener();
        initBottomNavigation();
        initDrawer();

        preferences = activity.getSharedPreferences("DEFAULT", Context.MODE_PRIVATE);
        // setup the view orientation
        setupViewOrientation();

        // setup filter
        binding.iconFilter.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity, binding.iconFilter);
            popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.latest_menu) {
                        getMvpView().onFilterChosen("LATEST");
                    } else if (menuItem.getItemId() == R.id.older_menu) {
                        getMvpView().onFilterChosen("OLDER");
                    }
                    return true;
                }
            });
            popupMenu.show();
        });
    }


    private void setupViewOrientation() {
        binding.changeOrientation.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed() && b) {
                preferences.edit().putString("scroll_type", "H").apply();
                getMvpView().showHorizontal();
            } else if (compoundButton.isPressed() && !b) {
                preferences.edit().putString("scroll_type", "V").apply();
                getMvpView().showVertical();
            }
        });
    }


    private void setupFilter() {

    }


    private void setClickListener() {
        binding.imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingRootNav.openMenu(true);
            }
        });
    }


    protected void exitCollapsed() {
        CoordinatorLayout.LayoutParams appBarLayoutLayoutParams = (CoordinatorLayout.LayoutParams) binding.appBarLayout.getLayoutParams();
        appBarLayoutLayoutParams.setBehavior(new AppBarLayout.Behavior() {
        });
    }

    private void initBottomNavigation() {
        binding.bottomNavigation.addButton("", R.drawable.ic_nav_category);
        binding.bottomNavigation.addButton("", R.drawable.ic_nav_search);
        binding.bottomNavigation.addButton("", R.drawable.ic_gif_tag);
        binding.bottomNavigation.addButton("", R.drawable.ic_nav_favourtie);
        binding.bottomNavigation.showIconOnly();
        binding.bottomNavigation.setSelectedItem(-1);
        binding.bottomNavigation.setButtonClickListener(new NavigationClickListner() {
            @Override
            public void onCentreButtonClick() {
                binding.viewPager.setCurrentItem(2);
                binding.bottomNavigation.setSelectedItem(-1);
                binding.toolbarTitle.setText("أخر الصور");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(int itemIndex, String itemName) {
                switch (itemIndex) {
                    case 0:
                        binding.viewPager.setCurrentItem(0);
                        binding.toolbarTitle.setText("الأصناف");
                        break;
                    case 1:
                        binding.viewPager.setCurrentItem(1);
                        binding.toolbarTitle.setText("بحث");
                        break;
                    case 2:
                        binding.viewPager.setCurrentItem(3);
                        binding.toolbarTitle.setText("GIF صور");
                        break;
                    case 3:
                        binding.viewPager.setCurrentItem(4);
                        binding.toolbarTitle.setText("مفضلتي");
                        break;
                }
                exitCollapsed();
            }
        });
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void initDrawer() {
        slidingRootNav = new SlidingRootNavBuilder(activity).withMenuOpened(false).withContentClickableWhenMenuOpened(false).withGravity(SlideGravity.LEFT).withMenuLayout(R.layout.drawer_layout).inject();
        TextView headerTitle = slidingRootNav.getLayout().findViewById(R.id.headerTitle);
        TextView headerEmail = slidingRootNav.getLayout().findViewById(R.id.headerEmail);
        headerTitle.setText(Constant.app_name);
        headerEmail.setText(Constant.app_email);

        if (CommonUtils.isNetworkConnected(activity)) {
        }


        if (SharedPref.getSharedPref(activity).contains(THEME_MODE)) {
            if (!SharedPref.getSharedPref(activity).readBoolean(THEME_MODE)) {
                binding.themeMode.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_light_mode));
            } else {
                binding.themeMode.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_dark_mode));
            }
        }

        binding.themeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SharedPref.getSharedPref(activity).contains(THEME_MODE)) {
                    if (!SharedPref.getSharedPref(activity).readBoolean(THEME_MODE)) {
                        SharedPref.getSharedPref(activity).write(THEME_MODE, true);
                        binding.themeMode.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_dark_mode));
                    } else {
                        SharedPref.getSharedPref(activity).write(THEME_MODE, false);
                        binding.themeMode.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_light_mode));
                    }
                } else {
                    SharedPref.getSharedPref(activity).write(THEME_MODE, true);
                }
                activity.startActivity(new Intent(activity, SplashActivity.class));
                activity.finish();
            }
        });

        slidingRootNav.getLayout().findViewById(R.id.menuHome).setOnClickListener(view -> {
            slidingRootNav.closeMenu(true);
            binding.viewPager.setCurrentItem(2);
        });

        slidingRootNav.getLayout().findViewById(R.id.menuLatest).setOnClickListener(view -> {
            slidingRootNav.closeMenu(true);
            binding.viewPager.setCurrentItem(2);
        });

        slidingRootNav.getLayout().findViewById(R.id.menuCategory).setOnClickListener(view -> {
            slidingRootNav.closeMenu(true);
            binding.viewPager.setCurrentItem(0);
        });

        slidingRootNav.getLayout().findViewById(R.id.menuFavourite).setOnClickListener(view -> {
            slidingRootNav.closeMenu(true);
            binding.viewPager.setCurrentItem(4);
        });

        slidingRootNav.getLayout().findViewById(R.id.menuShare).setOnClickListener(view -> {
            slidingRootNav.closeMenu(true);
            String shareMessage = "تطبيق كنز المسلم | Kenz Almuslim \n"
                    + "تطبيق إسلامي متميز يحتوي على أجمل الخلفيات الإسلامية، رسائل التفاؤل اليومية، الآيات القرآنية، والأذكار المصممة بدقة عالية HD/4K! 🕌🌸\n\n"
                    + "🌟 مميزات التطبيق:\n"
                    + "• خلفيات إسلامية ودينية متجددة بدقة عالية.\n"
                    + "• رسائل تفاؤل وإشعارات أذكار يومية تنير يومك.\n"
                    + "• تعيين الصور كخلفية للشاشة الرئيسية ولشاشة القفل بنقرة واحدة.\n"
                    + "• حفظ الصور في الاستوديو ومشاركتها بسهولة مع أصدقائك.\n"
                    + "• تصميم عصري مريح للعين مع دعم الوضع الليلي.\n\n"
                    + "📲 حمل التطبيق الآن مجاناً وشاركه مع من تحب ليكون صدقة جارية:\n"
                    + "https://play.google.com/store/apps/details?id=" + activity.getPackageName();

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "تطبيق كنز المسلم");
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            sendIntent.setType("text/plain");
            activity.startActivity(Intent.createChooser(sendIntent, "مشاركة تطبيق كنز المسلم مع الأصدقاء:"));
        });

        slidingRootNav.getLayout().findViewById(R.id.menuRate).setOnClickListener(view -> {
            slidingRootNav.closeMenu(true);
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.kenz.almuslim");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
        });

        slidingRootNav.getLayout().findViewById(R.id.menuMore).setOnClickListener(view -> {
            slidingRootNav.closeMenu(true);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.play_developer_id)));
            activity.startActivity(intent);
        });

        slidingRootNav.getLayout().findViewById(R.id.menuPrivacy).setOnClickListener(view -> {
            slidingRootNav.closeMenu(true);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/kenz-almuslim"));
            activity.startActivity(intent);
        });
    }


}