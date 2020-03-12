package com.example.se2einzelphase;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * @Author Macher Markus, 1619162
 *  SE2 - Einzelbeispiel --> 2.2) = Aufgabe 6
 */

public class MainActivity extends AppCompatActivity {

    private static final String IP = "se2-isys.aau.at";
    private  static final int PORT = 53212;
    private Client client = new Client();
    private TextView textView;
    private EditText inputText;
    private Button btn;
    private ProgressBar progressBar;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.resultText);
        inputText = findViewById(R.id.editText);
        btn = findViewById(R.id.sendMessage);
        progressBar = findViewById(R.id.progressBar);
    }

    public void onClickCalculate(View v){
        if(TextUtils.isEmpty(inputText.getText())) {
            showNotification("Enter values first");
        }else {
            if(disposable != null){
                disposable.dispose();
            }
            textView.setText(sortNumber(inputText.getText().toString()));
        }
    }
    public void sendNetworkCall(View v){
        if(TextUtils.isEmpty(inputText.getText())) {
            showNotification("Enter values first");
        }else if(!isNetworkAvailable()){
            showNotification("Network connection is not available");
        }else{
            textView.setText("");
            progressBar.setVisibility(View.VISIBLE);
            btn.setEnabled(false);
            Observable.just(client)
                    .delay(2, TimeUnit.SECONDS)
                    .doOnNext(client -> {
                        client.initClient(IP,PORT);
                        client.sendMessage(inputText.getText().toString());
                    })
                    .doOnDispose(() -> {
                        textView.setText("");
                        progressBar.setVisibility(View.GONE);
                        btn.setEnabled(true);
                        System.out.println("Network call canceled");
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Client>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            disposable = d;
                            System.out.println("Sending Message...");
                        }
                        @Override
                        public void onNext(@NonNull Client client) {
                            progressBar.setVisibility(View.GONE);
                            if(client.getReceivedMsg() != null){
                                textView.setText(MainActivity.this.client.getReceivedMsg());
                            }else{
                                textView.setText("internal error");
                            }
                        }
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e("Error", e.getMessage());
                            textView.setText("internal error");
                        }
                        @Override
                        public void onComplete() {
                            System.out.println("Done");
                            btn.setEnabled(true);
                        }
                    });
        }
    }
    public String sortNumber(String num){
        List<Character> even = new ArrayList<Character>();
        List<Character> uneven = new ArrayList<Character>();
        for (int i = 0; i < num.length(); i++) {
            if(num.charAt(i)%2 == 0){
                even.add(num.charAt(i));
            }else if(num.charAt(i) % 2 != 0){
                uneven.add(num.charAt(i));
            }
        }
        Collections.sort(even);
        Collections.sort(uneven);
        even.addAll(uneven);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < even.size() ; i++) {
            sb.append(even.get(i));
        }
        return sb.toString();
    }

    // pop-up notification for user if no values entered
    public void showNotification(String txt){
        Context context = getApplicationContext();
        CharSequence text = txt;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    // check if network connection is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
