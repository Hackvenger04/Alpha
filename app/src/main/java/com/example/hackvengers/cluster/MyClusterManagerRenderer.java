package com.example.hackvengers.cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.hackvengers.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {
    private final IconGenerator iconGenerator;
    private final ImageView imageView;
    private final int markerWidth;
    private final int markerHeight;
    private Context context;

    public MyClusterManagerRenderer(Context context, GoogleMap googleMap,
                                    ClusterManager<ClusterMarker> clusterManager) {

        super(context, googleMap, clusterManager);
        this.context=context;
        // initialize cluster item icon generator
        iconGenerator = new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        markerWidth = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        markerHeight = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(imageView);

    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {

//        Glide.with(context).load(Uri.parse(item.getIconPicture())).into(imageView);

//        String id=item.getSnippet();
        imageView.setImageResource(R.drawable.cartman_cop);

//        if(item.getUser().isMentor()){
//            imageView.setImageResource(R.drawable.cartman_cop);
//        }
        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title("Hello").snippet(item.getUser().getUid());

    }


    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return false;
    }

}
