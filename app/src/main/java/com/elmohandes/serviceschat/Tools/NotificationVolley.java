package com.elmohandes.serviceschat.Tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationVolley {

    private RequestQueue queue;
    Context context;
    String url;

    public  NotificationVolley(Context context){

        this.context = context;
        queue = Volley.newRequestQueue(context);
        FirebaseMessaging.getInstance().subscribeToTopic
                (FirebaseAuth.getInstance().getUid());
        url = "https://fcm.googleapis.com/fcm/send";

    }

    public void sendNotification(String title ,String body ,String recieverId){

        JSONObject json = new JSONObject();
        try {
            json.put("to","/topics/"+recieverId);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title",title);
            notificationObj.put("body",body);

            JSONObject extraData = new JSONObject();
            extraData.put("brandId","puma");
            extraData.put("category","Shoes");

            json.put("notification",notificationObj);
            json.put("data",extraData);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    url, json,
                    response -> {

                        Log.d("MUR", "onResponse: ");
                        //Toast.makeText(context, "on response", Toast.LENGTH_SHORT).show();
                    }, error -> {
                Log.d("MUR", "onError: "+error.networkResponse);
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAAqxZ8uT0:APA91bGSWsM87y0ceD27uZRmE4tQorRknSxu4191rYC6x5n8BtLd16MvpsMHjyuQR4OXqdGes7yY5NICGVpthZ0JUP1lp5tB6zPK3O25cPrC9dnQDQhPNN4KUh5qb-QY3qpgnzM30s4q");
                    return header;
                }
            };
            queue.add(request);
        }
        catch (JSONException e)

        {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

}
