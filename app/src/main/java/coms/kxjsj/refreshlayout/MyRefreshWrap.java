package coms.kxjsj.refreshlayout;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import coms.kxjsj.refreshlayout_master.RefreshLayout;

/**
 * Created by vange on 2017/12/15.
 */

public class MyRefreshWrap extends RefreshLayout.BaseRefreshWrap {
    private ImageView mHeaderimageView;
    private TextView mHeadertextView;
    private ProgressBar mHeaderPrgress;
    private ImageView mFooterimageView;
    private TextView mfootertextView;
    private ProgressBar mfootPrgress;

    public void onPullHeader(View view, int scrolls) {
        if (mHeaderimageView == null) {
            mHeaderimageView = findViewbyId(view, coms.kxjsj.refreshlayout_master.R.id.imageView);
            mHeadertextView = findViewbyId(view, coms.kxjsj.refreshlayout_master.R.id.textView);
            mHeaderPrgress = findViewbyId(view, coms.kxjsj.refreshlayout_master.R.id.progressBar);
        }

    }

    public void onPullFooter(View view, int scroll) {
        if(mfootertextView==null) {
            mFooterimageView = findViewbyId(view, coms.kxjsj.refreshlayout_master.R.id.imageView);
            mfootertextView = findViewbyId(view, coms.kxjsj.refreshlayout_master.R.id.textView);
            mfootPrgress = findViewbyId(view, coms.kxjsj.refreshlayout_master.R.id.progressBar);
        }
    }

    public void OnStateChange(RefreshLayout.State state) {
        if (state == RefreshLayout.State.LOADING) {
            mfootPrgress.setVisibility(View.VISIBLE);
        } else if (state == RefreshLayout.State.REFRESHING) {
            mHeaderPrgress.setVisibility(View.VISIBLE);
        }

    }
}
