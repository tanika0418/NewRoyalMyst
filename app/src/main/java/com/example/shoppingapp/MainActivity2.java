package com.example.shoppingapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.TaskStackBuilder;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppingapp.databinding.ActivityMain2Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.shoppingapp.ProductDetailsActivity.cartItem;
import static com.example.shoppingapp.RegisterActivity.setSignUpFragment;

public class MainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final int HOMEMADE_FRAGMENT = 0;
    private static final int CART_FRAGMENT = 1;
    private static final int ORDERS_FRAGMENT = 2;
    private static final int WISHLIST_FRAGMENT = 3;
    private static final int REWARDS_FRAGMENT = 4;
    private static final int ACCOUNT_FRAGMENT = 5;
    public static Boolean showCart = false;
    public static Activity mainActivity2;
    public static boolean resetMainActivity2 = false;

    private FrameLayout frameLayout;

    private ImageView actionBarLogo;
    private int currentFragment = -1;
    private NavigationView navigationView;

    private Window window;
    private Toolbar toolbar;
    private Dialog signInDialog;
    private FirebaseUser currentUser;
    private TextView badgeCount;
    private int scrollFlags;
    private AppBarLayout.LayoutParams params;
    private CircleImageView profileView;
    private TextView fullname,email;
    private ImageView addProfileIcon;

    public static DrawerLayout drawer;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        toolbar = findViewById(R.id.toolbar);
        actionBarLogo = findViewById(R.id.actionbar_logo);
        setSupportActionBar(toolbar);

        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        scrollFlags = params.getScrollFlags();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);


        frameLayout = findViewById(R.id.main_framelayout);

        profileView = navigationView.getHeaderView(0).findViewById(R.id.main_profile_image);
        fullname = navigationView.getHeaderView(0).findViewById(R.id.main_fullname);
        email = navigationView.getHeaderView(0).findViewById(R.id.main_email);
        addProfileIcon = navigationView.getHeaderView(0).findViewById(R.id.add_profile_icon);


        if (showCart) {
            mainActivity2 = this;
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            gotoFragment("My Cart", new MyCartFragment(), -2);
        } else {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this
                    , drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            setFragment(new HomemadeFragment(), HOMEMADE_FRAGMENT);
        }


        signInDialog = new Dialog(MainActivity2.this);
        signInDialog.setContentView(R.layout.sign_in_dialog);
        signInDialog.setCancelable(true);
        signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button dialogSignInBtn = signInDialog.findViewById(R.id.sign_in_btn);
        Button dialogSignUpBtn = signInDialog.findViewById(R.id.sign_up_btn);

        Intent registerIntent = new Intent(MainActivity2.this, RegisterActivity.class);


        dialogSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sign_in_fragment.disableCloseBtn = true;
                sign_up_fragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = false;
                startActivity(registerIntent);
            }
        });

        dialogSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sign_in_fragment.disableCloseBtn = true;
                sign_up_fragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = true;
                startActivity(registerIntent);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setEnabled(false);
        } else {



            if (DBqueries.email == null) {
                FirebaseFirestore.getInstance().collection("USERS").document(currentUser.getUid())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DBqueries.fullname = task.getResult().getString("fullname");
                            DBqueries.email = task.getResult().getString("email");
                            DBqueries.profile = task.getResult().getString("profile");

                            fullname.setText(DBqueries.fullname);
                            email.setText(DBqueries.email);
                            if (DBqueries.profile.equals("")) {
                                addProfileIcon.setVisibility(View.VISIBLE);
                            } else {
                                addProfileIcon.setVisibility(View.INVISIBLE);
                                Glide.with(MainActivity2.this).load(DBqueries.profile).apply(new RequestOptions().placeholder(R.drawable.ic_baseline_person_24)).into(profileView);
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(MainActivity2.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                fullname.setText(DBqueries.fullname);
                email.setText(DBqueries.email);
                if (DBqueries.profile.equals("")) {
                    profileView.setImageResource(R.drawable.ic_baseline_person_24);
                    addProfileIcon.setVisibility(View.VISIBLE);
                } else {
                    addProfileIcon.setVisibility(View.INVISIBLE);
                    Glide.with(MainActivity2.this).load(DBqueries.profile).apply(new RequestOptions().placeholder(R.drawable.ic_baseline_person_24)).into(profileView);
                }
            }
            navigationView.getMenu().getItem(navigationView.getMenu().size() - 2).setEnabled(true);

        }
        if (resetMainActivity2){
            actionBarLogo.setVisibility(View.VISIBLE);
            resetMainActivity2 = false;
            setFragment(new HomemadeFragment(), HOMEMADE_FRAGMENT);
            navigationView.getMenu().getItem(0).setChecked(true);
        }
        invalidateOptionsMenu();

    }

    @Override
    protected void onPause() {
        super.onPause();
        DBqueries.checkNotifications(true,null);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (currentFragment == HOMEMADE_FRAGMENT) {
                super.onBackPressed();
                currentFragment = -1;
            } else {
                if (showCart) {
                    mainActivity2 = null;
                    showCart = false;
                    finish();
                } else {
                    actionBarLogo.setVisibility(View.VISIBLE);
                    invalidateOptionsMenu();
                    setFragment(new HomemadeFragment(), HOMEMADE_FRAGMENT);
                    navigationView.getMenu().getItem(0).setChecked(true);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentFragment == HOMEMADE_FRAGMENT) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getMenuInflater().inflate(R.menu.main_activity2, menu);

            MenuItem cartItem = menu.findItem(R.id.main_cart_icon);


            cartItem.setActionView(R.layout.badge_layout);
            ImageView badgeIcon = cartItem.getActionView().findViewById(R.id.badge_icon);
            badgeIcon.setImageResource(R.drawable.cart_icon_top);
            badgeCount = cartItem.getActionView().findViewById(R.id.badge_count);

            if  (currentUser != null) {

                if (DBqueries.cartList.size() == 0) {
                    DBqueries.loadCartList(MainActivity2.this, new Dialog(MainActivity2.this), false, badgeCount,new TextView(MainActivity2.this));
                    badgeCount.setVisibility(View.INVISIBLE);
                } else {
                    badgeCount.setVisibility(View.VISIBLE);
                    if (DBqueries.cartList.size() < 99) {
                        badgeCount.setText(String.valueOf(DBqueries.cartList.size()));
                    } else {
                        badgeCount.setText("99");
                    }
                }
            }

            cartItem.getActionView().setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    if (currentUser == null) {
                        signInDialog.show();
                    } else {
                        gotoFragment("My Cart", new MyCartFragment(), CART_FRAGMENT);
                    }
                }
            });

            MenuItem notifyItem = menu.findItem(R.id.main_notification_icon);

            notifyItem.setActionView(R.layout.badge_layout);
            ImageView notifyIcon = notifyItem.getActionView().findViewById(R.id.badge_icon);
            notifyIcon.setImageResource(R.drawable.notification_icon);
           TextView notifyCount = notifyItem.getActionView().findViewById(R.id.badge_count);

           if (currentUser != null){
               DBqueries.checkNotifications(false,notifyCount);
           }

           notifyItem.getActionView().setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent notificationIntent = new Intent(MainActivity2.this,NotificationActivity.class);
                   startActivity(notificationIntent);
               }
           });

        }
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onOptionsItemSelected (@NonNull MenuItem item){
            int id = item.getItemId();
            if (id == R.id.main_search_icon) {
                Intent searchIntent = new Intent(this,SearchActivity.class);
                startActivity(searchIntent);
                return true;
            } else if (id == R.id.main_notification_icon) {
                Intent notificationIntent = new Intent(this,NotificationActivity.class);
                startActivity(notificationIntent);
                return true;
            } else if (id == R.id.main_cart_icon) {

                if (currentUser == null) {
                    signInDialog.show();
                } else {
                    gotoFragment("My Cart", new MyCartFragment(), CART_FRAGMENT);
                }
                return true;
            } else if (id == android.R.id.home) {
                if (showCart) {
                    mainActivity2 = null;
                    showCart = false;
                    finish();
                    return true;
                }
            }

            return super.onOptionsItemSelected(item);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void gotoFragment (String title, Fragment fragment,int fragmentNo){
            actionBarLogo.setVisibility(View.GONE);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(title);
            invalidateOptionsMenu();
            setFragment(fragment, fragmentNo);
            if (fragmentNo == CART_FRAGMENT || showCart) {
                navigationView.getMenu().getItem(3).setChecked(true);
                params.setScrollFlags(0);

            }else{
                params.setScrollFlags(scrollFlags);
            }
        }

        MenuItem menuItem;
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onNavigationItemSelected (@NonNull @NotNull MenuItem item){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            menuItem = item;


            if (currentUser != null) {

                drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        int id = menuItem.getItemId();
                        if (id == R.id.nav_my_mall) {
                            actionBarLogo.setVisibility(View.VISIBLE);
                            invalidateOptionsMenu();
                            setFragment(new HomemadeFragment(), HOMEMADE_FRAGMENT);
                        } else if (id == R.id.nav_my_orders) {
                            gotoFragment("My Orders", new MyOrdersFragment(), ORDERS_FRAGMENT);
                        } else if (id == R.id.nav_my_rewards) {
                            gotoFragment("My Rewards", new MyRewardsFragment(), REWARDS_FRAGMENT);
                        } else if (id == R.id.nav_my_cart) {
                            gotoFragment("My Cart", new MyCartFragment(), CART_FRAGMENT);

                        } else if (id == R.id.nav_my_wishlist) {
                            gotoFragment("My Wishlist", new MyWishlistFragment(), WISHLIST_FRAGMENT);
                        } else if (id == R.id.nav_my_account) {
                            gotoFragment("My Account", new MyAccountFragment(), ACCOUNT_FRAGMENT);
                        } else if (id == R.id.nav_sign_out) {
                            FirebaseAuth.getInstance().signOut();
                            DBqueries.clearData();
                            Intent registerIntent = new Intent(MainActivity2.this, RegisterActivity.class);
                            startActivity(registerIntent);
                            finish();
                        }else if (id==R.id.nav_about_developer){
                            Intent developerIntent = new Intent(MainActivity2.this,AboutDeveloperActivity.class);
                            startActivity(developerIntent);
                        }

                        drawer.removeDrawerListener(this);
                    }
                });


                return true;
            } else {
                signInDialog.show();
                return false;
            }


        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void setFragment (Fragment fragment,int fragmentNo){
            if (fragmentNo != currentFragment) {
                if (fragmentNo == REWARDS_FRAGMENT) {
                    window.setStatusBarColor(Color.parseColor("#4859B6"));
                    toolbar.setBackgroundColor(Color.parseColor("#4859B6"));
                } else {
                    window.setStatusBarColor(getResources().getColor(R.color.purple_200));
                    toolbar.setBackgroundColor(getResources().getColor(R.color.purple_200));
                }
                currentFragment = fragmentNo;
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                fragmentTransaction.replace(frameLayout.getId(), fragment);
                fragmentTransaction.commit();
            }
        }
    }