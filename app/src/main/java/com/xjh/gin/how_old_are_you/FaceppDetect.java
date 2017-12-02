package com.xjh.gin.how_old_are_you;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gin on 2017/11/23.
 */

public class FaceppDetect {
    public interface CallBack{
        void success(JSONObject result);
        void error(FaceppParseException exception);
    }
    public static void detect(final Bitmap bm, final CallBack callBack){//匿名内部类传入参数最好用final
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //request
                    HttpRequests requests = new HttpRequests(Constant.KEY,Constant.SECRET,true,true);//isCN在不在中国
                    Bitmap bmSmall=Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmSmall.compress(Bitmap.CompressFormat.JPEG,100,stream);//将bmSamll压缩到stream中去
                    byte[] arrays = stream.toByteArray();
                    PostParameters params = new PostParameters();
                    params.setImg(arrays);
                    JSONObject jsonObject = requests.detectionDetect(params);
                    //
                    Log.e("TAG",jsonObject.toString());

                    if(callBack!=null){
                        callBack.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if(callBack!=null){
                        callBack.error(e);
                    }
                }
            }

        }).start();
    }
}
