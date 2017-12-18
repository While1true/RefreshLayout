# RefreshLayout
nestscroll Refreshing重构
![GIF.gif](http://upload-images.jianshu.io/upload_images/6456519-27f56d146baa0afb.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![GIF2.gif](http://upload-images.jianshu.io/upload_images/6456519-9e217be853b06569.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## xml
```
 <coms.kxjsj.refreshlayout_master.RefreshLayout
        android:id="@+id/Refresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:canFooter="true"
        app:footerID="@layout/footer_layout_horizontal"
        app:headerID="@layout/header_layout_horizontal"
        app:scrollID="@layout/xx" />
```
## code
```
final RefreshLayout layout=findViewById(R.id.Refresh);
        layout.setListener(new RefreshLayout.Callback1<RefreshLayout.State>() {
            @Override
            public void call(RefreshLayout.State state) {
                super.call(state);
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
```

## more properities
```
    <declare-styleable name="RefreshLayout">
        <attr name="canHeader" format="boolean" />
        <attr name="canFooter" format="boolean" />
        <attr name="overscroll" format="boolean" />
        <attr name="headerID" format="reference" />
        <attr name="scrollID" format="reference" />
        <attr name="footerID" format="reference" />

        <attr name="orentation" format="enum" >
            <enum name="HORIZONTAL" value="0"/>
            <enum name="VERTICAL" value="1"/>
        </attr>
        <attr name="mMaxHeadertScroll" format="dimension" />
        <attr name="mMaxFooterScroll" format="dimension" />
        <attr name="mHeaderRefreshPosition" format="dimension" />
        <attr name="mFooterRefreshPosition" format="dimension" />
        <attr name="mHeaderRefreshCompletePosition" format="dimension" />
        <attr name="mFooterLoadingCompletePosition" format="dimension" />
        <attr name="mFlingmax" format="dimension" />
        <attr name="delayCompleteTime" format="integer" />
    </declare-styleable>
```

## GOLBAL SET
```
 RefreshLayout.init(new RefreshLayout.DefaultBuilder()
 .XXX
 .setBaseRefreshWrap(MyRefreshWrap.class))
 .XXX;
```
## SELF RefreshWrap AS SAMPLE
```
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

```
