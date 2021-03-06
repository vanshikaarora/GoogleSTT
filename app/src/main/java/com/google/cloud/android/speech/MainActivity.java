/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.android.speech;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.cloud.android.speech.Realm.Model;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;


public class MainActivity extends AppCompatActivity implements MessageDialogFragment.Listener {

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final String STATE_RESULTS = "results";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static boolean listening = false;
    private static boolean showFirebase = false;
    private SpeechService mSpeechService;
    private ImageView mic;
    private Realm realm;
    private RecyclerView firebaseRecyclerView;
    private LinearLayoutManager firebaseLinearLayoutManager;
    private FirebaseRecyclerAdapter firebaseAdapter;
    private VoiceRecorder mVoiceRecorder;
    // Resource caches
    private int mColorHearing;
    private int mColorNotHearing;
    // View references
    private TextView mStatus;
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            showStatus(true);
            if (mSpeechService != null && listening) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null && listening) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            showStatus(false);
            if (mSpeechService != null && listening) {
                mSpeechService.finishRecognizing();
            }
        }

    };
    private ConstraintLayout emptyLayout;
    private TextView mText;
    private String recentSpokenText;
    private ResultAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (mText != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    //mText.setText(null);
                                    addToDatabase(text);
                                    showSearchResults();
                                    recentSpokenText = text;

                                } else {
                                    mText.setText(text);
                                    addToDatabase(text);
                                }
                            }
                        });
                    }
                }
            };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            mStatus.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };

    /*private void createDatabase() {
        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("tasky.realm")
                .schemaVersion(0)
                .build();
        Realm.setDefaultConfiguration(realmConfig);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Resources resources = getResources();
        final Resources.Theme theme = getTheme();
        mColorHearing = ResourcesCompat.getColor(resources, R.color.status_hearing, theme);
        mColorNotHearing = ResourcesCompat.getColor(resources, R.color.status_not_hearing, theme);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mStatus = (TextView) findViewById(R.id.status);
        mText = (TextView) findViewById(R.id.text);
        emptyLayout = findViewById(R.id.emptyLayout);

        FirebaseApp.initializeApp(this);
        firebaseRecyclerView = (RecyclerView) findViewById(R.id.firebaseList);
        firebaseLinearLayoutManager = new LinearLayoutManager(this);
        firebaseRecyclerView.setLayoutManager(firebaseLinearLayoutManager);
        firebaseRecyclerView.setHasFixedSize(true);

        Realm.init(this);
        realm = Realm.getDefaultInstance();
        mic = (ImageView) findViewById(R.id.mic);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listening) {
                    listening = false;
                    stopVoiceRecorder();
                    Log.v("line188", "not listening");
                    mStatus.setText("Press Mic to hear again");
                    mic.setImageDrawable(getDrawable(R.drawable.ic_mic_black_24dp));
                } else {
                    startVoiceRecorder();
                    listening = true;
                    mStatus.setText("Listening...");
                    Log.v("line195", "listening");
                    mic.setImageDrawable(getDrawable(R.drawable.ic_mic_red_24dp));
                }
            }
        });
        //startVoiceRecorder();


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ArrayList<String> results = savedInstanceState == null ? null :
                savedInstanceState.getStringArrayList(STATE_RESULTS);
        if (results != null) {
            String result = "";
            for (String s : results) {
                result = result + " " + s;
            }
            addToDatabase(result);
            mAdapter = new ResultAdapter(results);
        }
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("msgs");
        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(query, new SnapshotParser<String>() {
                            @NonNull
                            @Override
                            public String parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return snapshot.getValue().toString();
                            }
                        })
                        .build();
        firebaseAdapter = new FirebaseRecyclerAdapter<String, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull String model) {
                firebaseRecyclerView.setVisibility(View.VISIBLE);
                holder.text.setText(model);
            }


            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
            }
        };
        firebaseRecyclerView.setAdapter(firebaseAdapter);


    }

    private void addToDatabase(final String result) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                Model object = bgRealm.createObject(Model.class);
                object.setSavedText(result);
                new AddDataAsync().execute(result);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.v("line193", "success");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.v("line198", error.getLocalizedMessage());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAdapter.startListening();

        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();
        firebaseAdapter.stopListening();

        // Stop Cloud Speech API
        if (mSpeechService != null)
            mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            outState.putStringArrayList(STATE_RESULTS, mAdapter.getResults());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_file:
                mSpeechService.recognizeInputStream(getResources().openRawResource(R.raw.audio));
                mText.setText("");
                return true;
            case R.id.show:
                if (!showFirebase) {
                    Toast.makeText(this, "Showing results from firebase", Toast.LENGTH_LONG).show();
                    mRecyclerView.setVisibility(View.GONE);
                    firebaseRecyclerView.setVisibility(View.VISIBLE);
                    showFirebase = true;
                } else {
                    Toast.makeText(this, "Showing cached result, hiding firebase", Toast.LENGTH_LONG).show();
                    mRecyclerView.setVisibility(View.VISIBLE);
                    firebaseRecyclerView.setVisibility(View.GONE);
                    showFirebase = false;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private void showStatus(final boolean hearingVoice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatus.setTextColor(hearingVoice ? mColorHearing : mColorNotHearing);
            }
        });
    }


    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void showSearchResults() {
        ArrayList<String> results = new ArrayList<>();
        RealmResults<Model> databaseResults = realm.where(Model.class).findAll();
        for (Model databaseResult : databaseResults) {
            TrieNode root = new TrieNode();
            root.insert(databaseResult.getSavedText());
            if (root.search(recentSpokenText, false))
                results.add(databaseResult.getSavedText());
        }
        //new ReadDataAsync().execute("");
        if (results.size() == 0) {
            emptyLayout.setVisibility(View.VISIBLE);
        }
        mAdapter = new ResultAdapter(results);
        mRecyclerView.setAdapter(mAdapter);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        CardView cardView;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_result, parent, false));
            text = (TextView) itemView.findViewById(R.id.text);
            cardView = (CardView) itemView.findViewById(R.id.card);
        }

    }

    private static class ResultAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArrayList<String> mResults = new ArrayList<>();

        ResultAdapter(ArrayList<String> results) {
            if (results != null) {
                mResults.addAll(results);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.text.setText(mResults.get(position));
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.text.setText(mResults.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }

        void addResult(String result) {
            mResults.add(0, result);
            notifyItemInserted(0);
        }

        public ArrayList<String> getResults() {
            return mResults;
        }

    }

    public class AddDataAsync extends android.os.AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("msgs");
            for (String s : strings) {
                myRef.push().setValue(s);
            }
            return null;
        }
    }
}
