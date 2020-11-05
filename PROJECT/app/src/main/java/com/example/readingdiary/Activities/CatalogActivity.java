package com.example.readingdiary.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.readingdiary.Classes.Directory;
import com.example.readingdiary.Classes.Note;
import com.example.readingdiary.Classes.RealNote;
import com.example.readingdiary.Fragments.CatalogFragment;
import com.example.readingdiary.Fragments.FilterDialogFragment;
import com.example.readingdiary.Fragments.SortDialogFragment;
import com.example.readingdiary.R;
import com.example.readingdiary.adapters.CatalogButtonAdapter;
import com.example.readingdiary.adapters.RecyclerViewAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class CatalogActivity extends AppCompatActivity implements SortDialogFragment.SortDialogListener, FilterDialogFragment.FilterDialogListener, CatalogFragment.OnCatalogFragmentListener {
    public DrawerLayout drawerLayout;
    Fragment fragment;
    MaterialToolbar toolbar;
    TextView counterText;
    public boolean action_mode = false;
    int count=0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String user = "user0";
    int active=0;
    String userID;
    LinearLayoutManager layoutManager;
    private AppBarConfiguration mAppBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // Кнопка добавление новой активности
        Toolbar toolbar = findViewById(R.id.toolbar_navigation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_catalog,
                R.id.nav_genres, R.id.nav_change_password, R.id.nav_log_out, R.id.nav_delete_account)
                .setDrawerLayout(drawerLayout)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);

        View headerView = navigationView.getHeaderView(0);
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            ((TextView)headerView.findViewById(R.id.textView)).setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }


    }




        @Override
    public void changeFragment(Fragment fragment) {
        this.fragment = fragment;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_catalog, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (fragment.toString().equals("catalog")){
            if (item.getItemId()== R.id.item_delete){
                ((CatalogFragment)fragment).deleteClick();
            }
            if (item.getItemId() == R.id.item_search){
                ((CatalogFragment)fragment).searchCLick1();

            }
            if (item.getItemId() == R.id.item_search1){
                ((CatalogFragment)fragment).searchClick2();
            }

            if (item.getItemId() == R.id.item_sort){
                ((CatalogFragment)fragment).sortClick();
            }

            if (item.getItemId()==R.id.item_filter){
                ((CatalogFragment)fragment).filterClick();

            }

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSortClick(int position) {
        if (fragment.toString().equals("catalog")){
            ((CatalogFragment)fragment).onSortClick(position);
        }
    }

    @Override
    public void onFilterClick(ArrayList<String> checkedAuthors, ArrayList<String> checkedGenres, ArrayList<String> checkedGenresID, String ratingStart, String ratingEnd, boolean showCatalog) {
        if (fragment.toString().equals("catalog")){
            ((CatalogFragment)fragment).onFilterClick(checkedAuthors, checkedGenres, checkedGenresID, ratingStart, ratingEnd, showCatalog);
        }

    }



    int rep =0;
    @Override
    public void onBackPressed()
    {

        for (int i=0;i<1;i++)
        {
            if (rep<3)
            {
                rep++;

                if(rep==1)
                {
                    Toast.makeText(CatalogActivity.this, "Для выхода из приложения нажмите ещё раз ", Toast.LENGTH_SHORT).show();
                    CountDownTimer mCount=new CountDownTimer(2000,1000)
                    {
                        @Override
                        public void onTick(long millisUntilFinished)
                        {

                        }

                        @Override
                        public void onFinish()
                        {

                            rep--;

                        }
                    }.start();


                }
                else if (rep==2)
                {
                    ext();
                    rep=0;
                }

            }
        }
    }

    private void ext()
    {
        moveTaskToBack(true);
        super.onDestroy();
        System.exit(0);

    }

}

