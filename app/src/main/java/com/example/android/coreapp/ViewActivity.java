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
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ViewActivity extends AppCompatActivity implements FragmentCore.SendReference {

    TabAdapter adapter;
    Fragment fr;
    ImageView corePic;
    ImageView corePicHist;
    Drawable coreLoad, coreLoadHist = null;

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

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment.getClass() == FragmentHistogram.class) {
            fr = fragment;
        }
    }

    private void loadImage(ImageView v) throws IOException {
        ((FragmentHistogram) fr).starter(v);
        //((FragmentHistogram) fr).resetHist();
    }

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

                    coreLoadHist = coreLoad.getConstantState().newDrawable();
                    corePicHist = findViewById(R.id.hist_img);
                    corePic = findViewById(R.id.view_core);
                    corePic.setImageDrawable(coreLoad);
                    corePicHist.setImageDrawable(coreLoadHist);

                    if(coreLoad!=null)
                    {
                        try {
                            loadImage(corePic);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
        }
    }

    @Override
    public void sendReferenceList(List<Double> reference) {
        String tag = "android:switcher:" + R.id.view_pager + ":" + 1;
        FragmentHistogram f = (FragmentHistogram) getSupportFragmentManager().findFragmentByTag(tag);
        f.ReceiveList(reference);
    }

    @Override
    public void sendReferenceDouble(double reference) {
        String tag = "android:switcher:" + R.id.view_pager + ":" + 1;
        FragmentHistogram f = (FragmentHistogram) getSupportFragmentManager().findFragmentByTag(tag);
        f.ReceiveDouble(reference);
    }

    public void setText(final TextView text, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }
}