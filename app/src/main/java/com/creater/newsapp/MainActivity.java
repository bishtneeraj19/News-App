package com.creater.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.creater.newsapp.api.ApiClient;
import com.creater.newsapp.api.ApiInterface;
import com.creater.newsapp.model.Article;
import com.creater.newsapp.model.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
 public static final String API_KEY="2ff7f7a8767f462d80d45f2e278b47e1";
 private RecyclerView recyclerView;
 private RecyclerView.LayoutManager layoutManager;
 private List<Article> articles=new ArrayList<>();
 private Adapter adapter;
 private TextView topHeadline;
 private String TAG=MainActivity.class.getSimpleName();
 private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout=findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
topHeadline=findViewById(R.id.topHeading);
        recyclerView=findViewById(R.id.recycleView);
        layoutManager=new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
 onLoadingSwipeRefresh("");
    }
    public void LoadJson(String keyword){
        topHeadline.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        ApiInterface apiInterface= ApiClient.getApiClient().create(ApiInterface.class);
        String country=Utils.getCountry();
String language=Utils.getLanguage();
        Call<News> call;
        if(keyword.length()>0){
            call=apiInterface.getNewsSearch(keyword,language,"publishedAt",API_KEY);
        }else {
            call = apiInterface.getNews(country, API_KEY);
        }  call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful()&&response.body().getArticle()!=null){
                    if(!articles.isEmpty()){
                        articles.clear();
                    }
                    articles=response.body().getArticle();
                    adapter=new Adapter(articles,MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                topHeadline.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                }
                else{
                    topHeadline.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "No Result", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                topHeadline.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        SearchManager searchManager=(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView=(SearchView) menu.findItem(R.id.search).getActionView();
        MenuItem search=menu.findItem(R.id.search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest News......");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length()>2){
                onLoadingSwipeRefresh(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
              //  LoadJson(newText);
                return false;
            }
        });
        search.getIcon().setVisible(false,false);
        return true;
    }

    @Override
    public void onRefresh() {
        LoadJson("");
    }
    private void onLoadingSwipeRefresh(final  String keyword){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson(keyword);
                    }
                }
        );
    }
}
