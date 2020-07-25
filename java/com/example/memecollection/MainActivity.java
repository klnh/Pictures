package com.example.memecollection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private DBOpenHandler dbOpenHandler;
    private SQLiteDatabase db;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;
    private EditText picURL;

    private GridView gridView;

    private final static int IMAGE_REQUEST_CODE = 1;
    public static final int RC_CHOOSE_PHOTO = 2;

    List<Map<String, Object>> list = new ArrayList<>();
    SimpleAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbOpenHandler = new DBOpenHandler(this, DBOpenHandler.DB_NAME, null,1 );
        db = dbOpenHandler.getWritableDatabase();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("表情图库");
        actionBar.setDisplayShowCustomEnabled(true);
        ImageView imageView = new ImageView(this);
        actionBar.setCustomView(imageView);

        gridView = findViewById(R.id.gridView);

        int index = 1;
        List<Integer> idList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            int id = getResources().getIdentifier("pic" + index, "mipmap", getPackageName());
            index++;
            if (id == 0)
                break;
            idList.add(id);
        }
        int len = idList.size();
        System.out.println("LEN:" + len);
        int[] idArr = new int[len];
        for (int i = 0; i < len; ++i) {
            idArr[i] = idList.get(i);
        }

        //查询已经插入的图片记录
        String sql = "SELECT url FROM property";
        Cursor cursor = db.rawQuery(sql,null);
        while(cursor.moveToNext()){
            String url = cursor.getString(cursor.getColumnIndex("url"));
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            Map<String ,Object> map = new HashMap<>();
            map.put("icon", bitmap);
            list.add(map);
        }

//        for (int i = 0; i < idArr.length; i++) { //这里注释掉是因为自带图片太多，不好分辨新加入的本机相册图片
        for (int i = 0; i < 5; i++) {
            Map<String, Object> map = new HashMap<>();
            //System.out.println(idArr[i]);
            map.put("icon", idArr[i]);
            list.add(map);
        }


        adapter = new SimpleAdapter(this,
                list,
                R.layout.list_item,
                new String[]{"icon"},
                new int[]{R.id.icon});

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(data instanceof Bitmap){
                    AppCompatImageView appCompatImageView = (AppCompatImageView)view;
                    ((AppCompatImageView) view).setImageBitmap((Bitmap)data);
                    return true;
                }
                return false;
            }
        });

        gridView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.support, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action1:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //未授权
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, IMAGE_REQUEST_CODE);
                } else {
                    choosePhoto();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void choosePhoto() {
        // 打开相册，选择图片
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_CHOOSE_PHOTO:
                choosePhoto();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn,
                            null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String path = cursor.getString(columnIndex);

                    cursor.close();
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    Map<String ,Object> map = new HashMap<>();
                    map.put("icon",bitmap);
                    list.add(map);
                    adapter.notifyDataSetChanged(); //通知新增了图片

                    //在数据库中插入数据
                    String INSERT_DATA = "INSERT INTO property(url) values (\'" + path  + "\')";
                    db.execSQL(INSERT_DATA);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
