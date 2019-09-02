package com.crossbox.brushup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private String CHANNEL_ID = "quotistic";
    private NotificationManager manager;
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence name = "quotistic_goodmorning";
            String desc = "quotistic_morning_notif";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,name,importance);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    ArrayList<String> quotesList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        // todo:  Experimental Code for Timed Notifications---------------------
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_new)
                .setContentTitle("Good Morning!")
                .setContentText("Start your day with a huge smile :D")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Start your day with a huge smile and lots of motivation!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat compat = NotificationManagerCompat.from(this);
        manager.notify(0,builder.build());
        // todo:  Experimental Code for Timed Notifications---------------------

        final TextView textView = findViewById(R.id.textview1);
        Typeface customFont2 = Typeface.createFromAsset(getAssets(),"fonts/lobster.otf");

        textView.setTypeface(customFont2);
        final MaterialButton nextButton = findViewById(R.id.nextButton);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final MaterialButton copyButton = findViewById(R.id.copyButton);

        progressBar.setVisibility(View.VISIBLE);
        nextButton.setEnabled(false);
        copyButton.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("quotes")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot doc:task.getResult()){
                            String s = doc.get("0").toString();
                            quotesList.add(s);
                        }
                        Random r = new Random();
                        textView.setText(quotesList.get(r.nextInt(quotesList.size())));
                        nextButton.setEnabled(true);
                        copyButton.setEnabled(true);
                        progressBar.setVisibility(View.INVISIBLE);
                    }else{
                        Snackbar.make(findViewById(R.id.coordinator),"Error in fetching quotes", BaseTransientBottomBar.LENGTH_LONG)
                                .show();
//
                    }
                }
            });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Random rand = new Random();

                final Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
                final Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in_main);

                textView.startAnimation(fadeOut);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(quotesList.get(rand.nextInt(quotesList.size())));
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                textView.startAnimation(fadeIn);
                            }
                        });
                    }
                },100);

            }
        });
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Quote",textView.getText().toString());
                clipboard.setPrimaryClip(clipData);
                Toast t = Toast.makeText(MainActivity.this, "Copied to clipboard!", Toast.LENGTH_SHORT);
                View toastView = t.getView();

//                toastView.setBackgroundColor(Color.WHITE);
                toastView.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                TextView text = (TextView) toastView.findViewById(android.R.id.message);
                text.setTextColor(Color.BLACK);
                text.setShadowLayer(0,0,0,Color.WHITE);
                t.show();

            }
        });
    }
}