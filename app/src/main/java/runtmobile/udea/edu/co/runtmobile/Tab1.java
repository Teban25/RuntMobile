package runtmobile.udea.edu.co.runtmobile;

import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View.OnClickListener;
import java.io.IOException;

/**
 * Created by davigofr on 2015/12/09.
 */
public class Tab1 extends Fragment implements SurfaceHolder.Callback{

    private LayoutInflater inflater = null;
    // Libreria para manipular la camara
    Camera camera;
    byte[] bytesImage;
    boolean previewRunning = false;
    // Definicion del objeto donde se coloca la camara
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    Button takePicture;

    // Metodos sobre escritos para la toma de la foto
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
        }
    };

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
        }
    };
    Camera.PictureCallback pictureJPEG = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            if(data != null){
                bytesImage = data;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.captura, container, false);
        surfaceView = (SurfaceView) v.findViewById(R.id.surface);
        // Configuracion del surface para colocarle la camara
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        takePicture = (Button) v.findViewById(R.id.button);
        // Evento del boton para tomar la foto
        takePicture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(shutterCallback, pictureCallback, pictureJPEG);
            }
        });
        return v;
    }

    // Inicio: Metodos sobre escritos del surface para la gestion de la camara
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        try{
            if(previewRunning){
                camera.stopPreview();
                previewRunning = false;
            }
            Camera.Parameters p = camera.getParameters();
            p.setPreviewSize(width,height);
            camera.setDisplayOrientation(90);
            camera.setParameters(p);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            previewRunning = true;
        }catch(Exception e){}
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(holder);
            // Encedemos la camara
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopPreview();
        previewRunning = false;
        camera.release();
        camera = null;
    }
    // Fin metodos sobre escritos surface
}
