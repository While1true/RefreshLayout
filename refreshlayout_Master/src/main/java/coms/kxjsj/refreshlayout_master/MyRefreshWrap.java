package coms.kxjsj.refreshlayout_master;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Created by vange on 2017/12/15.
 */

public class MyRefreshWrap extends RefreshLayout.BaseRefreshWrap<String> {
    private ImageView mHeaderimageView;
    private TextView mHeadertextView;
    private ProgressBar mHeaderPrgress;
    private ImageView mFooterimageView;
    private TextView mfootertextView;
    private ProgressBar mfootPrgress;
    private WeakReference<RefreshLayout> layoutWeakReference;

    public RefreshLayout getRefreshLayout() {
        return layoutWeakReference.get();
    }

    private RefreshLayout.State currentState;
    String[] title;

    public void onPullHeader(View view, int scrolls) {
        /**
         * 完成状态时不要改变字
         */
        if(currentState== RefreshLayout.State.REFRESHCOMPLETE||currentState== RefreshLayout.State.REFRESHING){
            return;
        }
        if (mHeadertextView!=null&&scrolls > getRefreshLayout().getmHeaderRefreshPosition()) {
            mHeadertextView.setText(title[1]);
        } else {
            mHeadertextView.setText(title[0]);
        }

    }

    public void onPullFooter(View view, int scrolls) {
        /**
         * 完成状态时不要改变字
         */
        if(currentState== RefreshLayout.State.LOADINGCOMPLETE||currentState==RefreshLayout.State.LOADING){
            return;
        }
        if (mfootertextView!=null&&scrolls > getRefreshLayout().getmFooterRefreshPosition()) {
            mfootertextView.setText(title[4]);
        } else {
            mfootertextView.setText(title[3]);
        }
    }

    public void OnStateChange(RefreshLayout.State state) {
        currentState=state;
        switch (state) {
            case REFRESHCOMPLETE:
                mHeaderPrgress.setVisibility(View.INVISIBLE);
                mHeadertextView.setText(data);
                break;
            case LOADING:
                mfootPrgress.setVisibility(View.VISIBLE);
                mfootertextView.setText(title[5]);
                break;
            case REFRESHING:
                mHeaderPrgress.setVisibility(View.VISIBLE);
                mHeadertextView.setText(title[2]);
                break;
            case LOADINGCOMPLETE:
                mfootPrgress.setVisibility(View.INVISIBLE);
                mfootertextView.setText(data);
                break;
            case IDEL:
                break;
            case PULL_HEADER:
                break;
            case PULL_FOOTER:
                break;
        }

    }
    @Override
    protected void initView(RefreshLayout layout) {
        super.initView(layout);
        layoutWeakReference=new WeakReference<RefreshLayout>(layout);
        View header = layout.getmHeader();
        View footer = layout.getmFooter();
        if(header!=null) {
            mHeaderimageView = header.findViewById(R.id.imageView);
            mHeadertextView = header.findViewById(R.id.textView);
            mHeaderPrgress = header.findViewById(R.id.progressBar);
        }
        if(footer!=null) {
            mFooterimageView = footer.findViewById(R.id.imageView);
            mfootertextView = footer.findViewById(R.id.textView);
            mfootPrgress = footer.findViewById(R.id.progressBar);
        }
        String[] tempVertical={"下拉刷新", "释放刷新", "正在刷新中", "上拉加载", "释放加载", "正在加载中"};
        String[] tempHorizontal={"右拉刷新", "释放刷新", "正在刷新中", "左拉加载", "释放加载", "正在加载中"};
        title=(layout.getOrentation()== RefreshLayout.Orentation.VERTICAL)?
                tempVertical:tempHorizontal;
    }
}