package com.shashipage.invoicechecker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //ui variables
    private Button[] buttons = new Button[12];
    private TextView[] numTextViews = new TextView[3];
    private TextView alertTextView;
    private Spinner mSpinner;

    ArrayList<String> spinnerArray = new ArrayList<>();
    private ArrayAdapter<String> mAdapter; //Spinner Adapter
    private int mSelectedIndex = 0; //Spinner index
    private String numberEntered = ""; //輸入值

    ApiResponse apiResponse; //Convert json to object with GSON

    String grand_prize = ""; //特別獎 1000萬
    String first_prize = ""; //特獎 200萬
    ArrayList<String> head_prize = new ArrayList<>(); //頭獎三組 20萬
    ArrayList<String> bonus_six = new ArrayList<>(); //增開1~3組六獎

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViewComponent();
        apiConnect();
    }

    private void setupViewComponent() {
        //ui變數定義與監聽
        for (int i = 0; i < 11; i++) {
            String buttonID = "btn" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = ((Button) findViewById(resID));
            buttons[i].setOnClickListener(this);
        }

        for (int i = 0; i < 3; i++) {
            String textViewID = "numTextView" + i;
            int resID = getResources().getIdentifier(textViewID, "id", getPackageName());
            numTextViews[i] = ((TextView) findViewById(resID));
            numTextViews[i].setOnClickListener(this);
        }

        alertTextView = (TextView) findViewById(R.id.alertTextView);

        mAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item) {
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                if (position == mSelectedIndex) {
                    tv.setTextColor(Color.BLUE);
                }
                return tv;
            }
        };
    }

    private void apiConnect() {
        //連線api取得中獎號碼
        final ProgressDialog proDialog = new ProgressDialog(this);
        proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        proDialog.setCancelable(false);
        proDialog.setTitle("中獎號碼載入中");
        proDialog.setMessage("請輸入發票末三碼，祝您中獎");
        proDialog.show();
        String BaseUrl = "https://script.google.com/macros/s/AKfycby8nAMFl6p4QrEzb6HzK5B9GEy4nzf9Iqm3G-sI-D0S3Lk9ATm7C-fHdcEiposzbbFa/";
        //自製Google AppScript爬蟲api
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl).addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<ApiResponse> call = service.getData();

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                apiResponse = response.body();
                assert apiResponse != null;
                if (response.code() == 200) {
                    for (int i = 0; i < apiResponse.data.size(); i++) {
                        spinnerArray.add(apiResponse.data.get(i).published);
                    }
                    setSpinner();
                    setWinningNumbers(0);
                    proDialog.cancel();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                alertTextView.setText(t.getMessage());
            }
        });
    }

    private ArrayList<String> splitWinningNumber(String str) {
        //取得json中，若獎項不只一組號碼則會以"、"隔開。這邊做切割並回傳ArrayList
        String[] winNumbers = str.split("、");
        ArrayList list = new ArrayList(Arrays.asList(winNumbers));
        return list;
    }

    private void prizeCheck() {
        //中獎判斷
        for (int i = 0; i < bonus_six.size(); i++) {
            if (numberEntered.equals(bonus_six.get(i))) {
                alertTextView.setText("恭喜中兩百！");
            }
        }

        for (int i = 0; i < head_prize.size(); i++) {
            if (numberEntered.equals(head_prize.get(i).substring(5))) {
                alertTextView.setText("恭喜中兩百！注意二三四五與頭獎：" + head_prize.get(i));
            }
        }
        //以下使用append以避免特獎與特別獎末三碼機率性與頭獎及增開獎相同的情形
        if (numberEntered.equals(first_prize.substring(5))) {
            alertTextView.append("注意特獎兩百萬：" + first_prize);
        }

        if (numberEntered.equals(grand_prize.substring(5))) {
            alertTextView.append("注意特別獎一千萬：" + grand_prize);
        }

        if (alertTextView.getText().toString().equals("")) {
            alertTextView.setText("沒中");
        }
    }

    private void numInput(int num) {
        //若第一格有數字則填地二格以此類推，三格都有數字時下一個輸入值會先觸發clearAll()清空
        String number = Integer.toString(num);
        if (!numTextViews[2].getText().toString().equals("")) {
            clearAll();
        }
        if (numTextViews[0].getText().toString().equals("")) {
            numberEntered = numberEntered + number;
            numTextViews[0].setText(number);
        } else if (numTextViews[1].getText().toString().equals("")) {
            numberEntered = numberEntered + number;
            numTextViews[1].setText(number);
        } else if (numTextViews[2].getText().toString().equals("")) {
            numberEntered = numberEntered + number;
            numTextViews[2].setText(number);
            prizeCheck();
        }
    }

    private void clearAll() {
        for (int i = 0; i < 3; i++) {
            numTextViews[i].setText("");
        }
        numberEntered = "";
        alertTextView.setText("");
    }

    private void setSpinner() {
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mAdapter.addAll(spinnerArray);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(mSpinOnItemSelLis);
    }

    private void setWinningNumbers(int i) {
        //根據Spinner所選擇的期別更動中獎號碼
        grand_prize = apiResponse.data.get(i).grand;
        first_prize = apiResponse.data.get(i).first;
        head_prize = splitWinningNumber(apiResponse.data.get(i).head);
        bonus_six = splitWinningNumber(apiResponse.data.get(i).bonusSix);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn0:
                numInput(0);
                break;

            case R.id.btn1:
                numInput(1);
                break;

            case R.id.btn2:
                numInput(2);
                break;

            case R.id.btn3:
                numInput(3);
                break;

            case R.id.btn4:
                numInput(4);
                break;

            case R.id.btn5:
                numInput(5);
                break;

            case R.id.btn6:
                numInput(6);
                break;

            case R.id.btn7:
                numInput(7);
                break;

            case R.id.btn8:
                numInput(8);
                break;

            case R.id.btn9:
                numInput(9);
                break;

            case R.id.btn10:
                clearAll();
                break;

        }
    }

    private AdapterView.OnItemSelectedListener mSpinOnItemSelLis = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedIndex = position;
            clearAll();
            setWinningNumbers(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

}