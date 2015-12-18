package runtmobile.udea.edu.co.runtmobile;

import android.hardware.Camera;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

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
    String resultado;
    // Objeto para invocar el servicio
    GetVehicle getInformation;
    JSONObject datosRecibidos;
    ProgressDialog progress;
    TextView nombre;
    TextView apellido;
    TextView dni;
    TextView licencia;
    TextView placa;
    TextView marca;
    TextView color;
    TextView modelo;
    TextView tecno;
    TextView soat;


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
                tabHost.setCurrentTabByTag("tab2");
                progress = new ProgressDialog(getActivity());
                getInformation = new GetVehicle(progress);
                getInformation.execute();
            }
        }
    };

    //Tarea asincrona para llamar el WS

    private  class GetVehicle extends AsyncTask<Void, Void, Boolean> {
        private String TAG = "getVehicle";
        String response;

        public GetVehicle(ProgressDialog progress1) {
            progress = progress1;
        }

        public void onPreExecute() {
            progress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(TAG, "doInBackground");
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://10.10.23.3:8080/RuntWebApp/rest/vehicle");
                ByteArrayEntity entity = new ByteArrayEntity(bytesImage);
                entity.setContentType("application/octet-stream");
                httpPost.setEntity(entity);
                HttpResponse resp = client.execute(httpPost);
                resultado = EntityUtils.toString(resp.getEntity());
                System.out.println("Resultado: " + resultado.toString() + "");
                datosRecibidos = new JSONObject(resultado);
            } catch (Exception e) {
                Log.e("RESTService", "Error:", e);
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(success==false){
                progress.dismiss();
                Toast.makeText(getActivity().getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }else{
                progress.dismiss();
                setInformationDetail(datosRecibidos);
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(getActivity().getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
        }
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
    public void setInformationDetail(JSONObject datosEntrega){
        nombre = (TextView)getActivity().findViewById(R.id.textViewNombre);
        apellido = (TextView)getActivity().findViewById(R.id.textViewApellido);
        dni = (TextView)getActivity().findViewById(R.id.textViewIdentificacion);
        licencia = (TextView)getActivity().findViewById(R.id.textViewLicencia);
        placa = (TextView)getActivity().findViewById(R.id.textViewPlaca);
        marca = (TextView)getActivity().findViewById(R.id.textViewMarca);
        color = (TextView)getActivity().findViewById(R.id.textViewColor);
        modelo = (TextView)getActivity().findViewById(R.id.textViewModelo);
        tecno = (TextView)getActivity().findViewById(R.id.textViewRevision);
        soat = (TextView)getActivity().findViewById(R.id.textViewSOAT);
        // Seteo cada uno de los textView con su respectivo valor del Json
        try{
            nombre.setText(datosEntrega.getString("name"));
            apellido.setText(datosEntrega.getString("lastName"));
            dni.setText(datosEntrega.getString("idNumber"));
            licencia.setText(datosEntrega.getString("licenseNumber"));
            placa.setText(datosEntrega.getJSONArray("vehicles").getJSONObject(0).getString("carriagePlate"));
            marca.setText(datosEntrega.getJSONArray("vehicles").getJSONObject(0).getJSONObject("brand").getString("name"));
            color.setText(datosEntrega.getJSONArray("vehicles").getJSONObject(0).getString("color"));
            modelo.setText(datosEntrega.getJSONArray("vehicles").getJSONObject(0).getString("model"));
            tecno.setText(datosEntrega.getJSONArray("vehicles").getJSONObject(0).getString("mechanicalTechno"));
            soat.setText(datosEntrega.getJSONArray("vehicles").getJSONObject(0).getString("soat"));
            setVisiblesTextV();
        }catch (Exception e){
            Log.e("JSON ERROR","Error:",e);
        }
    }

    public void setVisiblesTextV(){
        //Se setean todos los campos a visibles
        nombre.setVisibility(View.VISIBLE);
        apellido.setVisibility(View.VISIBLE);
        dni.setVisibility(View.VISIBLE);
        licencia.setVisibility(View.VISIBLE);
        placa.setVisibility(View.VISIBLE);
        marca.setVisibility(View.VISIBLE);
        color.setVisibility(View.VISIBLE);
        modelo.setVisibility(View.VISIBLE);
        tecno.setVisibility(View.VISIBLE);
        soat.setVisibility(View.VISIBLE);
    }
}
