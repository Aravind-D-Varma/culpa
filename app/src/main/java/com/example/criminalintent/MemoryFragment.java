package com.example.criminalintent;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.criminalintent.ImagePickerFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MemoryFragment extends Fragment {

    // region Declarations
    public static final String ARG_memory_ID = "memory_id";
    public static final String DIALOG_DATE = "DialogDate";
    public static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_PHOT0 = "DialogPhoto";
    public static final int REQUEST_DATE = 0;
    public static final int REQUEST_TIME = 1;
    public static final int REQUEST_CONTACT = 2;
    public static final int REQUEST_PHOTO = 3;

    private static final String[] DECLARED_CONTACT_PERMISSIONS = new String[] {Manifest.permission.READ_CONTACTS};
    private static final String[] DECLARED_PHOTO_PERMISSIONS = new String[] {Manifest.permission.CAMERA};
    private static final int MY_READ_CONTACTS_CODE = 100;
    private static final int MY_CAMERA_CODE = 101;

    private String mSuspectNumber;
    private Memory mMemory;
    private List<Memory> mMemories;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private TextInputLayout mTextInputLayout;
    private TextInputEditText mTitleField;
    private EditText mDetailField;
    private Button mDateButton;
    private Button mSendReportButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private static Button mFirstmemory;
    private static Button mLastmemory;

    private File mPhotoFile;
    private String mSuspectId;
    public int thumbnailWidth, thumbnailHeight;
    //endregion

    //region Fragment+Arguments
    public static MemoryFragment newInstance(UUID memoryId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_memory_ID, memoryId);
        MemoryFragment fragment = new MemoryFragment();
        fragment.setArguments(args);

        return fragment;
    }
    //endregion

    //region OverRidden methods

    //region onCreate
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID memoryId = (UUID) getArguments().getSerializable(ARG_memory_ID);
        mMemory = MemoryLab.get(getActivity()).getMemory(memoryId);
        mPhotoFile = MemoryLab.get(getActivity()).getPhotoFile(mMemory);
        setHasOptionsMenu(true);
    }
    //endregion

    // region Create and select from Menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_memory, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.delete_memory:
                //memoryLab.get(getActivity()).getmemorys().remove(mMemory);
                MemoryLab.get(getActivity()).deleteMemory(mMemory);
                Intent intent = new Intent(getActivity(), MemoryListActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
    //endregion

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_memory, container, false);
        mMemories = MemoryLab.get(getActivity()).getMemories();
        // region EditText
        mTextInputLayout = (TextInputLayout) v.findViewById(R.id.memory_text_input_layout);
        mTitleField = (TextInputEditText) v.findViewById(R.id.memory_title);
        mTitleField.setText(mMemory.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMemory.setTitle(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
                mTextInputLayout.setError(null);
            }
        });
        mTitleField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(mTitleField.getText().toString().length()==0) {
                        mTextInputLayout.setError("Please enter a title or discard this memory");
                        mTitleField.getBackground().clearColorFilter();
                    }
                    else
                        mTextInputLayout.setError(null);
                }
            }
        });
        //endregion
        //region EditText Details
        mDetailField = (EditText) v.findViewById(R.id.memory_details);
        mDetailField.setText(mMemory.getDetail());
        mDetailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMemory.setDetail(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mDetailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(mDetailField.getText().toString().length()==0){
                        mDetailField.setError("It is preferred to write some details of the memory");
                    }
                    else
                        mDetailField.setError(null);
                }
            }
        });
        //endregion
        //region DateButton
        mDateButton = (Button) v.findViewById(R.id.memory_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dp = DatePickerFragment.newInstance(mMemory.getDate());
                dp.setTargetFragment(MemoryFragment.this, REQUEST_DATE);
                dp.show(manager, DIALOG_DATE);
            }
        });
        //endregion
        // region TimeButton
        mTimeButton = (Button) v.findViewById(R.id.memory_time);
        updateTime(mMemory.getDate());
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                TimePickerFragment tp = TimePickerFragment.newInstance(mMemory.getDate());
                tp.setTargetFragment(MemoryFragment.this, REQUEST_TIME);
                tp.show(fm, DIALOG_TIME);
            }
        });
        //endregion
        //region SolvedCheckbox
        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.memory_solved);
        mSolvedCheckBox.setChecked(mMemory.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMemory.setSolved(isChecked);
            }
        });
        //endregion
        //region First and Last Button
        mFirstmemory = (Button) v.findViewById(R.id.first_memory);
        mLastmemory = (Button) v.findViewById(R.id.last_memory);
        mFirstmemory.setText("First memory");
        mLastmemory.setText("Last memory");
        if ( mMemory.getId().equals( mMemories.get(0).getId() ) )
            mFirstmemory.setVisibility(View.GONE);
        else {
            mFirstmemory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MemoryPagerActivity.getCurrentPosition(0);
                }
            });
        }
        if( mMemory.getId().equals( mMemories.get(mMemories.size()-1).getId() ) )
            mLastmemory.setVisibility(View.GONE);
        else {
            mLastmemory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MemoryPagerActivity.getCurrentPosition(MemoryLab.get(getActivity()).getMemories().size() - 1);
                }
            });
        }
        //endregion
        //region SendReport Button
        mSendReportButton = (Button) v.findViewById(R.id.memory_report);
        mSendReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = ShareCompat.IntentBuilder.from(getActivity()).setType("text/plain")
                        .setChooserTitle(getString(R.string.send_report))
                        .setSubject(getString(R.string.memory_report_subject))
                        .setText(getmemoryReport())
                        .createChooserIntent();
                startActivity(intent);
            }
        });
        //endregion
        //region SuspectButton
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.memory_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasContactPermission()) {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
                else {
                    requestPermissions(DECLARED_CONTACT_PERMISSIONS, MY_READ_CONTACTS_CODE);
                    if(hasContactPermission())
                        startActivityForResult(pickContact, REQUEST_CONTACT);
                }


            }
        });
        if(mMemory.getSuspect()!=null)
            mSuspectButton.setText("Suspect: "+mMemory.getSuspect());
        else
            mSuspectButton.setText("Choose Suspect");

        //endregion
        //region CallButton
        mCallButton = (Button) v.findViewById(R.id.call_suspect);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri number = Uri.parse("tel:"+mMemory.getNumber());
                Intent callContact = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callContact);
            }
        });
        //endregion
        PackageManager pM = getActivity().getPackageManager();
        //region PhotoButton
        mPhotoButton = (ImageButton)v.findViewById(R.id.memory_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasPhotoPermission()) {
                    takePhoto(captureImage);
                }
                else{
                    requestPermissions(DECLARED_PHOTO_PERMISSIONS, MY_CAMERA_CODE);
                    if(hasPhotoPermission()){
                        takePhoto(captureImage);
                    }
                }
            }
        });
        //endregion
        //region PhotoView
        mPhotoView = (ImageView)v.findViewById(R.id.memory_photo);
        mPhotoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                thumbnailHeight = mPhotoView.getHeight();
                thumbnailWidth =  mPhotoView.getWidth();
                updatePhotoView(thumbnailHeight, thumbnailWidth);
            }
        });
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                ImagePickerFragment iP = ImagePickerFragment.getInstance(PictureUtils.getScaledBitMap(mPhotoFile.getPath(), getActivity()));
                iP.setTargetFragment(MemoryFragment.this, REQUEST_PHOTO);
                iP.show(fragmentManager, DIALOG_PHOT0);
            }
        });
        //endregion
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode!= Activity.RESULT_OK)
            return;

        if(requestCode == REQUEST_DATE){
           Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mMemory.setDate(date);
            updateDate();
        }
        else if (requestCode == REQUEST_TIME){
            Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mMemory.setDate(time);
            updateTime(time);
        }
        else if (requestCode == REQUEST_CONTACT && data!=null){
            getSuspectName(data);
            getSuspectNunber();
        }
        else if (requestCode == REQUEST_PHOTO){
            Uri uri = FileProvider.getUriForFile(getActivity(),"com.example.criminalintent.fileprovider",mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView(thumbnailHeight, thumbnailWidth);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mTitleField.setError(null);
        if(mMemory.getTitle() == null)
            MemoryLab.get(getActivity()).deleteMemory(mMemory);
        MemoryLab.get(getActivity()).updateMemory(mMemory);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode) {
            case MY_READ_CONTACTS_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            case MY_CAMERA_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    //endregion

    //region User-defined methods
    private void updatePhotoView(float destHeight, float destWidth) {
        if(mPhotoFile == null || !mPhotoFile.exists())
            mPhotoView.setImageDrawable(null);
        else {
            Bitmap definedBitMap = PictureUtils.getScaledBitMap(mPhotoFile.getPath(), (int) destHeight, (int) destWidth);
            mPhotoView.setImageBitmap(definedBitMap);
        }
    }
    private void takePhoto(Intent captureImage) {
        Uri uri = FileProvider.getUriForFile(getActivity()
                , "com.example.criminalintent.fileprovider", mPhotoFile);
        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        List<ResolveInfo> cameraActivities = getActivity().getPackageManager()
                .queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo activity : cameraActivities) {
            getActivity().grantUriPermission(activity.activityInfo.packageName
                    , uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        startActivityForResult(captureImage, REQUEST_PHOTO);
    }
    private void getSuspectNunber() {
        Uri callNumberURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] queryFieldsNumber = {ContactsContract.Data.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selectionClause = ContactsContract.Data.CONTACT_ID + "= ?";
        String selectionArgs[] = {""};
        selectionArgs[0] = mSuspectId;
        Cursor numberCursor = getActivity().getContentResolver()
                .query(callNumberURI, queryFieldsNumber, selectionClause, selectionArgs, null);
        try{
            if(numberCursor.getCount()==0)
                return;
            numberCursor.moveToFirst();
            mMemory.setNumber( numberCursor.getString(1));
        }
        finally {
            numberCursor.close();
        }
    }
    private boolean hasContactPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), DECLARED_CONTACT_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    private boolean hasPhotoPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), DECLARED_PHOTO_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    //region update Date and time
    private void updateDate() {
        mDateButton.setText(DateFormat.getDateInstance(DateFormat.FULL).format(mMemory.getDate()));
    }
    private void updateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        mTimeButton.setText(sdf.format(date));
    }
    //endregion
    private String getmemoryReport(){
        String solvedString;
        if(mMemory.isSolved())
            solvedString = getString(R.string.memory_solved_label);
        else
            solvedString = getString(R.string.memory_report_unsolved);

        String dateString = DateFormat.getDateInstance(DateFormat.FULL).format(mMemory.getDate());

        String suspect = mMemory.getSuspect();
        if(suspect!=null)
            suspect = getString(R.string.memory_report_suspect, mMemory.getSuspect());
        else
            suspect = getString(R.string.memory_report_no_suspect);

        return getString(R.string.memory_report, mMemory.getTitle(), dateString, solvedString, suspect);
    }
    private void getSuspectName( Intent data) {
        Uri contactURI = data.getData();
        String[] queryFields = new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        Cursor c = getActivity().getContentResolver().query(contactURI, queryFields, null, null, null);

        try{
            if(c.getCount()==0)
                return;
            c.moveToFirst();
            mSuspectId = c.getString(0);
            mMemory.setSuspect(c.getString(1));
            mSuspectButton.setText("Suspect: "+mMemory.getSuspect());
        }
        finally {
            c.close();
        }
        return;
    }
    //endregion
}