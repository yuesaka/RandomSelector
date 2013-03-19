package com.example.randomselector;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class EditList extends Activity {
	private EditText mEditText;
	
	private static final int TYPE_CATEGORY = 0;
	private static final int TYPE_ITEM = 1;
	
	private static int edit_type = -1;
	
	private static DbAdapter mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_list);
		mDbHelper = new DbAdapter(this);
		mDbHelper.open();
		
		mEditText = (EditText) findViewById(R.id.new_content);
		
		Bundle extras =  getIntent().getExtras();
		if(extras == null) {
			System.out.println("wtfff");
			
		}
		//System.out.println(extras.getString("hintText"));
		if(extras.getString("hintText") == null) {
			System.out.println("noooo");
			
		}
		mEditText.setHint(extras.getString("hintText"));
	
		if (extras.getString("hintText").equals(getString(R.string.category_hint))){
			edit_type = TYPE_CATEGORY;
		} else if (extras.getString("hintText").equals(getString(R.string.item_hint))){
			edit_type = TYPE_ITEM;
		}

	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		mDbHelper.close();
		
	}
	
	public void updateList(View theButton){
		saveState();
		setResult(RESULT_OK);
		finish();
	}
	
	private void saveState(){
		String content = mEditText.getText().toString();
		if (!content.isEmpty()){
			if(edit_type == TYPE_CATEGORY) {
				System.out.println("hereerere?");
				mDbHelper.createCategory(content);
			} else if(edit_type == TYPE_ITEM) {
				mDbHelper.createItem(content, getIntent().getExtras().getString("categoryName"), 0);
			}
			
		}
		
	}
}
