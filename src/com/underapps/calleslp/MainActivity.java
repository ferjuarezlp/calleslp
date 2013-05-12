package com.underapps.calleslp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.underapps.calleslp.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	GoogleMap googleMap;
    MarkerOptions opcionesMarcador;
    LatLng latLng;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Context context = getApplicationContext();
		SupportMapFragment supportMapFragment = (SupportMapFragment)
			        getSupportFragmentManager().findFragmentById(R.id.map);
			 
		// Referencia al mapa
		googleMap = supportMapFragment.getMap();
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				-34.920626, -57.954388),
				13));
		
		final EditText editCalles = (EditText) findViewById(R.id.editCalle);
		final EditText editNumero = (EditText) findViewById(R.id.editNumero);
		final TextView textResult = (TextView) findViewById(R.id.textResultado);
		final ImageView imageTipo = (ImageView) findViewById(R.id.imageTipo);
		
		Button btnCalcular = (Button) findViewById(R.id.btnCalcular);
		
		// Listener para cuando ingresamos calle
		editCalles.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	if (s.length() == 2){
	        		String calle = s.toString();
	        		if(esDiagonal(Integer.parseInt(calle))){
	        			imageTipo.setImageResource(R.drawable.diagonal);
	        		} else imageTipo.setImageResource(R.drawable.calle);
	        		imageTipo.setVisibility(View.VISIBLE);
	        	} else if (s.length() < 2){
	        		imageTipo.setVisibility(View.INVISIBLE);
	        	} else if(s.length() > 2){
	        		
	        		// Direccion invalida 
	        		Toast.makeText(context, getString(R.string.direccion_invalida), Toast.LENGTH_LONG).show();
	        		imageTipo.setVisibility(View.INVISIBLE);
	        		editCalles.setText("");
	        	}
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
		
		btnCalcular.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int resultado = calcular(Integer.valueOf(editCalles.getText().toString()), Integer.valueOf(editNumero.getText().toString()));
				String strResultado = editCalles.getText().toString() + " N " + editNumero.getText().toString() + " La Plata, Buenos Aires, Argentina";
				
				String label = getString(R.string.la_direccion_buscada) + " " +editCalles.getText() + " " +getString(R.string.entre) + " " + resultado + " " + getString(R.string.y) + " ";
				if (resultado == 51) {
					// Calle 52 no existe
					label = label + (resultado +2); 
				} else label = label + (resultado + 1); 
				 
				
				textResult.setText(label);
				
				 if(strResultado!=null && !strResultado.equals("")){
	                    new GeocoderTask().execute(strResultado);
	            }
			}
		});
		
		
	}

	public int calcular(int calle, int numero) {
		int cantCifras = cantCifras(numero);
		int resultado = 0;

		switch (cantCifras) {

		case 3:

			int primeraCifra = numero / 100;

			if (esParalelaAv7(calle)) {
				resultado = primeraCifra * 2;
				if (resultado < 52) {
					return resultado + 32;
				} else
					return resultado + 33;

			} else if (esParalelaCalle50(calle)) {

				resultado = (primeraCifra * 2) - 5;

			} else if (esDiagonal(calle)) {
				if (calle == 73 || calle == 74 || calle == 79 || calle == 80) {
					resultado = primeraCifra - 5;
				} else if (calle == 75 || calle == 76) {
					resultado = primeraCifra + 14;
				} else if (calle == 77 || calle == 78) {
					resultado = primeraCifra + 1;
				}

			}

			break;
		case 4:

			int primeraSegundaCifra = numero / 100;

			if (esParalelaAv7(calle)) {

				resultado = primeraSegundaCifra * 2;
				if (resultado < 52) {
					return resultado + 33;
				} else
					return resultado + 32;

			} else if (esParalelaCalle50(calle)) {

				resultado = primeraSegundaCifra * 2 - 5;

			} else if (esDiagonal(calle)) {

				if (calle == 73 || calle == 74 || calle == 79 || calle == 80) {
					resultado = primeraSegundaCifra - 5;
				}

				else if (calle == 75 || calle == 76) {
					resultado = primeraSegundaCifra + 14;
				} else if (calle == 77 || calle == 78) {
					resultado = primeraSegundaCifra + 1;
				}

			}

			break;
		default:
			break;
		}

		return resultado;
	}
	
	public int cantCifras(int numero){
		String str = Integer.toString(numero);
		return str.length();
	}
	
	public boolean esParalelaAv7(int calle){
		if ((calle >= 1) && (calle <= 33)){
			return true;
		} else return false;
	}
	
	public boolean esParalelaCalle50(int calle){
		if ((calle >= 32) && (calle <= 72)){
			return true;
		} else return false;
	}
	
	public boolean esDiagonal(int calle){
		HashSet diagonales = new HashSet<Integer>(Arrays.asList(73, 74,79,80,75,76,77,78));
		if (diagonales.contains(calle)){
			return true;
		} else return false;
	}
	
	
	// AsyncTask para Geocodificacion
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{
 
        @Override
        protected List<Address> doInBackground(String... direccion) {
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> direcciones = null;
 
            try {
                // segundo parametro de getFromLocationName = cantidad de resultados 
                direcciones = geocoder.getFromLocationName(direccion[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return direcciones;
        }
 
		@Override
		protected void onPostExecute(List<Address> direcciones) {

			if (direcciones == null || direcciones.size() == 0) {
				Toast.makeText(getBaseContext(),
						getString(R.string.no_existe_direccion), Toast.LENGTH_SHORT)
						.show();
			}

			// Eliminar marcadores del mapa
			googleMap.clear();

			// Agrego marcador al mapa
			Address direccion = (Address) direcciones.get(0);

			// Instancio GeoPoint para mostrar en el mapa
			latLng = new LatLng(direccion.getLatitude(),
					direccion.getLongitude());

			// Defino label para mostrar en el marcador
			String labelDireccion = String.format("%s, %s", direccion
					.getMaxAddressLineIndex() > 0 ? direccion.getAddressLine(0)
					: "", direccion.getCountryName());

			// Opciones para el marcador
			opcionesMarcador = new MarkerOptions();
			opcionesMarcador.position(latLng);
			opcionesMarcador.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
			opcionesMarcador.title(labelDireccion);

			// Agrego el marcador al mapa
			googleMap.addMarker(opcionesMarcador);

			// Centrar el mapa en la posicion dada, con zoom de 15
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
					15));
		}
        
    }
	
	

}
