package com.example.safe_guide.Fragement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe_guide.R;
import com.example.safe_guide.adapter.BoardAdapter;
import com.example.safe_guide.model.BoardListModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BoardFragment extends Fragment {
    private View view;
    private ArrayList<BoardListModel> arrayList;
    private BoardAdapter boardAdapter;
    private RecyclerView rvBoradList;
    private LinearLayoutManager linearLayoutManager;
    private Button btn_add;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_board, container, false);
        // TODO: ui 구성을 설정합니다.

        return view;
    }

    private void fetchPosts() {
        OkHttpClient client = new OkHttpClient();

        String url = "https://safe-guide.herokuapp.com/api/v1/posts"; // 게시판 API 주소
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray jsonArray = jsonObject.getJSONArray("data");

                        // TODO: 게시물 목록을 처리하는 로직을 구현합니다.
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject postObject = jsonArray.getJSONObject(i);

                            // 게시물의 필요한 정보를 추출하여 처리합니다.
                            int postId = postObject.getInt("postId");
                            String title = postObject.getString("title");
                            String body = postObject.getString("body");

                            // TODO: 추출한 정보를 사용하여 게시물을 표시하거나 처리합니다.

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
