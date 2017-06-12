package com.akashjpro.sqlitesound;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnGhi, btnPause, btnPlay, btnStop, btnThem;
    EditText edtTitle;
    ListView lvSong;
    ArrayList<String> arrayTitle;
    ArrayList<byte[]> arrayFile;
    ArrayAdapter adapter = null;
    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;
    public static  SQLite db ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControls();

        db = new SQLite(this, "Music.sqlite", null, 1);
        db.queryData("CREATE TABLE IF NOT EXISTS Song(Id INTEGER PRIMARY KEY AUTOINCREMENT, Title VARCHAR, File BLOB)");


        arrayTitle = new ArrayList<String>();
        arrayFile  = new ArrayList<byte[]>();

        Cursor cursorSong = db.getData("SELECT * FROM Song");
        while (cursorSong.moveToNext()){
            arrayTitle.add(cursorSong.getString(1));
            arrayFile.add(cursorSong.getBlob(2));
        }

        adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, arrayTitle);
        lvSong.setAdapter(adapter);
        addEvents();
    }

    private void addEvents() {

        btnGhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/khoaphamvn.3gpp";
                myRecorder = new MediaRecorder();
                myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                myRecorder.setOutputFile(outputFile);

                start(view);
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop(view);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(view);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlay(view);
            }
        });

        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.insertSong(
                        edtTitle.getText().toString().trim(), FileLocal_To_Byte(outputFile)
                );
                Toast.makeText(MainActivity.this, "Đã thêm", Toast.LENGTH_SHORT).show();
                arrayTitle.clear();
                arrayFile.clear();
                Cursor cursorSong = db.getData("SELECT * FROM Song");
                while (cursorSong.moveToNext()){
                    arrayTitle.add(cursorSong.getString(1));
                    arrayFile.add(cursorSong.getBlob(2));
                }
                adapter.notifyDataSetChanged();
            }
        });

        lvSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                playMp3FromByte(arrayFile.get(i));
            }
        });

        lvSong.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                db.queryData("DELETE FROM Song WHERE Title = '"+ arrayTitle.get(i)+"' ");
                return false;
            }
        });
    }

    private void addControls() {
        btnGhi   = (Button)findViewById(R.id.buttonGhi);
        btnStop  = (Button)findViewById(R.id.buttonStop);
        btnPause = (Button)findViewById(R.id.buttonPause);
        btnPlay  = (Button)findViewById(R.id.buttonPlay);
        btnThem  = (Button)findViewById(R.id.buttonThem);

        edtTitle = (EditText)findViewById(R.id.editTextTitle);

        lvSong = (ListView) findViewById(R.id.listViewDSTitle);
    }

    public void start(View view){
        try {
            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Start recording...",
                Toast.LENGTH_SHORT).show();
    }

    public void stop(View view){
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder  = null;

            Toast.makeText(getApplicationContext(), "Stop recording...",
                    Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void play(View view) {
        try{
            myPlayer = new MediaPlayer();
            myPlayer.setDataSource(outputFile);
            myPlayer.prepare();
            myPlayer.start();

            Toast.makeText(getApplicationContext(), "Start play the recording...",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlay(View view) {
        try {
            if (myPlayer != null) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = null;

                Toast.makeText(getApplicationContext(), "Stop playing the recording...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] FileLocal_To_Byte(String path){
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    private void playMp3FromByte(byte[] mp3SoundByteArray) {
        try {

            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            MediaPlayer mediaPlayer = new MediaPlayer();

            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

}
