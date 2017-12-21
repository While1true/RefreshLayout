package coms.kxjsj.refreshlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import coms.kxjsj.refreshlayout_master.MyRefreshWrap;
import coms.kxjsj.refreshlayout_master.RefreshLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RefreshLayout.init(new RefreshLayout.DefaultBuilder().setBaseRefreshWrap(MyRefreshWrap.class));
        setContentView(R.layout.activity_main);

        final RefreshLayout layout=findViewById(R.id.Refresh);
        layout.setListener(new RefreshLayout.Callback1<RefreshLayout.State>() {
            @Override
            public void call(RefreshLayout.State state) {

                if(state== RefreshLayout.State.REFRESHING||state== RefreshLayout.State.LOADING){
                    layout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layout.NotifyCompleteRefresh1(layout.findInHeaderView(R.id.textView).getMeasuredWidth()+10,"为你更新了很多信息");
                        }
                    },3000);
                }
            }
            @Override
            public void call(RefreshLayout.State state, int scroll) {
                super.call(state, scroll);
            }
        });
        RecyclerView recyclerView=layout.getmScroll();
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false)) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 30;
            }
        });
        layout.setOrentation(RefreshLayout.Orentation.HORIZONTAL);
    }
}
