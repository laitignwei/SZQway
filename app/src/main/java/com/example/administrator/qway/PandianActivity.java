package com.example.administrator.qway;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import db.Goods;



public class PandianActivity extends Activity {

    private static final int JSON = 1;
    private ListView listView;
    private Button button;
    private Handler handler;
    private ArrayList<Goods> list;
    private TableAdapter adapter;
    private Button bt_search_condition;
    private TextView search_code;
//    String text;
    String code="";
//    private LinearLayout linear1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pandian);
        initView();
        //设置表格标题的背景颜色
        ViewGroup tableTitle = (ViewGroup) findViewById(R.id.table_title);
        tableTitle.setBackgroundColor(Color.rgb(177, 173, 172));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getResult();
            }
        });
        //搜索全部信息的
        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                String json = bundle.getString("json");
                list = new ArrayList<Goods>();
                if (msg.what == JSON) {
                    //列表数据
                    list = JSONUtil.parseJson(json);
                    Log.i("list", list.toString());
                    adapter = new TableAdapter(getBaseContext(), list);
                    listView.setAdapter(adapter);
                    listView.setTextFilterEnabled(true);
                }
            }
        };
        bt_search_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(PandianActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                //默认是横屏，加上下面这段代码转换为竖屏
                integrator.setCaptureActivity(ScanActivity.class);
                integrator.setPrompt("扫描条形码");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "扫码取消！", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "扫描成功，条码值: " + result.getContents(), Toast.LENGTH_SHORT).show();
                code=result.getContents();
                search_code.setText(code);

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);

        }

    }
    private void getResult() {
        final String uri = "http://szqwayoa.dns0755.net:82/Qwayservers/Pandian.php";
        Log.i("getResult：", "开始获取数据");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                String str = null;
                try {
                    str = getJsonContent(uri);
                } catch (IOException e) {

                }
                Log.i("result", str);
                bundle.putString("json", str);
                Message msg = new Message();
                msg.setData(bundle);
                msg.what = JSON;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private String getJsonContent(String urlPath) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection http= (HttpURLConnection) url.openConnection();
        //往网页写入POST数据，和网页POST方法类似，参数间用‘&’连接
        String params="code="+code;
        http.setDoOutput(true);
        http.setRequestMethod("POST");
        OutputStream out=http.getOutputStream();
        out.write(params.getBytes());//post提交参数
        out.flush();
        out.close();

        //读取网页返回的数据
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(http.getInputStream()));//获得输入流
        String line="";
        StringBuilder sb=new StringBuilder();//建立输入缓冲区
        while (null!=(line=bufferedReader.readLine())){//结束会读入一个null值
            sb.append(line);//写缓冲区
        }
        String rs_upload= sb.toString();//返回结果
        return rs_upload;//通过out.Stream.toByteArray获取到写的数据

    }

//    public String getSort() {
//        PandianActivity m = this;
//        return m.getSort();
//    }
    private void initView() {
        button = (Button) findViewById(R.id.button);
        bt_search_condition = (Button) findViewById(R.id.bt_search_condition);
        search_code = (TextView) findViewById(R.id.search_code);
        listView = (ListView) findViewById(R.id.list);
        listView.setTextFilterEnabled(true);
//        linear1= (LinearLayout) findViewById(R.id.linear1);
    }


}
