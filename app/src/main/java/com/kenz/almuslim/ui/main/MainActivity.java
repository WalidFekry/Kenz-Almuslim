package com.kenz.almuslim.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.messaging.FirebaseMessaging;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.adapter.ViewPagerAdapter;
import com.kenz.almuslim.data.base.BaseActivity;
import com.kenz.almuslim.data.utils.SharedPref;
import com.kenz.almuslim.databinding.ActivityMainBinding;
import com.kenz.almuslim.ui.home.HomeFragment;

import libs.mjn.prettydialog.PrettyDialog;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class MainActivity extends BaseActivity<MainView, MainPresenter> implements MainView {

    public ActivityMainBinding binding;
    private ViewPagerAdapter viewPagerAdapter;
    private final ActivityResultLauncher<String> requestPermissionLauncherNotification =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
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
        binding = DataBindingUtil.setContentView(this, getLayoutId());
        presenter.initView(this, binding, getSupportFragmentManager());

        setupEdgeToEdgeInsets();
        initViewPager();
        setupBackNavigation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        }
    }

    private void setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.coordinator, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Adjust AppBar padding to account for status bar and notch height
            binding.appBarLayout.setPadding(
                    binding.appBarLayout.getPaddingLeft(),
                    systemBars.top,
                    binding.appBarLayout.getPaddingRight(),
                    binding.appBarLayout.getPaddingBottom()
            );

            // Adjust BottomNavigation margin to sit above system navigation bar / gesture bar
            if (binding.bottomNavigation.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) binding.bottomNavigation.getLayoutParams();
                lp.bottomMargin = systemBars.bottom;
                binding.bottomNavigation.setLayoutParams(lp);
            }

            return insets;
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncherNotification.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void initViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.viewPager.setCurrentItem(2);
        binding.viewPager.setOffscreenPageLimit(viewPagerAdapter.getCount());
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
    }

    private void showExitDialog() {
        final PrettyDialog pDialog = new PrettyDialog(this);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setTitle("هل تود الخروج من التطبيق ؟")
                .setMessage("ما رأيك بتقييم التطبيق إذا نال اعجابك !")
                .setIcon(R.mipmap.ic_launcher)
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
                        () -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.kenz.almuslim")))
                )
                .addButton(
                        "مشاركه التطبيق",
                        R.color.gnt_white,
                        R.color.br,
                        () -> {
                            String shareMessage = "تطبيق كنز المسلم | Kenz Almuslim \n"
                                    + "تطبيق إسلامي متميز يحتوي على أجمل الخلفيات الإسلامية، رسائل التفاؤل اليومية، الآيات القرآنية، والأذكار المصممة بدقة عالية HD/4K! 🕌🌸\n\n"
                                    + "🌟 مميزات التطبيق:\n"
                                    + "• خلفيات إسلامية ودينية متجددة بدقة عالية.\n"
                                    + "• رسائل تفاؤل وإشعارات أذكار يومية تنير يومك.\n"
                                    + "• تعيين الصور كخلفية للشاشة الرئيسية ولشاشة القفل بنقرة واحدة.\n"
                                    + "• حفظ الصور في الاستوديو ومشاركتها بسهولة مع أصدقائك.\n"
                                    + "• تصميم عصري مريح للعين مع دعم الوضع الليلي.\n\n"
                                    + "📲 حمل التطبيق الآن مجاناً وشاركه مع من تحب ليكون صدقة جارية:\n"
                                    + "https://play.google.com/store/apps/details?id=" + getPackageName();

                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "تطبيق كنز المسلم");
                            sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            sendIntent.setType("text/plain");
                            startActivity(Intent.createChooser(sendIntent, "مشاركة تطبيق كنز المسلم مع الأصدقاء:"));
                        }
                )
                .addButton(
                        "الخروج من التطبيق",
                        R.color.gnt_white,
                        R.color.red_bg_light,
                        this::finish
                )
                .show();
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

    public void exitCollapsed() {
        presenter.exitCollapsed();
    }

    public void onChangeCheckBox(boolean isChecked) {
        binding.changeOrientation.setChecked(isChecked);
    }

    @Override
    public void onFilterChosen(String filter) {
        if (viewPagerAdapter != null && viewPagerAdapter.fragmentReferences.get(2) != null && viewPagerAdapter.fragmentReferences.get(2).get() instanceof HomeFragment) {
            ((HomeFragment) viewPagerAdapter.fragmentReferences.get(2).get()).onFilterNameChosen(filter);
        }
    }

    @Override
    public void showHorizontal() {
        if (viewPagerAdapter != null && viewPagerAdapter.fragmentReferences.get(2) != null && viewPagerAdapter.fragmentReferences.get(2).get() instanceof HomeFragment) {
            ((HomeFragment) viewPagerAdapter.fragmentReferences.get(2).get()).onShowHorizontal();
        }
    }

    @Override
    public void showVertical() {
        if (viewPagerAdapter != null && viewPagerAdapter.fragmentReferences.get(2) != null && viewPagerAdapter.fragmentReferences.get(2).get() instanceof HomeFragment) {
            ((HomeFragment) viewPagerAdapter.fragmentReferences.get(2).get()).onShowVertical();
        }
    }

    public interface OnNewFeatures {

        void onFilterNameChosen(String filterName);

        void onShowHorizontal();

        void onShowVertical();

    }
}
