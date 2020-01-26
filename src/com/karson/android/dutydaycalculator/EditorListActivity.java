package com.karson.android.dutydaycalculator;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EditorListActivity extends Activity {
	public static final int FILEDIALOG = 5;
	public static final int RESETDIALOG = 2;
	public static final int MENUIMPORT = 0;
	public static final int MENUEXPORT = 1;
	public static final int MENURESET = 2;
	public static final int MENUDELETE = 3;
	public static final int MENUEDIT = 4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//TODO: test action bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.editor_list);
		// assign list item click listener
		((ListView) findViewById(R.id.editorListView))
				.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// open editor and pass id of selected item
						Intent intent = new Intent(EditorListActivity.this,
								ProfileEditorActivity.class);
						intent.putExtra("index", position);
						startActivity(intent);
					}
				});
		// assign long click listener for popup menu
		registerForContextMenu(findViewById(R.id.editorListView));
		// assign new item click listener
		findViewById(R.id.editorListNewProfile).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(EditorListActivity.this,
								ProfileEditorActivity.class);
						intent.putExtra("index", -1); // indicate that this is a
														// new entry
						startActivity(intent);
					}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		// populate listview based on profiles in profileList
		refresh();
	}

	private void refresh() {
		List<String> nameList = new ArrayList<String>();
		for (ProfileClass profile : OutputActivity.profiles.profileList) {
			nameList.add(profile.getProfileName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, nameList);
		ListView list = (ListView) findViewById(R.id.editorListView);
		list.setAdapter(adapter);
	}

	// longclick menu for edit/export/delete
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, MENUEDIT, 0, "Edit");
		menu.add(0, MENUEXPORT, 0, "Export");
		menu.add(0, MENUDELETE, 0, "Delete");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int position = ((ListView) findViewById(R.id.editorListView))
				.getPositionForView(info.targetView);
		switch (item.getItemId()) {
		case MENUEDIT:
			Intent intent = new Intent(EditorListActivity.this,
					ProfileEditorActivity.class);
			intent.putExtra("index", position);
			startActivity(intent);
			return true;
		case MENUEXPORT:
			ProfileClass profile = OutputActivity.profiles.profileList
					.get(position);
			// export file named after profile to a default location
			try {
				File path = new File(Environment.getExternalStorageDirectory(),
						"Duty Day Calculator");
				path.mkdir();
				File filepath = new File(path, profile.getProfileName()
						+ ".txt");
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(filepath));
				DataOutputStream out = new DataOutputStream(fos);
				profile.exportToReadableFile(out);
				fos.close();
				Toast.makeText(this,
						"Profile saved to:" + '\n' + filepath.toString(),
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.d("SAVE", "save err: " + e.toString());
				Toast.makeText(this, "Error Saving Profile", Toast.LENGTH_SHORT)
						.show();
			}
			return true;
		case MENUDELETE:
			OutputActivity.profiles.profileList.remove(position);
			// save change to file
			OutputActivity.profiles.exportList(ProfileList.PROFILESTORAGE,
					OutputActivity.profiles.profileList, this);
			refresh();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	// OPTIONS MENU ///////////////////////////////////////////////////
	// Options menu for this screen
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENUEXPORT, 0, "Export All Profiles").setIcon(
				android.R.drawable.ic_menu_save);
		menu.add(0, MENUIMPORT, 0, "Import Profiles").setIcon(
				android.R.drawable.ic_menu_upload);
		menu.add(0, MENURESET, 0, "Reset to Defaults").setIcon(
				android.R.drawable.ic_menu_revert);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, OutputActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		case MENUEXPORT:
			try {
				File path = new File(Environment.getExternalStorageDirectory(),
						"Duty Day Calculator");
				path.mkdir();
				File filepath = new File(path, "Profiles.txt");
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(filepath));
				DataOutputStream out = new DataOutputStream(fos);
				for (ProfileClass profile : OutputActivity.profiles.profileList) {
					profile.exportToReadableFile(out);
				}
				fos.close();
				Toast.makeText(this,
						"Profiles saved to:" + '\n' + filepath.toString(),
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.d("SAVE", "save err: " + e.toString());
				Toast.makeText(this, "Error Saving Profile", Toast.LENGTH_SHORT)
						.show();
			}
			return true;
		case MENUIMPORT:
			showDialog(FILEDIALOG);
			return true;
		case MENURESET:
			showDialog(RESETDIALOG);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Handles all dialogs for this activity
	//these are some variables needed to be accessible by file dialog
	private String[] fileList;
	private File path = new File(Environment.getExternalStorageDirectory(),
			"Duty Day Calculator");
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);
		switch (id) {
		case RESETDIALOG:
			AlertDialog.Builder resetbuilder = new AlertDialog.Builder(this);
			return resetbuilder
					.setMessage(
							"Load Default Profiles? This will overwrite any custom profiles!")
					.setPositiveButton("Yes", defaultsDialogListener)
					.setNegativeButton("No", defaultsDialogListener).create();
		case FILEDIALOG:
			try {
				path.mkdirs();
			} catch (SecurityException e) {
				Log.e("DDC", "unable to write on the sd card " + e.toString());
			}
			if (path.exists()) {
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String filename) {
						File sel = new File(dir, filename);
						return filename.contains(".txt") || sel.isDirectory();
					}
				};
				fileList = path.list(filter);
			}
			builder.setTitle("Choose file from folder"+'\n'+ "Duty Day Calculator");
			if (fileList == null) {
				Log.e("DDC", "Showing file picker before loading the file list");
				Toast.makeText(this, "Error selecting file",Toast.LENGTH_SHORT).show();
				return dialog;
			}
			builder.setItems(fileList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int position) {
					String chosenFile = fileList[position];
					File fin = new File(path, chosenFile);
					Log.d("FILE",fin.toString());
					// import
					try {
						OutputActivity.profiles.profileList.addAll(OutputActivity.profiles
								.importReadableList(fin,
										getBaseContext()));
					} catch (Exception e) {
						Log.d("DDC", "import list err " + e.toString());
					}
					//save and update
					OutputActivity.profiles.exportList(ProfileList.PROFILESTORAGE,
							OutputActivity.profiles.profileList, getBaseContext());
					refresh();
				}
			});
			dialog = builder.show();
		default:
			return null;// OOOPS!
		}
	}


	DialogInterface.OnClickListener defaultsDialogListener = new DialogInterface.OnClickListener() {
		// @Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				// Yes button clicked, load defaults
				OutputActivity.profiles
						.resetWithDefault(getApplicationContext());
				refresh();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				// No clicked, simply exit dialog
				break;
			}
		}
	};

}
