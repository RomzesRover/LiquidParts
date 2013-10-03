package com.BBsRs.liquidpartsNew;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Donate extends Fragment{
	
	String Info;
	View view;
	TextView donated;
	Button donate;
	
	public Donate() { 
		this("");
	}
	
	public Donate(String Info) {
		this.Info=Info;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.donate, null);
		TextView wv = (TextView)view.findViewById(R.id.textView1);
		donated = (TextView)view.findViewById(R.id.textViewDonated);
		donate = (Button)view.findViewById(R.id.buttonDonate);
		
		SharedPreferences settingsTweaks = getActivity().getSharedPreferences("lpts", Context.MODE_PRIVATE);
		if (settingsTweaks.getBoolean("paid", false)){
			donated.setVisibility(View.VISIBLE);
			donate.setVisibility(View.GONE);
		}
		
		wv.setText(Html.fromHtml(Info));
		wv.setMovementMethod(LinkMovementMethod.getInstance());
    	
    	donate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((HelpActivity)getActivity()).buyButton();
			}
		});
		return view;
	}
}
