package com.example.capstoneproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class About_Page extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Constants
    static final float END_SCALE = 0.7f;  // Scale factor for contentView when the navigation drawer is opened

    // User information
    String personName, personEmail;

    // Google sign-in options and client
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    // LinkedIn sign-in user profile
    UserProfile userProfile;

    // UI components
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    LinearLayout contentView;
    private MyDatabaseHelper dbHelper;
    ImageView image, menuIcon, notification;
    TextView para;
    Button unseen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);

        // Initialize database helper
        dbHelper = new MyDatabaseHelper();

        // Hide the default ActionBar to give a full-screen view
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize all the UI components (hooks)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        contentView = findViewById(R.id.content);
        menuIcon = findViewById(R.id.menu_icon);
        notification = findViewById(R.id.notification);
        image = findViewById(R.id.poster);
        para = findViewById(R.id.para);
        unseen = findViewById(R.id.unseen);

        // Get the last signed-in LinkedIn account
        userProfile = UserProfile.getInstance();

        if (userProfile.getName() != null && userProfile.getEmail() != null){
            personName = userProfile.getName();   // Get the user's name
            personEmail = userProfile.getEmail(); // Get the user's email
        }

        else {
            // Setup Google Sign-In options to request user's email
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            gsc = GoogleSignIn.getClient(this, gso);

            // Get the last signed-in Google account
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                personName = acct.getDisplayName();  // Get the user's name
                personEmail = acct.getEmail();       // Get the user's email
            }
        }

        // Check if the user has seen the latest announcement
        dbHelper.getSeenAnnouncement(personEmail, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    // If seenStatus exists, hide the "unseen" button
                    if (dataSnapshot.exists()) {
                        String seenStatus = dataSnapshot.getValue(String.class);
                        if (seenStatus != null) {
                            unseen.setVisibility(View.GONE);  // Hide the unseen button
                        }
                    } else {
                        unseen.setVisibility(View.VISIBLE);  // Show the unseen button
                    }
                } catch (Exception e) {
                    System.out.println("An error occurred while processing SeenAnnouncement status: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log any error that occurs while retrieving the announcement status
                System.out.println("Failed to retrieve SeenAnnouncement status: " + databaseError.getMessage());
            }
        });

        // Set the descriptive text for the About page (conference details)
        para.setText("The Australasian Conference on Information Systems (ACIS 2024) will be hosted " +
                "at Canberra the capital of Australia from 4 December to 6 December 2024. Canberra meaning " +
                "“the meeting place” in the local Ngunnawal language, is one of the world’s most sustainable " +
                "cities. Let us come together to Canberra and share our research insights and perspectives " +
                "about how digital technologies can promote sustainability and facilitate a resilient " +
                "economy that works for the common good.");

        // Load the conference image using Picasso and handle any error with a default error image
        String imageUrl = "https://acis.aaisnet.org/acis2024/wp-content/uploads/2024/07/ACIS2024-digital-banner-e1720404776215.jpg";
        Picasso.get()
                .load(imageUrl)
                .error(R.drawable.baseline_error_24) // Optional: error image if loading fails
                .into(image);

        // Set click listener on the notification icon to navigate to the Announcement Page
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(About_Page.this, Announcement_Page.class));
            }
        });

        // Set click listener on the "unseen" button to open the Announcement Page
        unseen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(About_Page.this, Announcement_Page.class));
            }
        });

        // Initialize the navigation drawer
        navigationDrawer();
    }

    // Handle back press action to close the navigation drawer if it's open
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);  // Close the drawer if visible
        } else {
            super.onBackPressed();  // Proceed with the default back press action
        }
    }

    // Method to initialize and handle the navigation drawer
    private void navigationDrawer() {
        // Bring the navigation view to the front and set its item selected listener
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_about);  // Highlight the current page in the drawer

        // Handle menu icon click to open/close the drawer
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);  // Close drawer if visible
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);  // Open drawer if hidden
                }
            }
        });

        // Add animation to the navigation drawer
        animateNavigationDrawer();
    }

    // Method to animate the navigation drawer opening and closing
    private void animateNavigationDrawer() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Apply scaling and translation effect on the content view as the drawer slides
                final float diffScaleOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaleOffset;
                contentView.setScaleX(offsetScale);  // Scale X-axis
                contentView.setScaleY(offsetScale);  // Scale Y-axis

                // Translate content view to the right as the drawer opens
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaleOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }
        });
    }

    // Handle item selection in the navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Navigate to the appropriate activity based on the selected item
        if (item.toString().equals("Home")) {
            startActivity(new Intent(About_Page.this, Home_Page.class));
        }
        if (item.toString().equals("Sessions")) {
            startActivity(new Intent(About_Page.this, Session_Page.class));
        }
        if (item.toString().equals("QR Check-In")) {
            startActivity(new Intent(About_Page.this, QR_check_in.class));
        }
        if (item.toString().equals("Site Map")) {
            startActivity(new Intent(About_Page.this, Site_Map_Page.class));
        }
        if (item.toString().equals("Committee")) {
            startActivity(new Intent(About_Page.this, Organising_Committee_Page.class));
        }
        if (item.toString().equals("Chat")) {
            startActivity(new Intent(About_Page.this, Group_Chat_Page.class));
        }
        if (item.toString().equals("Sign Out")) {
            try {
                // Clear stored access token
                SharedPreferences sharedPreferences = getSharedPreferences("YourAppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(userProfile.getToken());
                editor.apply();

                // Clear any other user data
                UserProfile.getInstance().clearUserProfile(); // Implement this method to clear user profile data

                // Redirect user to the login screen or homepage
                Intent intent = new Intent(About_Page.this, Sign_In_Page.class); // Change to your login activity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } catch (Exception ignored){}

            try {
                gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                        startActivity(new Intent(About_Page.this, Sign_In_Page.class));
                    }
                });
            } catch (Exception ignored){}
        }
        return true;  // Indicate that the item selection has been handled
    }
}