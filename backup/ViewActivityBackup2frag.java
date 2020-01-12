package com.example.android.coreapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ViewActivity extends AppCompatActivity {

    TabAdapter adapter;
    Fragment fr;
    boolean flag;
    ImageView corePic;
    Drawable coreLoad = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);

        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tabs);

        adapter = new TabAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_core, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_load_image:
                Intent it = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(it, 101);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onAttachFragment(Fragment fragment) {
//        super.onAttachFragment(fragment);
//        if (fragment.getClass() == FragmentHistogram.class) {
//            fr = fragment;
//        }
//    }
//
//  useful stuff
//    private void loadImage(ImageView v, boolean ye) throws IOException {
//        ye = true;
//        ((FragmentHistogram) fr).reseter(v, ye);
//    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {

            case 101:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                        coreLoad = Drawable.createFromStream(inputStream, selectedImage.toString() );
                    } catch (FileNotFoundException e) {
                        coreLoad = ContextCompat.getDrawable(this, R.drawable.ic_checkered_back);
                    }
                    corePic = findViewById(R.id.view_core);
                    corePic.setImageDrawable(coreLoad);
                }
        }
    }
}