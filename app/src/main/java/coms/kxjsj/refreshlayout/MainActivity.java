package coms.kxjsj.refreshlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import coms.kxjsj.refreshlayout_master.RefreshLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RefreshLayout layout=findViewById(R.id.Refresh);
        layout.setListener(new RefreshLayout.Callback1<RefreshLayout.State>() {
            @Override
            public void call(RefreshLayout.State state) {
                super.call(state);
                System.out.println(state);
                if(state== RefreshLayout.State.REFRESHING){
                    layout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layout.NotifyCompleteRefresh();
                        }
                    },3000);
                }
            }
            @Override
            public void call(RefreshLayout.State state, int scroll) {
                super.call(state, scroll);
            }
        });
    }
}
