package com.paw.pawxy_avinaash;

import static android.content.Context.DOWNLOAD_SERVICE;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment {
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 1;


    ActivityResultLauncher<Intent> activityResultLauncher;
    Uri uri;
    Bundle bundle;
    TextInputEditText linktext;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StartFragment newInstance(String param1, String param2) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_start, container, false);


        linktext = v.findViewById(R.id.link);





        linktext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linktext.setCursorVisible(true);

            }
        });

        linktext.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                linktext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_clear_24, 0);
                linktext.setCursorVisible(false);
                if(linktext.getText().toString().trim().length()==0){
                    linktext.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(linktext.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        MaterialButton down = v.findViewById(R.id.download_btn);
        AutoCompleteTextView folder = v.findViewById(R.id.dest);
        CardView cardv = v.findViewById(R.id.card);

        NavHostFragment navHostFragment =
                (NavHostFragment) requireActivity().getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;






        down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(linktext.getText().toString().trim().length()==0){
                    linktext.setError("Enter valid Link");
                }
                else if(folder.getText().toString().trim().length()==0){
                    folder.setError("Choose a Folder");
                }

                else{


                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {

                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    down.setVisibility(View.GONE);
                                    cardv.setVisibility(View.VISIBLE);
                                }
                            });

                            try{
                                if (! Python.isStarted()) {
                                    Python.start(new AndroidPlatform(requireActivity()));
                                }

                                Python py = Python.getInstance();
                                PyObject pyobj = py.getModule("myscript");
                                List<PyObject> obj = pyobj.callAttr("main",linktext.getText().toString().trim()).asList();
                                Bundle bundle = new Bundle();
                                bundle.putString("likes",obj.get(0).toString());
                                bundle.putString("views",obj.get(1).toString());
                                bundle.putString("thumbnail",obj.get(2).toString());
                                bundle.putString("title",obj.get(3).toString());
                                bundle.putString("totalsize",obj.get(4).toString());
                                bundle.putString("vid_duration",obj.get(5).toString());
                                bundle.putString("link",linktext.getText().toString().trim());
                                bundle.putString("path",getFolderPath(uri));

                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Navigation.findNavController(view).navigate(R.id.downloadFragment, bundle);
                                    }
                                });



                            }

                            catch (Exception e){
                                Log.i("Error",e.getMessage());
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Toast.makeText(requireActivity(), "Failed to grab video info", Toast.LENGTH_SHORT).show();
                                        down.setVisibility(View.VISIBLE);
                                        cardv.setVisibility(View.GONE);
                                    }
                                });

                            }
                        }


                    });


                }

            }
        });


        folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                // Optionally, you can set the initial directory to the SD card
                Uri sdCardUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A");
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, sdCardUri);

                activityResultLauncher.launch(intent);
            }
        });


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()== Activity.RESULT_OK){
                    Intent intent = result.getData();
                    assert intent != null;
                    uri = intent.getData();
                    String outputfolder = uri.getLastPathSegment();
                    String[] parts = uri.getLastPathSegment().split(":");
                    if (parts.length > 1) {
                         outputfolder = parts[1];

                    }
                    folder.setText(outputfolder);
                }
            }
        });







        return  v;
    }

    private String getFolderPath(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(requireActivity(), uri);
        String filePath = null;

        if (documentFile != null) {

            String documentId = DocumentsContract.getTreeDocumentId(uri);


            Uri folderUri = DocumentsContract.buildDocumentUriUsingTree(uri, documentId);


            Cursor cursor = requireActivity().getContentResolver().query(folderUri, new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                String folderName = cursor.getString(nameIndex);
                filePath = Environment.getExternalStorageDirectory() + "/" + folderName;
                cursor.close();
            }
        }

        return filePath;
    }




}