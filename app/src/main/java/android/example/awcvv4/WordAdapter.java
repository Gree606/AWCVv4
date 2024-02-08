package android.example.awcvv4;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WordAdapter extends ArrayAdapter<String> {

    public WordAdapter(Activity activity, int grid_item, ArrayList<String> langVal){
        super(activity,0,langVal);
    }
    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridViewItem=convertView;
        if(gridViewItem==null){
            gridViewItem= LayoutInflater.from(getContext()).inflate(R.layout.grid_item,parent,false);
        }
        String currentWord=getItem(position);
        TextView defaultTextView=(TextView)gridViewItem.findViewById(R.id.LangName);
        String[] valueListLang={"ಕನ್ನಡ","தமிழ்","हिन्दी","English","ગુજરાતી","తెలుగు","मराठी"};
        List<String> availableLang = Arrays.asList(valueListLang);
        String[] valueListDest={"Default","5G","५.जी","5ஜி","5.ಜಿ","५जी","5జి","5જી","IC","आई.सी","ஐ.சி"};
        List<String> availableDest = Arrays.asList(valueListDest);
        if (availableLang.contains(currentWord)||availableDest.contains(currentWord)) {
            defaultTextView.setBackgroundResource(R.drawable.round_corner_active);
            defaultTextView.setTextColor(Color.WHITE);
        }
        else
        {
            defaultTextView.setBackgroundResource(R.drawable.round_corner_inactive);
            defaultTextView.setTextColor(Color.DKGRAY);
        }
        defaultTextView.setText(currentWord);
        return gridViewItem;

    }
}
