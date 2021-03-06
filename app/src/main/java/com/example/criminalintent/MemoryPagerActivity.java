package com.example.criminalintent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import java.util.List;
import java.util.UUID;

public class MemoryPagerActivity extends AppCompatActivity {

    //region Declarations
    private static final String EXTRA_memory_ID = "com.example.criminalintent.memory_id";
    private static ViewPager mViewPager;
    private List<Memory> mMemories;
    //endregion

    public static Intent newIntent(Context packageContext, UUID memoryId){
        Intent intent = new Intent(packageContext, MemoryPagerActivity.class);
        intent.putExtra(EXTRA_memory_ID, memoryId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_pager);

        mViewPager = (ViewPager) findViewById(R.id.memory_view_pager);
        mMemories = MemoryLab.get(this).getMemories();
        FragmentManager fragmentManager = getSupportFragmentManager();

        //region setAdapter
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager){
            @NonNull
            @Override
            public Fragment getItem(int position) {
                Memory memory = mMemories.get(position);
                return MemoryFragment.newInstance(memory.getId());
            }
            @Override
            public int getCount() {
                return mMemories.size();
            }
        });
        //endregion
        getCurrentPosition();
    }

    public void getCurrentPosition() {
        UUID memoryId = (UUID) getIntent().getSerializableExtra(EXTRA_memory_ID);
        for (int i = 0; i < mMemories.size(); i++) {
            if (mMemories.get(i).getId().equals(memoryId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
    public static void getCurrentPosition(int i){
            mViewPager.setCurrentItem(i);
    }
}
