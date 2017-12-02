package com.xjh.gin.how_old_are_you;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final int PICK_CODE = 0X110;
    private ImageView mPhoto;
    private Button mGetImage;
    private Button mDetect;
    private TextView mTip;
    private View mWaitting;

    private String mCurreatPhotoStr;
    private Bitmap mPhotoImg;

    private Paint mPaint;//画笔

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isPermissionAllGranted(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        initViews();
        initEvents();
        mPaint = new Paint();
    }

    private void initEvents() {
        mGetImage.setOnClickListener(this);
        mDetect.setOnClickListener(this);
    }

    private void initViews() {
        mPhoto = (ImageView) findViewById(R.id.id_photo);
        mGetImage = (Button) findViewById(R.id.id_getImage);
        mDetect = (Button) findViewById(R.id.id_detect);
        mTip = (TextView) findViewById(R.id.id_tip);
        mWaitting = findViewById(R.id.id_waitting);
    }

    private static final int MSG_SUCESS = 0x111;
    private static final int MSG_ERROR = 0x112;

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCESS:
                    mWaitting.setVisibility(View.GONE);
                    JSONObject rs = (JSONObject) msg.obj;
                    prepareRsBitmap(rs);
                    mPhoto.setImageBitmap(mPhotoImg);
                    break;
                case MSG_ERROR:
                    mWaitting.setVisibility(View.GONE);
                    String errorMsg = (String) msg.obj;
                    if (TextUtils.isEmpty(errorMsg)) {
                        mTip.setText("Error.");
                    } else {
                        mTip.setText(errorMsg);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void prepareRsBitmap(JSONObject rs) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(mPhotoImg.getWidth(), mPhotoImg.getHeight(), mPhotoImg.getConfig());
            Canvas canvas = new Canvas(bitmap);//定义画布
            canvas.drawBitmap(mPhotoImg, 0, 0, null);//把原图绘制在画布上

            JSONArray faces = rs.getJSONArray("face");
            int faceCount = faces.length();
            mTip.setText("find " + faceCount + " face");
            for (int i = 0; i < faceCount; i++) {
                JSONObject face = faces.getJSONObject(i);
                JSONObject posObj = face.getJSONObject("position");
                float x = (float) posObj.getJSONObject("center").getDouble("x");
                float y = (float) posObj.getJSONObject("center").getDouble("y");

                float w = (float) posObj.getDouble("width");
                float h = (float) posObj.getDouble("height");

                x = x / 100 * bitmap.getWidth();
                y = y / 100 * bitmap.getHeight();
                w = w / 100 * bitmap.getWidth();
                h = h / 100 * bitmap.getHeight();

                mPaint.setStrokeWidth(3);
                mPaint.setColor(0xffffffff);

                canvas.drawLine(x - w / 2, y - h / 2, x - w / 2, y + h / 2, mPaint);
                canvas.drawLine(x - w / 2, y - h / 2, x + w / 2, y - h / 2, mPaint);
                canvas.drawLine(x + w / 2, y + h / 2, x + w / 2, y - h / 2, mPaint);
                canvas.drawLine(x + w / 2, y + h / 2, x - w / 2, y + h / 2, mPaint);

                String gender = face.getJSONObject("attribute").getJSONObject("gender").getString("value");
                int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");

                Bitmap ageBitmap = buildAgeBitmap(age, "Male".equals(gender));
                int ageWidth = ageBitmap.getWidth();
                int ageHeight = ageBitmap.getHeight();

                if (bitmap.getWidth() < mPhoto.getWidth() && bitmap.getHeight() < mPhoto.getHeight()) {
                    float ratio = Math.max(bitmap.getWidth() * 1.0f / mPhoto.getWidth(), bitmap.getHeight() * 1.0f / mPhoto.getHeight());
                    ageBitmap = Bitmap.createScaledBitmap(ageBitmap, (int) (ageWidth * ratio), (int) (ageHeight * ratio), false);
                }

                canvas.drawBitmap(ageBitmap, x - ageBitmap.getWidth() / 2, y - h / 2 - ageBitmap.getHeight(), null);

                mPhotoImg = bitmap;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap buildAgeBitmap(int age, boolean isMale) {
        TextView tv = mWaitting.findViewById(R.id.id_age_and_gender);
        tv.setText(age + "");
        if (isMale) {
            tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male), null, null, null);//设置Drawable显示在text的左、上、右、下位置
        } else {
            tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female), null, null, null);
        }
        tv.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
        tv.destroyDrawingCache();
        return bitmap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_getImage:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_CODE);
                break;
            case R.id.id_detect:
                mWaitting.setVisibility(View.VISIBLE);//显示
                if (mCurreatPhotoStr != null && !mCurreatPhotoStr.trim().equals("")) {
                    resizePhoto();
                } else {
                    mPhotoImg = BitmapFactory.decodeResource(getResources(), R.drawable.defaultphoto);
                }
                FaceppDetect.detect(mPhotoImg, new FaceppDetect.CallBack() {
                    @Override
                    public void success(JSONObject result) {
                        Message msg = Message.obtain();
                        msg.what = MSG_SUCESS;
                        msg.obj = result;
                        mHandle.sendMessage(msg);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message msg = Message.obtain();
                        msg.what = MSG_ERROR;
                        msg.obj = exception.getErrorMessage();
                        mHandle.sendMessage(msg);
                    }
                });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CODE) {
            if (data != null) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                mCurreatPhotoStr = cursor.getString(idx);
                cursor.close();
                resizePhoto();
                mPhoto.setImageBitmap(mPhotoImg);
                mTip.setText("Click Detect ==>");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void resizePhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//true为不加载图片，只有宽高这类的数据
        BitmapFactory.decodeFile(mCurreatPhotoStr, options);

        double ratio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024f);//求出压缩比例
        options.inSampleSize = (int) Math.ceil(ratio);//把压缩比例传入options中
        options.inJustDecodeBounds = false;
        mPhotoImg = BitmapFactory.decodeFile(mCurreatPhotoStr, options);//压缩图片,mPhoto是压缩过后的Bitmap
    }
}