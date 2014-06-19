package com.lanian.nfctest;

import java.io.IOException;

import android.app.Activity;
import android.app.Fragment;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NfcDiscoveredActivity extends Activity implements View.OnClickListener {
	static final String TAG = "NfcTest";
	static final String DOMAIN = "com.lanian";
	static final String TYPE_PLACE = "place";
	static final String PLACE_HOME = "home";
	static final String PLACE_WORK = "work";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc_discovered);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nfc_discovered, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_clear_tag) {
			clearTag();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void clearTag() {
		Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag == null)
			return;
		try {
			Ndef ndef = Ndef.get(tag);
			ndef.connect();
			ndef.writeNdefMessage(new NdefMessage(new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)));
			ndef.close();
		} catch (IOException | FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		String action = getIntent().getAction();
		((TextView)findViewById(R.id.textView_action)).setText(action);
		Log.d(TAG, action);
		
		printTechList();

		((TextView)findViewById(R.id.textView_tag)).setText(R.string.no_tag);
		if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
			handleNdefDiscovered();
		} else if (action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
			handleTechDiscovered();
		} else if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
			
		}

	}

	private void handleNdefDiscovered() {
		Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMsgs != null) {
			for (Parcelable raw : rawMsgs)
				handleNdefMessage((NdefMessage)raw);
		}
	}

	private void handleTechDiscovered() {
		Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag == null)
			return;
		Ndef ndef = Ndef.get(tag);
		try {
			ndef.connect();
			handleNdefMessage(ndef.getNdefMessage());
		} catch (IOException | FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				ndef.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handleNdefMessage(NdefMessage message) {
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
	}

	private void printTechList() {
		Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag == null)
			return;
		Log.d(TAG, "<tech-list>");
		for (String tech : tag.getTechList())
			Log.d(TAG, String.format("\t<tech>%s</tech>", tech));
		Log.d(TAG, "</tech-list>");
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
			View rootView = inflater.inflate(R.layout.fragment_nfc_discovered, container,
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
			writeTag(PLACE_HOME);
			break;
		case R.id.button_write_tag_work:
			writeTag(PLACE_WORK);
			break;
		}
	}
	
	private void writeTag(String name) {
		Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
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
