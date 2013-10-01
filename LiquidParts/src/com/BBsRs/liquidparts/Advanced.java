package com.BBsRs.liquidparts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

public class Advanced extends Fragment{
	View view;
	SharedPreferences settingsAdvanced;
	SharedPreferences.Editor prefEditor;
	
	String[] suggestedVddThepasto = {"900","925","975","1025","1200","1250","1300","1300","1300","1325","1425","1475","1525","1600","1600"};	
	
	boolean enabledSvs, enabledSuggestedVdds, setOnBoot;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.three_r, null);
		settingsAdvanced = getActivity().getSharedPreferences("lpadvanced", Context.MODE_PRIVATE);
		prefEditor = settingsAdvanced.edit();
		
		readConfig();																		//readData
																							//Enabled SVS
		CheckBox checkBoxEnabled = (CheckBox)view.findViewById(R.id.CheckBoxEnabledSvs);
		checkBoxEnabled.setChecked(enabledSvs);
		checkBoxEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				enabledSvs=arg1;
				writeConfig();
				UpdateIU();
				if(!arg1)
	    			cleanUp();																//reset all settings on this page to default
			}
		});
		
		Button butSetSugg = (Button) view.findViewById(R.id.button1);						//set suggested by thepasto
		butSetSugg.setEnabled(enabledSuggestedVdds);
		butSetSugg.setOnClickListener(new Button.OnClickListener() {  
			@Override
			public void onClick(View v) {
				setVddPicker(getFreqs(), suggestedVddThepasto);
			}
         });
		
		Button butResDef = (Button) view.findViewById(R.id.button2);						//set suggested by default in kernel
		butResDef.setOnClickListener(new Button.OnClickListener() {  
			@Override
			public void onClick(View v) {
				String[] defaultVdd = settingsAdvanced.getString("defaultVdds", "1000 1000 1075 1025 1150 1250 1300 1300 1300 1300 1300 1300 1300 1300 1300").split(" ");
				setVddPicker(getFreqs(),defaultVdd);
			}
         });
		
		CheckBox checkBoxSetOnBoot = (CheckBox) view.findViewById(R.id.CheckBoxSetOnBoot);
		checkBoxSetOnBoot.setChecked(setOnBoot);
		checkBoxSetOnBoot.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				setOnBoot=arg1;
			}
		});
		
		Button save = (Button) view.findViewById(R.id.Button01);						//set suggested by default in kernel
		save.setOnClickListener(new Button.OnClickListener() {  
			@Override
			public void onClick(View v) {
				writeVdds();
			}
         });
		
		return view;
	}
	
	public void readConfig(){
		enabledSvs = settingsAdvanced.getBoolean("svs", false);								//Enabled SVS
		setOnBoot = settingsAdvanced.getBoolean("sob", false);								//Enable set on boot
		setVddPicker(getFreqs(), getVdds());												//set freqs and vdds
		enabledSuggestedVdds = suggestedVddThepasto.length==settingsAdvanced.getString("defaultFreqs", "").split(" ").length ? true : false;
		UpdateIU();
	}
	
	public void writeConfig(){
		prefEditor.putBoolean("svs", enabledSvs);											//Enabled SVS
		prefEditor.commit();
	}
	
	public void writeVdds(){
		String vddtable=parsePref();
		BashCommand.doCmds("su","rm /data/local/userinit.d/03customvdds");
		prefEditor.putBoolean("sob", setOnBoot);											//Enable set on boot
		if (setOnBoot)
			BashCommand.doCmds("su","echo \""+vddtable+"\" > /data/local/userinit.d/03customvdds && chmod 755 /data/local/userinit.d/03customvdds");
		prefEditor.commit();
		BashCommand.doCmds("su",vddtable);
	}
	
	public void cleanUp(){
		String[] defaultVdd = settingsAdvanced.getString("defaultVdds", "1000 1000 1075 1025 1150 1250 1300 1300 1300 1300 1300 1300 1300 1300 1300").split(" ");
		String[] freqs = getFreqs();
		String fcont="";
		for (int i=0; i<freqs.length; i++){
			fcont+="echo '"+freqs[i]+" ";
			fcont+=defaultVdd[i]+"' > /sys/devices/system/cpu/cpu0/cpufreq/vdd_levels\n";
			prefEditor.putString(freqs[i], defaultVdd[i]);
		}
		prefEditor.putBoolean("sob", false);
		prefEditor.commit();
		BashCommand.doCmds("su","rm /data/local/userinit.d/03customvdds");
		BashCommand.doCmds("su",fcont);

	}
	
	public String parsePref(){
		String fcont ="";
		String [] freqs = getFreqs();
		String [] vdds = getVdds();
		for (int i=0; i<freqs.length; i++){
			fcont+="echo '"+freqs[i]+" ";
			fcont+=settingsAdvanced.getString(freqs[i], vdds[i])+"' > /sys/devices/system/cpu/cpu0/cpufreq/vdd_levels\n";
		}
		Log.i("LIQUIDPARTS", fcont);
		return fcont;
	}
	
	public void UpdateIU(){
		TableLayout table = (TableLayout) view.findViewById(R.id.TableLayout01);
		final CheckBox cbsob = (CheckBox) view.findViewById(R.id.CheckBoxSetOnBoot);
		ImageView imgsep2 = (ImageView) view.findViewById(R.id.imageView2);
		ImageView imgsep3 = (ImageView) view.findViewById(R.id.ImageView05);
		ImageView imgsep4 = (ImageView) view.findViewById(R.id.ImageView02);
		Button butapp = (Button) view.findViewById(R.id.button1);
		Button butss = (Button) view.findViewById(R.id.button2);
		Button butres = (Button) view.findViewById(R.id.Button01);
		TextView textSet = (TextView) view.findViewById(R.id.TextView02);
		
		if (enabledSvs){
			table.setVisibility(View.VISIBLE);
			cbsob.setVisibility(View.VISIBLE);
			imgsep2.setVisibility(View.VISIBLE);
			imgsep3.setVisibility(View.VISIBLE);
			imgsep4.setVisibility(View.VISIBLE);
			butapp.setVisibility(View.VISIBLE);
			butss.setVisibility(View.VISIBLE);
			butres.setVisibility(View.VISIBLE);
			textSet.setVisibility(View.VISIBLE);
		} else {
			table.setVisibility(View.GONE);
			cbsob.setVisibility(View.GONE);
			imgsep2.setVisibility(View.GONE);
			imgsep3.setVisibility(View.GONE);
			imgsep4.setVisibility(View.GONE);
			butapp.setVisibility(View.GONE);
			butss.setVisibility(View.GONE);
			butres.setVisibility(View.GONE);
			textSet.setVisibility(View.GONE);
		}
	}
	
	public void setVddPicker(String[] freq, String[] vdd) {

		TableLayout table = (TableLayout) view.findViewById(R.id.TableLayout01);
		table.removeAllViews();

		for (int i=0; i<freq.length; i++){
			final TableRow row = new TableRow(getActivity());
			final TextView cfreq = new TextView(getActivity());
			final TextView cvdd = new TextView(getActivity());
			final Button upvdd = new Button(getActivity());
			final Button downvdd = new Button(getActivity());

			prefEditor.putString(freq[i], vdd[i]);
			prefEditor.commit();
			cfreq.setText(freq[i]);
			cvdd.setText(settingsAdvanced.getString(freq[i], vdd[i]));
			upvdd.setText("+");
			downvdd.setText("-");

			row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));
			row.addView(cfreq);
			row.addView(downvdd);
			row.addView(cvdd);
			row.addView(upvdd);
			table.addView(row);

			upvdd.setOnClickListener(new Button.OnClickListener() {  

				@Override
				public void onClick(View v) {
				try {
					int fval=Integer.valueOf(cvdd.getText().toString());
					if(fval<1600){
						cvdd.setText(String.valueOf(fval+25));
						prefEditor.putString(cfreq.getText().toString(),cvdd.getText().toString());
						prefEditor.commit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				}
	         });

			downvdd.setOnClickListener(new Button.OnClickListener() {  

				@Override
				public void onClick(View v) {
				try {
					int fval=Integer.valueOf(cvdd.getText().toString());
					if(fval>800) {
						cvdd.setText(String.valueOf(fval-25));
						prefEditor.putString(cfreq.getText().toString(),cvdd.getText().toString());
						prefEditor.commit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				}
	         });
		}		
	}	
	
	public String[] getFreqs(){
		String freqs="";
		freqs=BashCommand.doCmds("sh","cat /sys/devices/system/cpu/cpu0/cpufreq/vdd_levels | awk -F': ' '{print $1}' | xargs echo -ne");
		if (!settingsAdvanced.getBoolean("hasDefaultFreqs", false)){
			prefEditor.putString("defaultFreqs", freqs.replace("\n",""));								//here we save default freqs from kernel
			prefEditor.putBoolean("hasDefaultFreqs", true);
			prefEditor.commit();
		};	
		return(freqs.replace("\n","").split(" "));
	}
	
	public String[] getVdds(){
		String vdds="";
		vdds=BashCommand.doCmds("sh","cat /sys/devices/system/cpu/cpu0/cpufreq/vdd_levels | awk -F': ' '{print $2}' | xargs echo -ne");
		if (!settingsAdvanced.getBoolean("hasDefaultVdds", false)){										//here we save default vdds from kernel
			prefEditor.putString("defaultVdds", vdds.replace("\n",""));
			prefEditor.putBoolean("hasDefaultVdds", true);
			prefEditor.commit();
			Log.i("RR", vdds);
		};
		return(vdds.replace("\n","").split(" "));
		
	}

}
