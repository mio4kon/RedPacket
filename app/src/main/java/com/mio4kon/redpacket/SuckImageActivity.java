package com.mio4kon.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mio4kon.library.RedPacketView;

public class SuckImageActivity extends AppCompatActivity {

    private RecyclerView rv;
    private RedPacketView mRedPacketView;
    private TestImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suck_image);

        rv = (RecyclerView) findViewById(R.id.recycler);
        mRedPacketView = (RedPacketView) findViewById(R.id.red_packet);
        rv.setLayoutManager(new LinearLayoutManager(this));
        //1.inject RecyclerView
        mRedPacketView.setRecyclerView(rv);
        mAdapter = new TestImageAdapter();
        rv.setAdapter(mAdapter);
        setClick();
    }


    private void setClick() {
        mAdapter.setClickListener(new TestImageAdapter.ItemClickListener() {
            @Override
            public void clickItem(View v, int position) {
                startAnimi(v, position);
            }
        });
    }

    private void startAnimi(View v, int position) {
        View suckedView = LayoutInflater.from(this).inflate(R.layout.view_image, null);
        View orgSuckedView = v.findViewById(R.id.layout_image);
        mRedPacketView.supportOtherView4Suck(suckedView);
        mRedPacketView.suck(orgSuckedView);
    }


}
