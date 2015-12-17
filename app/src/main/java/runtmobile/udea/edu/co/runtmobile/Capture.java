package runtmobile.udea.edu.co.runtmobile;

import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.ProgressDialog;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by davigofr on 2015/12/09.
 */
public class Capture extends Fragment implements SurfaceHolder.Callback{

    // Progress Dialog Object
    ProgressDialog prgDialog;
    //Fragment que contiene las pestañas
    FragmentTabHost tabHost;
    // Libreria para manipular la camara
    Camera camera;
    byte[] bytesImage;
    boolean previewRunning = false;
    // Definicion del objeto donde se coloca la camara
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    ImageButton takePicture;
    // Objeto de mensajes
    Toast toast;

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
                // Se notifica al usuario que esta enviando la imagen
                toast = Toast.makeText(getActivity(),"Procesando solicitud...", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                tabHost.setCurrentTabByTag("tab2");
                RequestParams params = new RequestParams();
                params.put("imageData", bytesImage);
                invokeWS(params);
            }
        }
    };

    public void invokeWS(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post("http://localhost:8080/RuntWebApp/rest/vehicle", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.capture, container, false);
        surfaceView = (SurfaceView) v.findViewById(R.id.surface);
        tabHost = (FragmentTabHost) getActivity().findViewById(android.R.id.tabhost);
        // Configuracion del surface para colocarle la camara
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        takePicture = (ImageButton) v.findViewById(R.id.buttonCam);
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
        }catch(Exception e){
            Log.e("Exception","Ha sucedido un error cuando se cambio la configuracion de la camara",e);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(holder);
            // Encendemos la camara
            camera.startPreview();
        } catch (IOException e) {
            Log.e("IOException","Ha sucedido un error al iniciar la configuración de la camara",e);
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

    // Metodo para asignar la información entregada por el servicio en los componentes.
    public void setInformationDetail(){

    }
}
