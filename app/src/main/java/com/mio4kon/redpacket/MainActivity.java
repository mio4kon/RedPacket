package com.mio4kon.redpacket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mio4kon.library.RedPacketView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rv;
    private RedPacketView mRedPacketView;
    private TestAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = (RecyclerView) findViewById(R.id.recycler);
        mRedPacketView = (RedPacketView) findViewById(R.id.red_packet);
        rv.setLayoutManager(new LinearLayoutManager(this));
        //1.inject RecyclerView
        mRedPacketView.setRecyclerView(rv);
        mAdapter = new TestAdapter();
        rv.setAdapter(mAdapter);
        setClick();
    }

    private void setClick() {
        mAdapter.setClickListener(new TestAdapter.ItemClickListener() {
            @Override
            public void clickItem(View v, int position) {
                startAnimi(v, position);
            }
        });

        mRedPacketView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });
    }

    private void startAnimi(View v, int position) {
        if (position % 2 == 0) {
            //2. suck the view
            View suckView = v.findViewById(R.id.tv_money);
            mRedPacketView.suck(suckView);
        } else {
            //or you can bubble view in redPacket
            View bubbleView = LayoutInflater.from(this).inflate(R.layout.view_scores, null);
            TextView bubleTV = (TextView) bubbleView.findViewById(R.id.tv_scores);
            bubleTV.setText("+" + 22.0);
            mRedPacketView.bubble(bubbleView);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.action_suck_image);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, SuckImageActivity.class));
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}
