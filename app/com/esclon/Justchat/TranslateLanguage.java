
package com.escalon.JustChat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.escalon.JustChat.R;
import com.escalon.JustChat.Fragments.TranslateLanguageFragment1;

public class TranslateLanguage extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.escalon.JustChat.R.layout.activity_translate_language);

        if (savedInstanceState == null) {
            this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, TranslateLanguageFragment1.newInstance())
                    .commitNow();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.translatemenu,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.tranlate_help:
                androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Translate language");
                builder.setMessage("Select a source language from which text is to converted " +
                        "and target language to which text is needed to translated." +
                        " "+
                        "The downloaded models tells which translation languages are downloaded on " +
                        "your mobile and you can use any two of them to translate languages."+
                        " "+
                        "You can download and delete languages according to your need.");

                builder.setIcon(R.drawable.ic_baseline_description_24);
                builder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
                return true;
        }
        return false;
    }
}