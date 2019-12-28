package com.example.commonutil;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MirrorActivity extends Activity {
    private static final String TAG = "MirrorActivity";
    private Button mirrorBtn;
    private Button stopBtn;
    private Context mContext;
    private MirrorPermissionFragment mPermissionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirror);
        mirrorBtn = findViewById(R.id.mirror_id);
        mContext = MirrorActivity.this.getApplicationContext();
        mirrorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MirrorPermissionFragment permissionFragment = (MirrorPermissionFragment)MirrorActivity.this.getFragmentManager()
                        .findFragmentByTag(MirrorPermissionFragment.TAG);;
                boolean isNewInstance = permissionFragment == null;
                if (isNewInstance) {
                    permissionFragment = MirrorPermissionFragment.newInstance();
                    permissionFragment.setAppContext(mContext);
                    FragmentManager fm = MirrorActivity.this.getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction()
                            .add(permissionFragment, MirrorPermissionFragment.TAG);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        transaction.commitNow();
                    } else {
                        transaction.commitAllowingStateLoss();
                    }
                    fm.executePendingTransactions();
                } else {
                    permissionFragment.setAppContext(mContext);
                }
                mPermissionFragment = permissionFragment;
//                permissionFragment.setMediaProjectionListener(mediaProjectionListener);
//                permissionFragment.setPlayerListener(mPlayerListener);
//                permissionFragment.registerMediaProjectionPermission();
            }
        });

        stopBtn = findViewById(R.id.mirror_stop_id);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPermissionFragment!=null){
                    mPermissionFragment.stopMirror();
                }
            }
        });

    }
}
