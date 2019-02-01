package com.example.newsreader;

import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadClass extends AsyncTask<String, Void, String> {

    private String getData(URL url) {
        String retString = "";
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);

            int data = reader.read();

            while(data > -1) {
                char current = (char) data;
                retString += current;
                data = reader.read();
            }

            return retString;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected String doInBackground(String... strings) {
        URL url;
        String result = "";

        try{
            url = new URL(strings[0]);

            result = this.getData(url);
            if(result == null) {
                throw new Exception("Cannot get data");
            }

            JSONArray jsonArray = new JSONArray(result);

            int numberOfItems = jsonArray.length() > 20 ? 20 : jsonArray.length();

            MainActivity.articlesDB.execSQL("DELETE FROM articles");

            for(int i = 0; i < numberOfItems; i++) {

                String articleID = jsonArray.getString(i);

                url = new URL("https://hacker-news.firebaseio.com/v0/item/"+ articleID +".json?print=pretty");

                String articleInfo = this.getData(url);

                if(articleInfo == null) {
                    throw new Exception("Cannot get article");
                }

                JSONObject jsonObject = new JSONObject(articleInfo);

                if(!jsonObject.isNull("title") && !jsonObject.isNull("url")){
                    String articleTitle = jsonObject.getString("title");
                    String articleURL = jsonObject.getString("url");

//                    Log.i("Info ", articleTitle + articleURL);

                    url = new URL(articleURL);
                    String articleContent = this.getData(url);

                    Log.i("article content:", articleContent);

                    String sql = "INSERT INTO articles(articlesId, title, content) VALUES(?, ?, ?)";

                    SQLiteStatement statement = MainActivity.articlesDB.compileStatement(sql);
                    statement.bindString(1, articleID);
                    statement.bindString(2, articleTitle);
                    statement.bindString(3, articleContent);
                    statement.execute();
                }
            }

            MainActivity.updateListView();


        }catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
