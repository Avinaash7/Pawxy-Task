package com.paw.pawxy_avinaash;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DownloadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */



public class DownloadFragment extends Fragment {

    private DownloadProgressListener downloadProgressListener;
    private long videoDurationInSeconds; // Duration of the video in seconds

    Boolean success = false;
    String filsize;
    private boolean conversionInProgress; // Flag to track if conversion is in progress
    TextView downsize;

    String source;
    String uniqueTitle;

    final Executor exeCUTOR;


    LinearProgressIndicator progressbar;
    TextView distext;
    String videoduration;
    TextView totsizetv;
    TextView resultt;
    MaterialButton bottombtn;
    ImageView finalsym;





    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DownloadFragment() {
        exeCUTOR = Executors.newSingleThreadExecutor();
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(requireActivity()));
        }
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DownloadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DownloadFragment newInstance(String param1, String param2) {
        DownloadFragment fragment = new DownloadFragment();
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




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_download, container, false);

         progressbar = v.findViewById(R.id.pb);

        TextView tv = v.findViewById(R.id.title_name);
        TextView likes = v.findViewById(R.id.likes_tv);
        TextView views = v.findViewById(R.id.views_tv);
        ImageView img = v.findViewById(R.id.imagev);
        bottombtn = v.findViewById(R.id.bottom_btn);
         totsizetv = v.findViewById(R.id.total_size);
        downsize = v.findViewById(R.id.downloaded_size);
        resultt = v.findViewById(R.id.result);
        finalsym = v.findViewById(R.id.final_symbol);

        filsize = getArguments().getString("totalsize");
        long sizeinbytes = Long.parseLong(filsize);
        double fileSizeInMB = bytesToMB(sizeinbytes);
        DecimalFormat df = new DecimalFormat("#.##");
        double roundedNumber = Double.parseDouble(df.format(fileSizeInMB));


        totsizetv.setText("/ "+roundedNumber+"MB");



        distext = v.findViewById(R.id.display_text);

        assert getArguments() != null;
        Picasso.get().load(getArguments().getString("thumbnail")).into(img);
        tv.setText(getArguments().getString("title"));
        String likesM = formatViews(getArguments().getString("likes"));
        String viewsM = formatViews(getArguments().getString("views"));
        likes.setText(likesM);
        views.setText(viewsM);

         videoduration = getArguments().getString("vid_duration");
        videoDurationInSeconds = convertDurationToSeconds(videoduration);

        bottombtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.startFragment);
            }
        });






        exeCUTOR.execute(new Runnable() {
            @Override
            public void run() {

                Python py = Python.getInstance();
                PyObject pyObject = py.getModule("media");
                // Rest of your code here
                //PyObject result = pyObject.callAttr("download_media", getArguments().getString("link"));
                downloadProgressListener = new YourDownloadProgressListener2();
                PyObject result = pyObject.callAttr("download_media", getArguments().getString("link"), downloadProgressListener);
                Log.i("resultx",result.toString());

                uniqueTitle = result.toString();
                source = "/storage/emulated/0/Download/"  + uniqueTitle;

                //ffmpeg();
                convertVideoToMp3();



            }
        });






        return v;
    }

    String formatViews(String viewsString) {
        try {
            long views = Long.parseLong(viewsString);
            if (views >= 1_000_000_000) {
                return String.format("%.1fB", views / 1_000_000_000.0);
            } else if (views >= 1_000_000) {
                return String.format("%.1fM", views / 1_000_000.0);
            } else if (views >= 1_000) {
                return String.format("%.1fK", views / 1_000.0);
            } else {
                return String.valueOf(views);
            }
        } catch (NumberFormatException e) {
            // Invalid number format, handle it accordingly
            return "Invalid number format";
        }
    }

    public int convertDurationToSeconds(String duration) {
        String[] timeComponents = duration.split(":");
        int seconds = 0;

        if (timeComponents.length == 1) {
            // Duration is in seconds format
            seconds = Integer.parseInt(timeComponents[0]);
        } else if (timeComponents.length == 2) {
            // Duration is in MM:SS format
            int minutes = Integer.parseInt(timeComponents[0]);
            int secondsComponent = Integer.parseInt(timeComponents[1]);
            seconds = minutes * 60 + secondsComponent;
        } else if (timeComponents.length == 3) {
            // Duration is in H:MM:SS format
            int hours = Integer.parseInt(timeComponents[0]);
            int minutes = Integer.parseInt(timeComponents[1]);
            int secondsComponent = Integer.parseInt(timeComponents[2]);
            seconds = hours * 3600 + minutes * 60 + secondsComponent;
        }

        return seconds;
    }


    private void convertVideoToMp3() {
        String uniformizedText = uniqueTitle.replaceAll("[^\\w\\s]+", "");


        uniformizedText = uniformizedText.replaceAll("\\s+", " ");


        uniformizedText = uniformizedText.trim();
        String dest = getArguments().getString("path") + "/" + uniformizedText;
        String checkdest = getArguments().getString("path") + "/" + uniformizedText+".mp3";


        File destFile = new File(checkdest);
        int suffix = 1;
        while (destFile.exists()) {
            // Append the suffix to the filename
            dest.replace(".mp3","");
            String newFileName = uniformizedText + "(" + suffix + ")";
            dest = getArguments().getString("path") + "/" + newFileName+".mp3";
            destFile = new File(dest);
            suffix++;
        }

        String replaced = dest.replace(".mp3","");


        String command = String.format("-i \"%s\".mp4 -vn -c:a libmp3lame \"%s\".mp3", source, replaced);

        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage message) {
                Log.d(Config.TAG, message.getText());
            }
        });

        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics newStatistics) {
                float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / (1000);



                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateProgress(progress);
                    }
                });

            }
        });


        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(final long executionId, final int returnCode) {
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    // Conversion completed successfully
                    progressbar.setProgressCompat(100,true);
                    distext.setText("Success");
                    totsizetv.setVisibility(View.INVISIBLE);
                    finalsym.setImageResource(R.drawable.greentick);
                    finalsym.setVisibility(View.VISIBLE);
                    progressbar.setIndicatorColor(Color.parseColor("#72BD6C"));
                    resultt.setText("MP3 successfully saved into selected folder");
                    resultt.setVisibility(View.VISIBLE);
                    downsize.setVisibility(View.INVISIBLE);
                    bottombtn.setVisibility(View.VISIBLE);
                    Log.i(Config.TAG, "Command execution completed successfully.");
                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    // Conversion canceled
                    progressbar.setProgressCompat(100,true);
                    distext.setText("Failed");
                    downsize.setVisibility(View.INVISIBLE);
                    distext.setTextColor(Color.parseColor("#DC4A4A"));
                    totsizetv.setVisibility(View.INVISIBLE);
                    finalsym.setImageResource(R.drawable.redcross);
                    finalsym.setVisibility(View.VISIBLE);
                    resultt.setText("Please check your internet connection");
                    resultt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.failed, 0, 0, 0);
                    resultt.setVisibility(View.VISIBLE);
                    bottombtn.setVisibility(View.VISIBLE);
                    progressbar.setIndicatorColor(Color.parseColor("#DC4A4A"));
                    Log.i(Config.TAG, "Command execution canceled by user.");
                } else {
                    // Conversion failed
                    progressbar.setProgressCompat(100,true);
                    distext.setText("Failed");
                    downsize.setVisibility(View.INVISIBLE);
                    distext.setTextColor(Color.parseColor("#DC4A4A"));
                    totsizetv.setVisibility(View.INVISIBLE);
                    finalsym.setImageResource(R.drawable.redcross);
                    finalsym.setVisibility(View.VISIBLE);
                    resultt.setText("Please check your internet connection");
                    resultt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.failed, 0, 0, 0);
                    resultt.setVisibility(View.VISIBLE);
                    bottombtn.setVisibility(View.VISIBLE);
                    progressbar.setIndicatorColor(Color.parseColor("#DC4A4A"));
                    Log.i(Config.TAG, String.format("Command execution failed with returnCode=%d.", returnCode));
                }


                conversionInProgress = false;
            }
        });

        conversionInProgress = true;
    }


    private void updateProgress(float progress) {


        String formattedTime = formatTime((int) progress);
        int percent = (int) ((progress/videoDurationInSeconds)* 100)  % 100;
        int actualpercent = (int) ((progress/videoDurationInSeconds) * 100);
        if(percent<50){
            distext.setText("Converting...");
            progressbar.setIndicatorColor(Color.parseColor("#FFBB0E"));
        }
        else if(percent>50 && percent<90){
            distext.setText("Converting...");
            totsizetv.setText(videoduration);
            downsize.setText(formattedTime+" / ");
            progressbar.setProgressCompat(percent,true);
            progressbar.setIndicatorColor(Color.parseColor("#FFBB0E"));
        }
        else if(percent>90){
            distext.setText("Saving...");
            downsize.setVisibility(View.INVISIBLE);
            totsizetv.setText(actualpercent + "%");
            progressbar.setProgressCompat(percent,true);
            progressbar.setIndicatorColor(Color.parseColor("#28C6D0"));
        }



    }

    private String formatTime(int timeInSeconds) {
        int hours = timeInSeconds / 3600;
        int minutes = (timeInSeconds % 3600) / 60;
        int seconds = timeInSeconds % 60;

        // Format the time as HH:MM:SS
        String result = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        if(result.charAt(0) == '0' && result.charAt(1) == '0'){
             result = result.substring(3);
        }


        return result;
    }



    double bytesToMB(long bytes) {
        return (double) bytes / (1024 * 1024);
    }


    private class YourDownloadProgressListener2 implements DownloadProgressListener {
        @Override
        public void updateProgress(String percent, String totalBytes) {
            // Update the progress bar on the UI thread
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    float progressValue = Float.parseFloat(percent.replace("%", "").trim());

                    long sizeinbytes = Long.parseLong(filsize);
                    double fileSizeInMB = bytesToMB(sizeinbytes);
                    DecimalFormat df = new DecimalFormat("#.##");
                    double downloadedize = ((progressValue/100) * fileSizeInMB);
                    double roundedNumber = Double.parseDouble(df.format(downloadedize));
                    int val = Math.round(progressValue/2);
                    progressbar.setProgressCompat(val,true);
                    downsize.setText(String.valueOf(roundedNumber)+"MB ");
                }
            });
        }
    }







    }

