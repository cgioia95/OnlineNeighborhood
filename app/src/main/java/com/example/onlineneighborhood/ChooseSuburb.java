package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ChooseSuburb extends AppCompatActivity {

    private SearchView suburbSearch;
    private ListView suburbList;
    private ArrayList<String[]> suburbs;
    private ArrayList<String> suburbNames;
    private String suburbid;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_suburb);

        suburbSearch = findViewById(R.id.searchSuburb);
        suburbList = findViewById(R.id.suburbList);
        suburbs = parseCSV();
        suburbNames = convertArray(suburbs, 0);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,suburbNames);
        suburbList.setAdapter(arrayAdapter);

        suburbSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                arrayAdapter.getFilter().filter(s);
                return false;
            }
        });

        suburbList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //when item is clicked in the list view,
                // we take its name, check if it is in our list of suburbs,
                // and if it is we start the intent
                String name = adapterView.getItemAtPosition(i).toString().trim();
                if(findSuburbId(suburbs, name)){
                   startIntent();
                } else{
                    Toast.makeText(getApplicationContext(), "please try again", Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    public void startIntent(){
        //setting the global variable of suburb so that the rest of the app
        // can access this information without excessive passing of intents
        ((OnlineNeighborhood) this.getApplication()).setsuburb(suburbid);
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        startActivity(intent);
    }


    /***
     * method which extracts each row of data from a CSV file which has suburb information saved in it
     * each row is extracted into a String[] array containing 3 feilds, name, postcode, and firebase ID
     * @return  an arraylist of the CSV file saved locally
     */
    public ArrayList<String[]> parseCSV(){
        ArrayList<String[]> suburbList = new ArrayList<String[]>();
        try{
            InputStream is = getResources().openRawResource(R.raw.melbourne_suburbs);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8"))
            );
            String line = "";
            while ((line = reader.readLine()) != null) {
                //spilt by ","
                String [] tokens = line.split(",");
                //read the data
                suburbList.add(tokens);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return suburbList;
    }

    /**
     * simple method which seperates the csv columns and extracts the data, returning a array list
     * of the speicific column requested
     * @param arr --  requires ArrayList which should be created from CSV
     * @param index -- choose which column index of CSV you want to extract
     * @return -- returns ArrayList of specified column
     */
    public ArrayList<String> convertArray(ArrayList<String[]> arr, int index){

        //Declaration and initialise String Array
        ArrayList<String> str = new ArrayList<String>();

        //ArrayList to Array Conversion
        for (int j = 0; j < arr.size(); j++) {
            //Assign each value to String array
            String[] strings = arr.get(j);
            str.add(strings[index]);
        }
        return str;
    }

    //Checks if suburb is found within a list and sets it to SuburbID
    private boolean findSuburbId(ArrayList<String[]> idList, String suburb){
        boolean successfulFind = false;
        for(int i = 0; i < idList.size(); i++){
            String[] details = idList.get(i);
            if(details[0].equals(suburb)){
                suburbid = details[2];
                successfulFind = true;
                break;
            }
        }

        return successfulFind;
    }
}
