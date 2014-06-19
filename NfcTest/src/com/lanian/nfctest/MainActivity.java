package com.lanian.nfctest;

import java.io.IOException;

import android.app.Activity;
import android.app.Fragment;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {
	static final String TAG = "NfcTest";
	static final String DOMAIN = "com.lanian";
	static final String TYPE_PLACE = "place";
	
	Tag tag = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		String action = getIntent().getAction();
		Log.d(TAG, action);
		if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			
		} else if (action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
			((TextView)findViewById(R.id.textView_tag)).setText(R.string.no_tag);
			
			tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
			for (String tech : tag.getTechList())
				Log.d(TAG, "Tech: "+tech);

			Ndef ndef = Ndef.get(tag);
			try {
				ndef.connect();
				NdefMessage message = ndef.getNdefMessage();
				if (message != null) {
					for (NdefRecord record : message.getRecords()) {
				
						Log.d(TAG, "TNF: "+record.getTnf());
						if (record.getTnf() == NdefRecord.TNF_EXTERNAL_TYPE) {
							Log.d(TAG, "Type: "+new String(record.getType()));
							if (new String(record.getType()).equals(DOMAIN+":"+TYPE_PLACE)) {
								print("Payload", record.getPayload());
								Log.d(TAG, "Uri: "+record.toUri().toString());
								String name = new String(record.getPayload());
								((TextView)findViewById(R.id.textView_tag)).setText(name);
							}
						}
						
					}
					
				}
			} catch (IOException | FormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					//mifare.close();
					ndef.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
			
		}

	}
	
	private void print(String name, byte[] buf) {
		StringBuilder sb = new StringBuilder(name);
		sb.append(": ");
		for (int i = 0; i < buf.length; ++i) {
			sb.append(String.format("%02X ", buf[i]));
		}
		Log.d(TAG, sb.toString());
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			
			rootView.findViewById(R.id.button_write_tag_home).setOnClickListener((View.OnClickListener)getActivity());
			rootView.findViewById(R.id.button_write_tag_work).setOnClickListener((View.OnClickListener)getActivity());
			
			return rootView;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_write_tag_home:
			writeTag(tag, "home");
			break;
		case R.id.button_write_tag_work:
			writeTag(tag, "work");
			break;
		}
	}
	
	private void writeTag(Tag tag, String name) {
		if (tag == null)
			return;
		try {
			Ndef ndef = Ndef.get(tag);
			ndef.connect();
			ndef.writeNdefMessage(new NdefMessage(NdefRecord.createExternal(DOMAIN, TYPE_PLACE, name.getBytes("UTF-8"))));
			ndef.close();
		} catch (IOException | FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
