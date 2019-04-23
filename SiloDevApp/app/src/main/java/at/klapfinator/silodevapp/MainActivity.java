package at.klapfinator.silodevapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import at.klapfinator.silo.LogFormatHelper;
import at.klapfinator.silo.Silo;

public class MainActivity extends AppCompatActivity {
    TextView textViewCount;
    Button btnViewLogCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Silo
        Silo.initialize(MainActivity.this);
        Silo.setUrl("http://192.168.1.105:3000/index");
        Silo.setLogCatOutputEnabled(true);

        setContentView(R.layout.activity_main);

        Button btnSendDirectLog = findViewById(R.id.btn_sendDirectLog);
        Button btnGenerateLog = findViewById(R.id.btn_generateLog);
        Button btnGenerateLogs = findViewById(R.id.btn_generateLogs);
        Button btnPushLogs = findViewById(R.id.btn_pushLogs);
        btnViewLogCount = findViewById(R.id.btn_viewLogCount);
        textViewCount = findViewById(R.id.textViewCount);

        //LogFormatHelper logFormatHelper = new LogFormatHelper(this);

        btnGenerateLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < 1000; i++) {
                    Silo.w("Button generateLog clicked" + i);
                }
            }
        });

        btnGenerateLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Silo.w("Button generateLog clicked");
            }
        });

        btnSendDirectLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Silo.send("Button sendDirectLog clicked");
            }
        });

        btnPushLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Silo.push();
            }
        });

        btnViewLogCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Silo.i("Button getCount clicked");
                try {
                    String pendingLogs = String.valueOf(Silo.getPendingLogAmount());
                    textViewCount.setText(pendingLogs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
