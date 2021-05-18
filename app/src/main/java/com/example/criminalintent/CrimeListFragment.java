package com.example.criminalintent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.List;

public class CrimeListFragment extends Fragment {

    //region Declarations
    private static final String SAVE_SUBTITLE = "save_subtitle";
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFloatingActionButton;
    private Button mNoCrimeButton;
    private TextView mNoCrimeTextView;
    private Crime mNewCrime;
    private CrimeAdapter mAdapter;
    private Callbacks mCallbacks;
    private List<Crime> mCrimes = CrimeLab.get(getActivity()).getCrimes();
    private int itemChangedposition;
    private boolean firstTime = true;
    private Context mContext;
    private Comparator<Crime> mComparator;
    //endregion

    public interface Callbacks{
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.crime_search_menu:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    SearchView searchView = (SearchView) item.getActionView();
                    searchView.setQueryHint(getString(R.string.crime_search_hint));
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return false;
                        }
                    });
                    searchView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        //region RV and FAB
        mRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.crime_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewCrime = new Crime();
                CrimeLab.get(getActivity()).addCrime(mNewCrime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), mNewCrime.getId());
                startActivity(intent);
            }
        });
        //endregion
        //region EmptyRecyclerView
        if(CrimeLab.get(getActivity()).getCrimes().size()==0){
            View noCrimeView = inflater.inflate(R.layout.empty_list_page, container, false);
            mNoCrimeTextView = (TextView) noCrimeView.findViewById(R.id.no_crime_text);
            mNoCrimeButton = (Button) noCrimeView.findViewById(R.id.no_crime_button);
            mNoCrimeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View noCrimeView) {
                    mNewCrime = new Crime();
                    CrimeLab.get(getActivity()).addCrime(mNewCrime);
                    Intent intent = CrimePagerActivity.newIntent(getActivity(), mNewCrime.getId());
                    startActivity(intent);
                }
            });
            return noCrimeView;
        }
        //endregion
        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(CrimeLab.get(getActivity()).getCrimes().size()!=0 && firstTime) {
            CrimeListFragment fragment = new CrimeListFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        updateUI();
    }
    
    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //region updateUI (Adapter<->RecyclerView)
    private void updateUI() {

        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if(mAdapter == null && crimes.size()!=0) {
            firstTime = false;
            mAdapter = new CrimeAdapter(crimes);
            mRecyclerView.setAdapter(mAdapter);
        }
        else {
            if (crimes.size() != 0) {
                mAdapter.setCrimes(crimes);
                mAdapter.notifyItemChanged(itemChangedposition);
            }
        }
        updateSubtitle();
    }
    //endregion

    //region CrimeHolder (our ViewHolder)
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //region Declarations
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;
        //endregion

        // region CrimeHolder constructor
        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.list_item_crime, parent, false));
                itemView.setOnClickListener(this);

                mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
                mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
                mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved_preview);
        }
        //endregion

        //region bind
        public void bind(Crime crime){
            mCrime = crime;
            String solvedText = mCrime.isSolved() ? "Solved" : "Not Solved";
            mTitleTextView.setText(mCrime.getTitle() + ": " + solvedText);
            mDateTextView.setText("Noticed on: " + DateFormat.getDateInstance(DateFormat.FULL).format(mCrime.getDate()));
            if (mCrime.isSolved())
                mSolvedImageView.setImageResource(R.drawable.solved_preview);
            else
                mSolvedImageView.setImageResource(R.drawable.not_solved_preview);
        }
        //endregion
        //region onClick
        @Override
        public void onClick(View v) {
            //region notifyItemChanged position
            int i = 0;
            for (Crime crime: CrimeLab.get(getActivity()).getCrimes()){
                if(crime.getId().equals(mCrime.getId())) {
                    itemChangedposition = i;
                    break;
                }
                i++;
            }
            //endregion
            mCallbacks.onCrimeSelected(mCrime);
        }
        //endregion
    }
    //endregion
    //region SortedList
    private final SortedList.Callback<Crime> mCallback = new SortedList.Callback<Crime>() {
        @Override
        public void onInserted(int position, int count) {
            mAdapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            mAdapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            mAdapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            mAdapter.notifyItemRangeChanged(position, count);
        }

        @Override
        public int compare(Crime a, Crime b) {
            return mComparator.compare(a, b);
        }

        @Override
        public boolean areContentsTheSame(Crime oldItem, Crime newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Crime item1, Crime item2) {
            return item1.getTitle().equals(item2.getTitle());
        }
    };
    //endregion
    final SortedList<Crime> list = new SortedList<>(Crime.class, mCallback);
    //region CrimeAdapter (our CrimeAdapter)
    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{

        private List<Crime> mCrimes;

        private final SortedList<Crime> mSortedList = new SortedList<>(Crime.class, new SortedList.Callback<Crime>() {
            @Override
            public int compare(Crime a, Crime b) {
                return mComparator.compare(a, b);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Crime oldItem, Crime newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(Crime item1, Crime item2) {
                return item1.getId() == item2.getId();
            }
        });
        
        // region CrimeAdapter constructor
        public CrimeAdapter(List<Crime> crimes){
            mCrimes = crimes;
        }
        //endregion
        //region onCreateViewHolder
        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }
        //endregion
        //region onBindViewHolder
        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            if(crime.getTitle()==null)
                CrimeLab.get(getActivity()).deleteCrime(crime);
            else
                holder.bind(crime);
        }
        //endregion
        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }

        public void add(Crime model) {
            mSortedList.add(model);
        }

        public void remove(Crime model) {
            mSortedList.remove(model);
        }

        public void add(List<Crime> models) {
            mSortedList.addAll(models);
        }

        public void remove(List<Crime> models) {
            mSortedList.beginBatchedUpdates();
            for (Crime model : models) {
                mSortedList.remove(model);
            }
            mSortedList.endBatchedUpdates();
        }

        public void replaceAll(List<Crime> models) {
            mSortedList.beginBatchedUpdates();
            for (int i = mSortedList.size() - 1; i >= 0; i--) {
                final Crime model = mSortedList.get(i);
                if (!models.contains(model)) {
                    mSortedList.remove(model);
                }
            }
            mSortedList.addAll(models);
            mSortedList.endBatchedUpdates();
        }

    }
    //endregion

}
